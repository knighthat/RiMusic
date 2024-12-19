package me.knighthat.updater

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.*
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.CheckUpdateState
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.screens.settings.EnumValueSelectorSettingsEntry
import it.fast4x.rimusic.ui.screens.settings.SettingsDescription
import it.fast4x.rimusic.utils.checkUpdateStateKey
import it.fast4x.rimusic.utils.rememberPreference
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

    fun checkForUpdate() {
        if( !::upstreamBuildDate.isInitialized )
            runBlocking( Dispatchers.IO ) { fetchUpdate() }

        val localBuildDate = BuildConfig.BUILD_DATE.toUInt()
        val canBeUpdated = localBuildDate < upstreamBuildDate.first

        NewUpdateAvailableDialog.isActive = canBeUpdated
    }

    @Composable
    fun SettingEntry() {
        var checkUpdateState by rememberPreference( checkUpdateStateKey, CheckUpdateState.Disabled )

        Row( Modifier.fillMaxWidth() ) {
            EnumValueSelectorSettingsEntry(
                title = stringResource( R.string.enable_check_for_update ),
                selectedValue = checkUpdateState,
                onValueSelected = { checkUpdateState = it },
                valueText = { it.text },
                modifier = Modifier.weight( 1f )
            )

            // Slide in from right + fade in effect. Slide out from left + fade out effect
            AnimatedVisibility(
                visible = checkUpdateState != CheckUpdateState.Disabled,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(initialAlpha = 0f),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(targetAlpha = 0f)
            ) {
                SecondaryTextButton(
                    text = stringResource( R.string.info_check_update_now ),
                    onClick = {
                        runBlocking( Dispatchers.IO ) { fetchUpdate() }       // Force fetch new update
                        checkForUpdate()
                    },
                    modifier = Modifier.padding( end = 24.dp )
                )
            }
        }

        SettingsDescription( text = stringResource(R.string.when_enabled_a_new_version_is_checked_and_notified_during_startup) )
    }
}