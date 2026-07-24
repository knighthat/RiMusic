package app.kreate.internal.discord

import app.kreate.exceptions.SessionNotAvailableException
import app.kreate.gateway.ImageHostingService
import app.kreate.gateway.discord.DiscordApi
import app.kreate.gateway.discord.DiscordRpc
import app.kreate.gateway.discord.ListeningActivity
import app.kreate.gateway.discord.Type
import app.kreate.logging.DiscordLogger
import app.kreate.utils.ImageProcessor
import app.kreate.utils.guessMimetype
import app.kreate.utils.isLocalFile
import app.kreate.utils.readByteArray
import co.touchlab.kermit.Logger
import com.eygraber.uri.Uri
import com.eygraber.uri.toKmpUri
import kizzy.gateway.DiscordWebSocket
import kizzy.gateway.DiscordWebSocketImpl
import kizzy.gateway.entities.presence.Activity
import kizzy.gateway.entities.presence.Assets
import kizzy.gateway.entities.presence.Presence
import kizzy.gateway.entities.presence.Timestamps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndUpdate
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@OptIn(ExperimentalAtomicApi::class)
internal class DiscordRpcImpl(scope: CoroutineScope) : DiscordRpc {

    companion object {

        private const val MAX_DIMENSION = 1024                              // Per Discord's guidelines
        private const val MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024     // 2 MB in bytes
        private const val KREATE_IMAGE_URL = "https://i.ibb.co/v4CzX3kT/discord-rpc-kreate.jpg"

        private val cachedExternalUrls = ConcurrentHashMap<String, String>()
    }

    private val logger = Logger.withTag( "DiscordRpcImpl" )
    private val token = MutableStateFlow<String?>(null)
    private val session = AtomicReference<DiscordWebSocket?>(null)
    private val smallImage by lazy(::getAppLogoUrl)
    private val _previousPresence = AtomicReference<Presence?>(null)
    private val _state = AtomicReference<State>(State.BROWSING)

    /**
     * Thread-safe lock prevent multiple activities from being sent at the same time
     */
    private val lock = Mutex()

    init {
        token.onEach { token ->
            logout()

            logger.v { "Starting new session..." }
            if( token.isNullOrBlank() ) {
                logger.e { "Cannot start session with null or empty token" }
                return@onEach
            }

            try {
                session.fetchAndUpdate { DiscordWebSocketImpl(token, DiscordLogger()) }
                    ?.connect()
            } catch( e: Exception ) {
                logger.e( e ) { "Session closed unexpectedly!" }
            }
        }.launchIn( scope )
    }

    private fun getToken() = requireNotNull( token.value ) { "Token is not set" }

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

            val mimetype = requireNotNull( uploadableUri.guessMimetype() ) {
                "Failed to read mimetype of \"$uploadableUri\""
            }
            val fileData = requireNotNull( uploadableUri.readByteArray() ) {
                "Failed to read data from \"$uploadableUri\""
            }
            ImageHostingService.uploadToLitterBox( mimetype, fileData )
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

        return DiscordApi.getExternalImageUrl( artworkUri, getToken() )
                         .onSuccess { cachedExternalUrls[artworkCacheKey] = it }
                         .getOrDefault( smallImage )
    }

    /**
     * This function shouldn't be called anywhere other than initialization of [smallImage]
     */
    private fun getAppLogoUrl(): String? = runBlocking {
        DiscordApi.getExternalImageUrl( KREATE_IMAGE_URL, getToken() )
            .getOrNull()
    }
    //</editor-fold>

    private suspend fun makeAssets( largeImage: String?, smallImage: String? ): Assets {
        val largeImage = getImageUrl( largeImage )
        val smallImage = if( largeImage == this.smallImage && smallImage == null )
            null
        else
            getImageUrl( smallImage )

        return Assets(largeImage, smallImage)
    }

    override fun login( token: String ) {
        if( getToken() == token && session.load()?.isActive == true ) {
            logger.w { "Not log in with the same token." }
            return
        }

        this.token.update { token }
    }

    override suspend fun logout(): Boolean {
        logger.v { "Closing connection to Discord" }

        try {
            val existingConnection = session.exchange( null )
            existingConnection?.close()

            _previousPresence.store( null )
            _state.store( State.BROWSING )

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
                val session = session.load() ?: throw SessionNotAvailableException()
                val assets = makeAssets( song.thumbnailUrl, song.artistThumbnailUrl )
                val activity = Activity(
                    name = "Kreate",
                    state = song.artistName,
                    details = song.songName,
                    type = Type.LISTENING,
                    timestamps = Timestamps(song.timeStart + song.duration, song.timeStart),
                    assets = assets,
                    applicationId = DiscordApi.APPLICATION_ID,
                    url = "https://github.com/knighthat/Kreate"
                )
                val presence = Presence(listOf(activity), false)

                // Update listening state can be triggered due to many factors:
                // changing song, seeking to new position, skipping, etc.
                // So we only check whether the request is duplicated.
                if( presence == _previousPresence.load() ) {
                    logger.w { "Duplicate listening activity detected. Skipping..." }
                    return@withLock
                }

                // Now, before sending this request away, we must validate session.
                if( session.isWebSocketConnected() ) {
                    session.sendActivity( presence )
                    _state.store( State.PLAYING )        // Only update state if activity sent successfully
                    _previousPresence.store( presence )
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
                val session = session.load() ?: throw SessionNotAvailableException()
                val assets = makeAssets( song.thumbnailUrl, song.artistThumbnailUrl )
                val activity = Activity(
                    name = "Kreate",
                    state = "Pausing",
                    details = song.songName,
                    type = Type.LISTENING,
                    timestamps = Timestamps(null, song.timeStart),
                    assets = assets,
                    applicationId = DiscordApi.APPLICATION_ID,
                    url = "https://github.com/knighthat/Kreate"
                )
                val presence = Presence(listOf(activity), true, System.currentTimeMillis())

                // For pausing, only enact if it's not previously
                val previousState = _previousPresence.load()?.activities?.firstOrNull()?.state
                if( previousState == "Pausing" ) {
                    logger.w { "Duplicate pausing activity detected. Skipping..." }
                    return@withLock
                }

                // Now, before sending this request away, we must validate session.
                if( session.isWebSocketConnected() ) {
                    session.sendActivity( presence )
                    _state.store( State.PAUSING )       // Only update state if activity sent successfully
                    _previousPresence.store( presence )
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
                val session = session.load() ?: throw SessionNotAvailableException()
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
                    applicationId = DiscordApi.APPLICATION_ID
                )
                val presence = Presence(listOf(activity), true, now)

                // Similarly to pausing, resetting presence requires
                // user not in the state already.
                val previousState = _previousPresence.load()?.activities?.firstOrNull()?.state
                if( previousState == "Browsing") {
                    logger.w { "Duplicate browsing activity detected. Skipping..." }
                    return@withLock
                }

                // Now, before sending this request away, we must validate session.
                if( session.isWebSocketConnected() ) {
                    session.sendActivity( presence )
                    _state.store( State.BROWSING )      // Only update state if activity sent successfully
                    _previousPresence.store( presence )
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