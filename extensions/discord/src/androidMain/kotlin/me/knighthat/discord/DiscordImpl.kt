package me.knighthat.discord

import android.content.Context
import app.kreate.gateway.ImageHostingService
import app.kreate.gateway.discord.DiscordApi
import app.kreate.utils.ImageProcessor
import app.kreate.utils.isLocalFile
import co.touchlab.kermit.Logger
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import com.eygraber.uri.toKmpUri
import kizzy.gateway.DiscordWebSocket
import kizzy.gateway.DiscordWebSocketImpl
import kizzy.gateway.entities.presence.Activity
import kizzy.gateway.entities.presence.Assets
import kizzy.gateway.entities.presence.Presence
import kizzy.gateway.entities.presence.Timestamps
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import me.knighthat.exception.SessionNotAvailableException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndUpdate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@ExperimentalAtomicApi
class DiscordImpl : Discord, KoinComponent {

    companion object {
        private const val LOGGING_TAG = "DiscordRPC"
        private const val APPLICATION_ID = "1370148610158759966"
        private const val MAX_DIMENSION = 1024                           // Per Discord's guidelines
        private const val MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024     // 2 MB in bytes
        private const val KREATE_IMAGE_URL = "https://i.ibb.co/v4CzX3kT/discord-rpc-kreate.jpg"

        private val cachedExternalUrls = ConcurrentHashMap<String, String>()
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName(LOGGING_TAG))
    }

    private val context: Context by inject()
    private val logger = Logger.withTag( LOGGING_TAG )
    private val lock = Mutex()
    private val smallImage by lazy(::getAppLogoUrl)
    private val _session = AtomicReference<DiscordWebSocket?>(null)
    private val _token = MutableStateFlow<String?>(null)
    private val _isActive = AtomicBoolean(false)

    @Volatile
    private var previousPresence: Presence? = null
    @Volatile
    private var state: State = State.BROWSING

    init { onTokenChanged() }

    //<editor-fold defaultstate="collapsed" desc="External image handler">
    private suspend fun uploadLocalArtwork( artworkUri: Uri ): Result<String> =
        runCatching {
            val uploadableUri = ImageProcessor.compressArtwork(
                artworkUri,
                MAX_DIMENSION,
                MAX_DIMENSION,
                MAX_FILE_SIZE_BYTES
            )
            logger.d {
                if( artworkUri !== uploadableUri )
                    "Upload compressed version $uploadableUri"
                else
                    "Upload artwork without any compression"
            }

            val androidUri = uploadableUri.toAndroidUri()
            val (mimeType, fileData) = with( context.contentResolver ) {
                getType( androidUri )!! to openInputStream( androidUri )!!.readBytes()
            }
            ImageHostingService.uploadToLitterBox( mimeType, fileData )
                               .getOrThrow()
        }.onSuccess {
            logger.d { "Local artwork uploaded successfully" }
        }.onFailure { err ->
            when( err ) {
                is IllegalArgumentException,
                is SecurityException,
                is IOException,
                is OutOfMemoryError -> logger.e("Failed to compress image", err)

                else -> logger.e( err ) { "Error occurs while uploading local artwork" }
            }
        }

    @OptIn(ExperimentalContracts::class)
    private suspend fun getImageUrl( artworkUri: String? ): String? {
        contract {
            returns( null ) implies( artworkUri == null )
        }
        if( artworkUri.isNullOrBlank() )
            return smallImage

        logger.v { "Getting external url for artwork $artworkUri" }

        val artworkCacheKey = artworkUri
        if( cachedExternalUrls.containsKey( artworkCacheKey ) ) {
            logger.d { "artwork is cached" }
            return cachedExternalUrls[artworkCacheKey]
        }

        val kmpUri = artworkUri.toKmpUri()
        val artworkUri =
            if( kmpUri.isLocalFile() )
                uploadLocalArtwork( kmpUri ).getOrNull().toString()
            else
                artworkUri

        return DiscordApi.getExternalImageUrl( artworkUri, _token.value!! )
                         .onSuccess { cachedExternalUrls[artworkCacheKey] = it }
                         .getOrDefault( smallImage )
    }

    /**
     * This function shouldn't be called anywhere other than initialization of [smallImage]
     */
    private fun getAppLogoUrl(): String? = runBlocking {
        DiscordApi.getExternalImageUrl( KREATE_IMAGE_URL, _token.value!! )
                  .getOrNull()
    }
    //</editor-fold>

    private fun onTokenChanged() = scope.launch {
        _token.collectLatest { token ->
            logout()

            logger.v { "Starting new session..." }
            if( token.isNullOrBlank() ) {
                logger.e { "Cannot start session with null or empty token" }
                return@collectLatest
            }

            try {
                val session = lock.withLock {
                    DiscordWebSocketImpl(token, DiscordLogger)
                        .also( _session::store )
                }

                session.connect()
            } catch( e: Exception ) {
                logger.e( e ) { "Session closed unexpectedly!" }
            }
        }
    }

    private suspend fun makeAssets( largeImage: String?, smallImage: String? ): Assets {
        val largeImage = getImageUrl( largeImage )
        val smallImage = if( largeImage == this.smallImage && smallImage == null )
            null
        else
            getImageUrl( smallImage )

        return Assets(largeImage, smallImage)
    }

    override fun login( token: String ) {
        val isSimilarToken = _token.value == token
        if( isSimilarToken && _isActive.load() ) {
            logger.w { "Not log in with the same token." }
            return
        }

        _token.value = token
    }

    override suspend fun logout(): Boolean {
        logger.v { "Closing connection to Discord" }

        try {
            // Obtaining the lock here does 2 main things:
            // - Prevent new update from being sent to Discord
            // - Wait for all update to finish before disconnecting
            val existingConnection = lock.withLock {
                _session.fetchAndUpdate { null }
                        ?.also(DiscordWebSocket::close )
            }

            previousPresence = null
            state = State.BROWSING

            return existingConnection != null
        } catch( e: Exception ) {
            logger.e( e ) { "Failed to close connection" }

            return false
        }
    }

    override suspend fun listening( song: ListeningActivity ) {
        try {
            lock.withLock {
                /**
                 * At first, we only check if websocket is established.
                 * This is quick and reliable enough for the program
                 * to start setting up other parts such as uploading artwork,
                 * making [Activity], etc. These don't need websocket connection to work.
                 */
                val session = _session.load() ?: throw SessionNotAvailableException()
                val assets = makeAssets( song.thumbnailUrl, song.artistThumbnailUrl )
                val activity = Activity(
                    name = "Kreate",
                    state = song.artistName,
                    details = song.songName,
                    type = Type.LISTENING,
                    timestamps = Timestamps(song.timeStart + song.duration, song.timeStart),
                    assets = assets,
                    applicationId = APPLICATION_ID,
                    url = "https://github.com/knighthat/Kreate"
                )
                val presence = Presence(listOf(activity), false)

                // Update listening state can be triggered due to many factors:
                // changing song, seeking to new position, skipping, etc.
                // So we only check whether the request is duplicated.
                if( presence == previousPresence ) {
                    logger.w { "Duplicate listening activity detected. Skipping..." }
                    return@withLock
                }

                // Now, before sending this request away, we must validate session.
                if( session.isWebSocketConnected() ) {
                    session.sendActivity( presence )
                    state = State.PLAYING       // Only update state if activity sent successfully
                    previousPresence = presence
                } else
                    throw SessionNotAvailableException()
            }
        } catch ( e: Exception ) {
            if( e is SessionNotAvailableException )
                logger.w { "Session not available!" }
            else
                logger.e( e ) { "Send listening activity failed!" }
        }
    }

    override suspend fun pause( song: ListeningActivity ) {
        try {
            lock.withLock {
                /**
                 * At first, we only check if websocket is established.
                 * This is quick and reliable enough for the program
                 * to start setting up other parts such as uploading artwork,
                 * making [Activity], etc. These don't need websocket connection to work.
                 */
                val session = _session.load() ?: throw SessionNotAvailableException()
                val assets = makeAssets( song.thumbnailUrl, song.artistThumbnailUrl )
                val activity = Activity(
                    name = "Kreate",
                    state = "Pausing",
                    details = song.songName,
                    type = Type.LISTENING,
                    timestamps = Timestamps(null, song.timeStart),
                    assets = assets,
                    applicationId = APPLICATION_ID,
                    url = "https://github.com/knighthat/Kreate"
                )
                val presence = Presence(listOf(activity), true, System.currentTimeMillis())

                // For pausing, only enact if it's not previously
                val previousState = previousPresence?.activities?.firstOrNull()?.state
                if( previousState == "Pausing" ) {
                    logger.w { "Duplicate pausing activity detected. Skipping..." }
                    return@withLock
                }

                // Now, before sending this request away, we must validate session.
                if( session.isWebSocketConnected() ) {
                    session.sendActivity( presence )
                    state = State.PAUSING       // Only update state if activity sent successfully
                    previousPresence = presence
                } else
                    throw SessionNotAvailableException()
            }
        } catch( e: Exception ) {
            if( e is SessionNotAvailableException )
                logger.w { "Session not available!" }
            else
                logger.e( e ) { "Send pause activity failed!" }
        }
    }

    override suspend fun reset() {
        try {
            lock.withLock {
                /**
                 * At first, we only check if websocket is established.
                 * This is quick and reliable enough for the program
                 * to start setting up other parts such as uploading artwork,
                 * making [Activity], etc. These don't need websocket connection to work.
                 */
                val session = _session.load() ?: throw SessionNotAvailableException()
                val assets = Assets(
                    largeImage = smallImage,
                    smallImage = null
                )
                val now = System.currentTimeMillis()
                val activity = Activity(
                    name = "Kreate",
                    details = "Music your way",
                    state = "Browsing",
                    type = Type.LISTENING,
                    timestamps = Timestamps(null, now),
                    assets = assets,
                    applicationId = APPLICATION_ID
                )
                val presence = Presence(listOf(activity), true, now)

                // Similarly to pausing, resetting presence requires
                // user not in the state already.
                val previousState = previousPresence?.activities?.firstOrNull()?.state
                if( previousState == "Browsing") {
                    logger.w { "Duplicate browsing activity detected. Skipping..." }
                    return@withLock
                }

                // Now, before sending this request away, we must validate session.
                if( session.isWebSocketConnected() ) {
                    session.sendActivity( presence )
                    state = State.BROWSING       // Only update state if activity sent successfully
                    previousPresence = presence
                } else
                    throw SessionNotAvailableException()
            }
        } catch( e: Exception ) {
            if( e is SessionNotAvailableException )
                logger.w { "Session not available!" }
            else
                logger.e( e ) { "Reset activity failed!" }
        }
    }

    enum class State {

        BROWSING, PAUSING, PLAYING;
    }
}