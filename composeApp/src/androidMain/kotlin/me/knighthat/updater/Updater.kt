package me.knighthat.updater

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import it.fast4x.rimusic.BuildConfig
import it.fast4x.rimusic.ui.components.tab.toolbar.ConfirmDialog
import it.fast4x.rimusic.ui.components.themed.NewVersionDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

object Updater {

    const val TAG_PATH = "/knighthat/RiMusic/releases/tags/weekly-kbuild"
    const val REPO_API = "https://api.github.com/repos"
    const val GITHUB = "https://github.com"
    const val TAG_URL = "https://api.github.com/repos/knighthat/RiMusic/releases/tags/weekly-kbuild"

    private val JSON = Json {
        ignoreUnknownKeys = true
    }

    lateinit var build: GithubRelease.Build
    lateinit var upstreamBuildDate: Pair<UInt, Date>

    private fun parseDate( title: String ): Pair<UInt, Date> {
        val formattedDate = title.split( "|" ).last().trim()

        val dateFormatter = DateTimeFormatter.ofPattern( "yyyyMMdd" )
        val localDate = LocalDate.parse( formattedDate, dateFormatter )

        return formattedDate.toUInt() to Date.from(
                                                 localDate.atStartOfDay()
                                                          .atZone(
                                                              ZoneId.systemDefault()
                                                          ).toInstant()
                                             )
    }

    private fun extractBuild( assets: List<GithubRelease.Build> ): GithubRelease.Build {
        val versionSuffix = BuildConfig.VERSION_NAME.split( "-" ).last().trim()
        return assets.first {
            val fileName =
                if( versionSuffix.equals("kbf", true) )
                    "RiMusic-kbuild-full.apk"
                else if( versionSuffix.equals("kbm", true) )
                    "RiMusic-kbuild-minified.apk"
                else
                    throw IllegalArgumentException( "Unknown version suffix $versionSuffix" )

            it.name == fileName
        }
    }

    suspend fun fetchUpdate() = withContext( Dispatchers.IO ) {
        val client = OkHttpClient()

        val request = Request.Builder().url( TAG_URL ).build()
        val response = client.newCall( request ).execute()

        if( response.isSuccessful ) {
            val resBody = response.body?.string() ?: return@withContext

            val githubRelease = JSON.decodeFromString<GithubRelease>( resBody )

            build = extractBuild( githubRelease.builds )
            upstreamBuildDate = parseDate( githubRelease.name )
        }
    }

    @Composable
    fun CheckForUpdate() {
        val uriHandler = LocalUriHandler.current

        val updateDialog = object: ConfirmDialog {
            override fun onConfirm() {
                uriHandler.openUri( "$GITHUB$TAG_PATH")
            }

            override var isActive: Boolean by remember { mutableStateOf( false ) }
            override val dialogTitle: String
                @Composable
                get() = ""

            @Composable
            override fun Render() {
                if( !isActive ) return

                NewVersionDialog(
                    updatedProductName = "RiMusic-KBuild",
                    updatedVersionCode = BuildConfig.VERSION_CODE,
                    updatedVersionName = upstreamBuildDate.first.toString(),
                    onDismiss = ::onDismiss
                )
            }
        }
        updateDialog.Render()

        if( !::upstreamBuildDate.isInitialized )
            runBlocking { fetchUpdate() }

        val localBuildDate = BuildConfig.BUILD_DATE.toUInt()
        val canBeUpdated = localBuildDate < upstreamBuildDate.first

        updateDialog.isActive = canBeUpdated
    }
}