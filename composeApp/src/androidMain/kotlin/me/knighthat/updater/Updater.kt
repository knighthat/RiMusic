package me.knighthat.updater

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.*
import it.fast4x.rimusic.R
import it.fast4x.rimusic.ui.components.tab.toolbar.Dialog
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.knighthat.util.Repository
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

object Updater {

    private val JSON = Json {
        ignoreUnknownKeys = true
    }

    private var updateRejected = false

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

        val tagUrl = "${Repository.GITHUB_API}/repos${Repository.API_RELEASE_PATH}"
        val request = Request.Builder().url( tagUrl ).build()
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
        val updateDialog = NewUpdateAvailableDialog.init { build }
        updateDialog.Render()

        if( !::upstreamBuildDate.isInitialized )
            runBlocking { fetchUpdate() }

        val localBuildDate = BuildConfig.BUILD_DATE.toUInt()
        val canBeUpdated = localBuildDate < upstreamBuildDate.first

        updateDialog.isActive = !updateRejected && canBeUpdated
    }

    private class NewUpdateAvailableDialog private constructor(
        private val activeState: MutableState<Boolean>,
        private val getBuild: () -> GithubRelease.Build,
    ): Dialog {

        companion object {
            @JvmStatic
            @Composable
            fun init( getBuild: () -> GithubRelease.Build ) =
                NewUpdateAvailableDialog(
                    rememberSaveable { mutableStateOf( false ) },
                    getBuild
                )
        }

        override val dialogTitle: String
            @Composable
            get() = stringResource( R.string.update_available )

        override var isActive: Boolean = activeState.value
            set(value) {
                activeState.value = value
                field = value
            }

        fun onDismiss() {
            isActive = false
            updateRejected = true
        }

        @Composable
        override fun Render() {
            if( !isActive ) return

            val uriHandler = LocalUriHandler.current

            @Composable
            fun DialogText(
                text: String,
                style: TextStyle,
                spacerHeight: Dp = 10.dp
            ) {
                BasicText(
                    text = text,
                    style = style,
                )
                Spacer( Modifier.height(spacerHeight) )
            }

            DefaultDialog( ::onDismiss ) {
                // Title
                DialogText(
                    text = dialogTitle,
                    style = typography().s.bold.copy( color = colorPalette().text )
                )

                // Update information
                val size = "(${getBuild().size} bytes)"
                DialogText(
                    text = stringResource( R.string.app_update_dialog_new, size ),
                    style = typography().xs.semiBold.copy( color = colorPalette().text )
                )

                // Available actions
                DialogText(
                    text = stringResource( R.string.actions_you_can_do ),
                    style = typography().xs.semiBold.copy( color = colorPalette().textSecondary )
                )

                // Option 1: Go to github page to download
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                ) {
                    BasicText(
                        text = stringResource( R.string.open_the_github_releases_web_page_and_download_latest_version ),
                        style = typography().xxs.semiBold.copy( color = colorPalette().textSecondary ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    Image(
                        painter = painterResource(R.drawable.globe),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette().shimmer),
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                onDismiss()

                                val tagUrl = "${Repository.GITHUB}${Repository.RELEASE_PATH}"
                                uriHandler.openUri( tagUrl )
                            }
                    )
                }
                Spacer( Modifier.height(10.dp) )

                // Option 2: Go straight to download page to start the download
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                ) {
                    BasicText(
                        text = stringResource(R.string.download_latest_version_from_github_you_will_find_the_file_in_the_notification_area_and_you_can_install_by_clicking_on_it),
                        style = typography().xxs.semiBold.copy(color = colorPalette().textSecondary),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    Image(
                        painter = painterResource(R.drawable.downloaded),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette().shimmer),
                        modifier = Modifier.size( 30.dp )
                                           .clickable {
                                               onDismiss()

                                               // https://github.com/knighthat/RiMusic/releases/latest/download/RiMusic-kbuild-full.apk
                                               val downloadUrl =
                                                   "${Repository.REPO_URL}/releases/latest/download/${getBuild().name}"
                                               uriHandler.openUri(downloadUrl)
                                           }
                    )
                }
                Spacer( Modifier.height(10.dp) )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                                       .clip( RoundedCornerShape(20) )
                                       .border( 2.dp, colorPalette().background2 )
                                       .clickable { onDismiss() }
                ) {
                    BasicText(
                        text = stringResource( R.string.cancel ),
                        style = typography().xs.medium.color( colorPalette().text ),
                        modifier = Modifier.padding( vertical = 16.dp )
                    )
                }
            }
        }
    }
}