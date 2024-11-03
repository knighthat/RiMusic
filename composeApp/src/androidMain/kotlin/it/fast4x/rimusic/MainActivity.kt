package it.fast4x.rimusic

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.WindowManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.kieronquinn.monetcompat.core.MonetActivityAccessException
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.interfaces.MonetColorsChangedListener
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import dev.kdrag0n.monet.theme.ColorScheme
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.bodies.BrowseBody
import it.fast4x.innertube.requests.playlistPage
import it.fast4x.innertube.requests.song
import it.fast4x.innertube.utils.LocalePreferenceItem
import it.fast4x.innertube.utils.LocalePreferences
import it.fast4x.innertube.utils.ProxyPreferenceItem
import it.fast4x.innertube.utils.ProxyPreferences
import it.fast4x.innertube.utils.YoutubePreferenceItem
import it.fast4x.innertube.utils.YoutubePreferences
import it.fast4x.rimusic.enums.AudioQualityFormat
import it.fast4x.rimusic.enums.CheckUpdateState
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.FontType
import it.fast4x.rimusic.enums.HomeScreenTabs
import it.fast4x.rimusic.enums.Languages
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.PlayerService
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.screens.AppNavigation
import it.fast4x.rimusic.ui.screens.player.MiniPlayer
import it.fast4x.rimusic.ui.screens.player.Player
import it.fast4x.rimusic.ui.screens.player.components.YoutubePlayer
import it.fast4x.rimusic.ui.screens.player.rememberPlayerSheetState
import it.fast4x.rimusic.ui.styling.Appearance
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.colorPaletteOf
import it.fast4x.rimusic.ui.styling.customColorPalette
import it.fast4x.rimusic.ui.styling.dynamicColorPaletteOf
import it.fast4x.rimusic.ui.styling.typographyOf
import it.fast4x.rimusic.utils.InitDownloader
import it.fast4x.rimusic.utils.LocalMonetCompat
import it.fast4x.rimusic.utils.OkHttpRequest
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.applyFontPaddingKey
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.audioQualityFormatKey
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.checkUpdateStateKey
import it.fast4x.rimusic.utils.closeWithBackButtonKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.customThemeDark_Background0Key
import it.fast4x.rimusic.utils.customThemeDark_Background1Key
import it.fast4x.rimusic.utils.customThemeDark_Background2Key
import it.fast4x.rimusic.utils.customThemeDark_Background3Key
import it.fast4x.rimusic.utils.customThemeDark_Background4Key
import it.fast4x.rimusic.utils.customThemeDark_TextKey
import it.fast4x.rimusic.utils.customThemeDark_accentKey
import it.fast4x.rimusic.utils.customThemeDark_iconButtonPlayerKey
import it.fast4x.rimusic.utils.customThemeDark_textDisabledKey
import it.fast4x.rimusic.utils.customThemeDark_textSecondaryKey
import it.fast4x.rimusic.utils.customThemeLight_Background0Key
import it.fast4x.rimusic.utils.customThemeLight_Background1Key
import it.fast4x.rimusic.utils.customThemeLight_Background2Key
import it.fast4x.rimusic.utils.customThemeLight_Background3Key
import it.fast4x.rimusic.utils.customThemeLight_Background4Key
import it.fast4x.rimusic.utils.customThemeLight_TextKey
import it.fast4x.rimusic.utils.customThemeLight_accentKey
import it.fast4x.rimusic.utils.customThemeLight_iconButtonPlayerKey
import it.fast4x.rimusic.utils.customThemeLight_textDisabledKey
import it.fast4x.rimusic.utils.customThemeLight_textSecondaryKey
import it.fast4x.rimusic.utils.disableClosingPlayerSwipingDownKey
import it.fast4x.rimusic.utils.disablePlayerHorizontalSwipeKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.encryptedPreferences
import it.fast4x.rimusic.utils.fontTypeKey
import it.fast4x.rimusic.utils.forcePlay
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.intent
import it.fast4x.rimusic.utils.invokeOnReady
import it.fast4x.rimusic.utils.isAtLeastAndroid6
import it.fast4x.rimusic.utils.isAtLeastAndroid8
import it.fast4x.rimusic.utils.isKeepScreenOnEnabledKey
import it.fast4x.rimusic.utils.isProxyEnabledKey
import it.fast4x.rimusic.utils.isValidIP
import it.fast4x.rimusic.utils.isVideo
import it.fast4x.rimusic.utils.keepPlayerMinimizedKey
import it.fast4x.rimusic.utils.languageAppKey
import it.fast4x.rimusic.utils.miniPlayerTypeKey
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.navigationBarTypeKey
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.playerVisualizerTypeKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.proxyHostnameKey
import it.fast4x.rimusic.utils.proxyModeKey
import it.fast4x.rimusic.utils.proxyPortKey
import it.fast4x.rimusic.utils.rememberEncryptedPreference
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.restartActivityKey
import it.fast4x.rimusic.utils.setDefaultPalette
import it.fast4x.rimusic.utils.shakeEventEnabledKey
import it.fast4x.rimusic.utils.showButtonPlayerVideoKey
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.showTotalTimeQueueKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.transitionEffectKey
import it.fast4x.rimusic.utils.useSystemFontKey
import it.fast4x.rimusic.utils.ytCookieKey
import it.fast4x.rimusic.utils.ytVisitorDataKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.knighthat.colorPalette
import me.knighthat.invidious.Invidious
import me.knighthat.piped.Piped
import me.knighthat.thumbnailShape
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import timber.log.Timber
import java.io.File
import java.net.Proxy
import java.net.UnknownHostException
import java.util.Locale
import java.util.Objects
import kotlin.math.sqrt


@UnstableApi
class MainActivity :
    //MonetCompatActivity(),
    AppCompatActivity(),
    MonetColorsChangedListener
    //,PersistMapOwner
{

    var downloadUtil = MyDownloadHelper

    var client = OkHttpClient()
    var request = OkHttpRequest(client)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.Binder) {
                this@MainActivity.binder = service
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }

    }

    private var binder by mutableStateOf<PlayerService.Binder?>(null)
    private var intentUriData by mutableStateOf<Uri?>(null)

    //override lateinit var persistMap: PersistMap

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var shakeCounter = 0
    private var appRunningInBackground: Boolean = false

    private var _monet: MonetCompat? by mutableStateOf(null)
    private val monet get() = _monet ?: throw MonetActivityAccessException()

    override fun onStart() {
        super.onStart()

        runCatching {
            bindService(intent<PlayerService>(), serviceConnection, Context.BIND_AUTO_CREATE)
        }.onFailure {
            Timber.e("MainActivity.onStart bindService ${it.stackTraceToString()}")
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalTextApi
    @UnstableApi
    @OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MonetCompat.enablePaletteCompat()

        //@Suppress("DEPRECATION", "UNCHECKED_CAST")
        //persistMap = lastCustomNonConfigurationInstance as? PersistMap ?: PersistMap()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        var splashScreenStays = true
        val delayTime = 800L

        installSplashScreen().setKeepOnScreenCondition { splashScreenStays }
        Handler(Looper.getMainLooper()).postDelayed({ splashScreenStays = false }, delayTime)

        MonetCompat.setup(this)
        _monet = MonetCompat.getInstance()
        monet.setDefaultPalette()
        monet.addMonetColorsChangedListener(
            listener = this,
            notifySelf = false
        )
        monet.updateMonetColors()

        monet.invokeOnReady {
            startApp()
        }

        if (preferences.getBoolean(shakeEventEnabledKey, false)) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            Objects.requireNonNull(sensorManager)
                ?.registerListener(
                    sensorListener,
                    sensorManager!!
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL
                )
        }

        checkIfAppIsRunningInBackground()

        // Fetch Piped & Invidious instances
        lifecycleScope.launch( Dispatchers.IO ) {
            try {
                Piped.fetchInstances()
                Invidious.fetchInstances()
            } catch ( _: UnknownHostException ) {
                // Probably because there's no internet connection
                // See report #4328
            }
        }
    }

    private fun checkIfAppIsRunningInBackground(){
        val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(runningAppProcessInfo)
        appRunningInBackground = runningAppProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND

    }

    @OptIn(ExperimentalTextApi::class,
        ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
        ExperimentalMaterial3Api::class
    )
    fun startApp() {

        if (!preferences.getBoolean(closeWithBackButtonKey, false))
            if (Build.VERSION.SDK_INT >= 33) {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT
                ) {
                    //Log.d("onBackPress", "yeah")
                }
            }

        /*
            Instead of checking getBoolean() individually, we can use .let() to express condition.
            Or, the whole thing is 'false' if null appears in the process.
         */
        val launchedFromNotification: Boolean =
            intent?.extras?.let {
                it.getBoolean("expandPlayerBottomSheet") || it.getBoolean("fromWidget")
            } ?: false

        println("MainActivity.onCreate launchedFromNotification: $launchedFromNotification intent $intent.action")

        intentUriData = intent.data ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri()

        with(preferences) {
            if (getBoolean(isKeepScreenOnEnabledKey, false)) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            if (getBoolean(isProxyEnabledKey, false)) {
                val hostName = getString(proxyHostnameKey, null)
                val proxyPort = getInt(proxyPortKey, 8080)
                val proxyMode = getEnum(proxyModeKey, Proxy.Type.HTTP)
                if (isValidIP(hostName)) {
                    hostName?.let { hName ->
                        ProxyPreferences.preference =
                            ProxyPreferenceItem(hName, proxyPort, proxyMode)
                    }
                } else {
                    SmartMessage("Your Proxy Hostname is invalid, please check it", PopupType.Warning, context = this@MainActivity)
                }
            }
            //if (getBoolean(isEnabledDiscoveryLangCodeKey, true))
        }


        setContent {

            if (preferences.getEnum(
                    checkUpdateStateKey,
                    CheckUpdateState.Ask
                ) == CheckUpdateState.Enabled
            ) {
                val urlVersionCode =
                    "https://raw.githubusercontent.com/fast4x/RiMusic/master/updatedVersion/updatedVersionCode.ver"
                //val urlVersionCode = "https://rimusic.xyz/update/updatedVersionCode.ver"
                request.GET(urlVersionCode, object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val responseData = response.body?.string()
                        runOnUiThread {
                            try {
                                if (responseData != null) {
                                    val file = File(filesDir, "RiMusicUpdatedVersionCode.ver")
                                    file.writeText(responseData.toString())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }

                    override fun onFailure(call: Call, e: java.io.IOException) {
                        Log.d("UpdatedVersionCode", "Check failure")
                    }
                })
            }

            val coroutineScope = rememberCoroutineScope()
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val navController = rememberNavController()
            var showPlayer by rememberSaveable { mutableStateOf(false) }
            var switchToAudioPlayer by rememberSaveable { mutableStateOf(false) }

            LocalePreferences.preference =
                LocalePreferenceItem(
                    hl = Locale.getDefault().toLanguageTag(),
                    //Locale.getDefault().country
                    gl = ""
                    //gl = "US" // US IMPORTANT
                )

            var visitorData by rememberEncryptedPreference(key = ytVisitorDataKey, defaultValue = Innertube.DEFAULT_VISITOR_DATA)

            if (visitorData.isEmpty())  runBlocking {
                Innertube.visitorData().getOrNull()?.also {
                    visitorData = it
                }
            }

            YoutubePreferences.preference =
                YoutubePreferenceItem(
                    cookie = encryptedPreferences.getString(ytCookieKey, ""),
                    visitordata = visitorData
                )

            preferences.getEnum(audioQualityFormatKey, AudioQualityFormat.Auto)

            var appearance by rememberSaveable(
                isSystemInDarkTheme,
                stateSaver = Appearance.Companion
            ) {
                with(preferences) {
                    val colorPaletteName =
                        getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
                    val colorPaletteMode = getEnum(colorPaletteModeKey, ColorPaletteMode.Dark)
                    val thumbnailRoundness =
                        getEnum(thumbnailRoundnessKey, ThumbnailRoundness.Heavy)
                    val useSystemFont = getBoolean(useSystemFontKey, false)
                    val applyFontPadding = getBoolean(applyFontPaddingKey, false)

                    var colorPalette =
                        colorPaletteOf(colorPaletteName, colorPaletteMode, isSystemInDarkTheme)

                    val fontType = getEnum(fontTypeKey, FontType.Rubik)

                    if (colorPaletteName == ColorPaletteName.MaterialYou) {
                        colorPalette = dynamicColorPaletteOf(
                            Color(monet.getAccentColor(this@MainActivity)),
                            colorPaletteMode == ColorPaletteMode.Dark || (colorPaletteMode == ColorPaletteMode.System && isSystemInDarkTheme),
                            colorPaletteMode == ColorPaletteMode.PitchBlack
                        )
                    }

                    setSystemBarAppearance(colorPalette.isDark)

                    mutableStateOf(
                        Appearance(
                            colorPalette = colorPalette,
                            typography = typographyOf(
                                colorPalette.text,
                                useSystemFont,
                                applyFontPadding,
                                fontType
                            ),
                            thumbnailShape = thumbnailRoundness.shape()
                        )
                    )
                }



            }



            DisposableEffect(binder, isSystemInDarkTheme) {
                var bitmapListenerJob: Job? = null

                fun setDynamicPalette(colorPaletteMode: ColorPaletteMode) {
                    val isDark =
                        colorPaletteMode == ColorPaletteMode.Dark || (colorPaletteMode == ColorPaletteMode.System && isSystemInDarkTheme)
                    val isPicthBlack = colorPaletteMode == ColorPaletteMode.PitchBlack

                    binder?.setBitmapListener { bitmap: Bitmap? ->
                        if (bitmap == null) {
                            val colorPalette =
                                colorPaletteOf(
                                    ColorPaletteName.Dynamic,
                                    colorPaletteMode,
                                    isSystemInDarkTheme
                                )

                            setSystemBarAppearance(colorPalette.isDark)

                            appearance = appearance.copy(
                                colorPalette = colorPalette,
                                typography = appearance.typography.copy(colorPalette.text)
                            )

                            return@setBitmapListener
                        }

                        bitmapListenerJob = coroutineScope.launch(Dispatchers.IO) {
                            dynamicColorPaletteOf(bitmap, isDark, isPicthBlack)?.let {
                                withContext(Dispatchers.Main) {
                                    setSystemBarAppearance(it.isDark)
                                }
                                appearance = appearance.copy(
                                    colorPalette = it,
                                    typography = appearance.typography.copy(it.text)
                                )
                            }
                        }
                    }
                }

                val listener =
                    SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                        when (key) {

                            languageAppKey -> {
                                val lang = sharedPreferences.getEnum(
                                    languageAppKey,
                                    Languages.English
                                )

                                //val precLangCode = LocaleListCompat.getDefault().get(0).toString()
                                val systemLangCode =
                                    AppCompatDelegate.getApplicationLocales().get(0).toString()
                                //Log.d("LanguageActivity", "lang.code ${lang.code} precLangCode $precLangCode systemLangCode $systemLangCode")

                                val sysLocale: LocaleListCompat =
                                    LocaleListCompat.forLanguageTags(systemLangCode)
                                val appLocale: LocaleListCompat =
                                    LocaleListCompat.forLanguageTags(lang.code)
                                AppCompatDelegate.setApplicationLocales(if (lang.code == "") sysLocale else appLocale)
                            }

                            effectRotationKey, playerThumbnailSizeKey,
                            playerVisualizerTypeKey,
                            UiTypeKey,
                            disablePlayerHorizontalSwipeKey,
                            disableClosingPlayerSwipingDownKey,
                            showSearchTabKey,
                            navigationBarPositionKey,
                            navigationBarTypeKey,
                            showTotalTimeQueueKey,
                            backgroundProgressKey,
                            transitionEffectKey,
                            playerBackgroundColorsKey,
                            miniPlayerTypeKey,
                            thumbnailRoundnessKey,
                            restartActivityKey
                            -> {
                                this@MainActivity.recreate()
                            }

                            colorPaletteNameKey, colorPaletteModeKey,
                            customThemeLight_Background0Key,
                            customThemeLight_Background1Key,
                            customThemeLight_Background2Key,
                            customThemeLight_Background3Key,
                            customThemeLight_Background4Key,
                            customThemeLight_TextKey,
                            customThemeLight_textSecondaryKey,
                            customThemeLight_textDisabledKey,
                            customThemeLight_iconButtonPlayerKey,
                            customThemeLight_accentKey,
                            customThemeDark_Background0Key,
                            customThemeDark_Background1Key,
                            customThemeDark_Background2Key,
                            customThemeDark_Background3Key,
                            customThemeDark_Background4Key,
                            customThemeDark_TextKey,
                            customThemeDark_textSecondaryKey,
                            customThemeDark_textDisabledKey,
                            customThemeDark_iconButtonPlayerKey,
                            customThemeDark_accentKey,
                            -> {
                                val colorPaletteName =
                                    sharedPreferences.getEnum(
                                        colorPaletteNameKey,
                                        ColorPaletteName.Dynamic
                                    )

                                val colorPaletteMode =
                                    sharedPreferences.getEnum(
                                        colorPaletteModeKey,
                                        ColorPaletteMode.System
                                    )

                                if (colorPaletteName == ColorPaletteName.Dynamic) {
                                    setDynamicPalette(colorPaletteMode)
                                } else {
                                    bitmapListenerJob?.cancel()
                                    binder?.setBitmapListener(null)

                                    var colorPalette = colorPaletteOf(
                                        colorPaletteName,
                                        colorPaletteMode,
                                        isSystemInDarkTheme
                                    )

                                    if (colorPaletteName == ColorPaletteName.MaterialYou) {
                                        colorPalette = dynamicColorPaletteOf(
                                            Color(monet.getAccentColor(this@MainActivity)),
                                            colorPaletteMode == ColorPaletteMode.Dark || (colorPaletteMode == ColorPaletteMode.System && isSystemInDarkTheme),
                                            colorPaletteMode == ColorPaletteMode.PitchBlack
                                        )
                                    }

                                    if (colorPaletteName == ColorPaletteName.Customized) {
                                        colorPalette = customColorPalette(colorPalette, this@MainActivity, isSystemInDarkTheme)
                                    }

                                    setSystemBarAppearance(colorPalette.isDark)

                                    appearance = appearance.copy(
                                        colorPalette = colorPalette,
                                        typography = appearance.typography.copy(colorPalette.text),
                                    )
                                }
                            }

                            thumbnailRoundnessKey -> {
                                val thumbnailRoundness =
                                    sharedPreferences.getEnum(key, ThumbnailRoundness.Heavy)

                                appearance = appearance.copy(
                                    thumbnailShape = thumbnailRoundness.shape()
                                )
                            }

                            useSystemFontKey, applyFontPaddingKey, fontTypeKey -> {
                                val useSystemFont =
                                    sharedPreferences.getBoolean(useSystemFontKey, false)
                                val applyFontPadding =
                                    sharedPreferences.getBoolean(applyFontPaddingKey, false)
                                val fontType =
                                    sharedPreferences.getEnum(fontTypeKey, FontType.Rubik)

                                appearance = appearance.copy(
                                    typography = typographyOf(
                                        appearance.colorPalette.text,
                                        useSystemFont,
                                        applyFontPadding,
                                        fontType
                                    ),
                                )
                            }
                        }
                    }

                with(preferences) {
                    registerOnSharedPreferenceChangeListener(listener)

                    val colorPaletteName =
                        getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
                    if (colorPaletteName == ColorPaletteName.Dynamic) {
                        setDynamicPalette(getEnum(colorPaletteModeKey, ColorPaletteMode.Dark))
                    }

                    onDispose {
                        bitmapListenerJob?.cancel()
                        binder?.setBitmapListener(null)
                        unregisterOnSharedPreferenceChangeListener(listener)
                    }
                }
            }

            val rippleConfiguration =
                remember(appearance.colorPalette.text, appearance.colorPalette.isDark) {
                        RippleConfiguration(color = appearance.colorPalette.text)
                }

            val shimmerTheme = remember {
                defaultShimmerTheme.copy(
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            easing = LinearEasing,
                            delayMillis = 250,
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    shaderColors = listOf(
                        Color.Unspecified.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.50f),
                        Color.Unspecified.copy(alpha = 0.25f),
                    ),
                )
            }

            LaunchedEffect(Unit) {
                val colorPaletteName =
                    preferences.getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
                if (colorPaletteName == ColorPaletteName.Customized) {
                    appearance = appearance.copy(
                        colorPalette = customColorPalette(appearance.colorPalette, this@MainActivity, isSystemInDarkTheme)
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appearance.colorPalette.background0)
            ) {


                val density = LocalDensity.current
                val windowsInsets = WindowInsets.systemBars
                val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }

                val playerSheetState = rememberPlayerSheetState(
                    dismissedBound = 0.dp,
                    collapsedBound = Dimensions.collapsedPlayer + bottomDp,
                    expandedBound = maxHeight,
                )

                val playerState =
                    rememberModalBottomSheetState(skipPartiallyExpanded = true)

                val playerAwareWindowInsets by remember(
                    bottomDp,
                    playerSheetState.value
                ) {
                    derivedStateOf {
                        val bottom = playerSheetState.value.coerceIn(
                            bottomDp,
                            playerSheetState.collapsedBound
                        )

                        windowsInsets
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .add(WindowInsets(bottom = bottom))
                    }
                }

                var openTabFromShortcut = remember { -1 }
                if (intent.action in arrayOf(action_songs, action_albums, action_library, action_search)) {
                    openTabFromShortcut =
                        when (intent?.action) {
                            action_songs -> HomeScreenTabs.Songs.index
                            action_albums -> HomeScreenTabs.Albums.index
                            action_library -> HomeScreenTabs.Playlists.index
                            action_search -> -2
                            else -> -1
                        }
                    intent.action = null
                }

                CompositionLocalProvider(
                    LocalAppearance provides appearance,
                    LocalIndication provides ripple(bounded = true),
                    LocalRippleConfiguration provides rippleConfiguration,
                    LocalShimmerTheme provides shimmerTheme,
                    LocalPlayerServiceBinder provides binder,
                    LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                    LocalDownloader provides downloadUtil,
                    LocalPlayerSheetState provides playerState,
                    LocalMonetCompat provides monet
                ) {

                    AppNavigation(
                        navController = navController,
                        miniPlayer = {
                            MiniPlayer(
                                showPlayer = { showPlayer = true },
                                hidePlayer = { showPlayer = false },
                                navController = navController
                            )
                        },
                        openTabFromShortcut = openTabFromShortcut
                    )

                    checkIfAppIsRunningInBackground()
                    if (appRunningInBackground) showPlayer = false


                    val thumbnailRoundness by rememberPreference(
                        thumbnailRoundnessKey,
                        ThumbnailRoundness.Heavy
                    )

                    val isVideo = binder?.player?.currentMediaItem?.isVideo ?: false
                    val isVideoEnabled = preferences.getBoolean(showButtonPlayerVideoKey, false)
                    val player: @Composable () -> Unit = {
                        Player(
                            navController = navController,
                            onDismiss = {
                                showPlayer = false
                            }
                        )
                    }

                    val youtubePlayer: @Composable () -> Unit = {
                        binder?.player?.currentMediaItem?.mediaId?.let {
                            YoutubePlayer(
                                ytVideoId = it,
                                lifecycleOwner = LocalLifecycleOwner.current,
                                onCurrentSecond = {},
                                showPlayer = showPlayer,
                                onSwitchToAudioPlayer = {
                                    showPlayer = false
                                    switchToAudioPlayer = true
                                }
                            )
                        }
                    }

                    CustomModalBottomSheet(
                        showSheet = switchToAudioPlayer || showPlayer,
                        onDismissRequest = {
                            showPlayer = false
                            switchToAudioPlayer = false
                        },
                        containerColor = colorPalette().background0,
                        contentColor = colorPalette().background0,
                        modifier = Modifier.fillMaxWidth(),
                        sheetState = playerState,
                        dragHandle = {
                            Surface(
                                modifier = Modifier.padding(vertical = 0.dp),
                                color = colorPalette().background0,
                                shape = thumbnailShape()
                            ) {}
                        },
                        shape = thumbnailRoundness.shape()
                    ) {
                         player()
                    }

                    CustomModalBottomSheet(
                        showSheet = isVideo && isVideoEnabled && showPlayer,
                        onDismissRequest = { showPlayer = false },
                        containerColor = colorPalette().background0,
                        contentColor = colorPalette().background0,
                        modifier = Modifier.fillMaxWidth(),
                        sheetState = playerState,
                        dragHandle = {
                            Surface(
                                modifier = Modifier.padding(vertical = 0.dp),
                                color = colorPalette().background0,
                                shape = thumbnailShape()
                            ) {}
                        },
                        shape = thumbnailRoundness.shape()
                    ) {
                        youtubePlayer()
                    }

                    /*
                    BottomSheetMenu(
                        state = LocalMenuState.current,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )
                     */

                    val menuState = LocalMenuState.current
                        CustomModalBottomSheet(
                            showSheet = menuState.isDisplayed,
                            onDismissRequest = menuState::hide,
                            containerColor = Color.Transparent,
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                            dragHandle = {
                                Surface(
                                    modifier = Modifier.padding(vertical = 0.dp),
                                    color = Color.Transparent,
                                    //shape = thumbnailShape
                                ) {}
                            },
                            shape = thumbnailRoundness.shape()
                        ) {
                            menuState.content()
                        }

                }

                DisposableEffect(binder?.player) {
                    val player = binder?.player ?: return@DisposableEffect onDispose { }

                    if (player.currentMediaItem == null) {
                        if (playerState.isVisible) {
                            showPlayer = false
                        }
                    } else {
                        if (launchedFromNotification) {
                            intent.replaceExtras(Bundle())
                            if (preferences.getBoolean(keepPlayerMinimizedKey, false))
                                showPlayer = false
                            else showPlayer = true
                        } else {
                            showPlayer = false
                        }
                    }

                    val listener = object : Player.Listener {
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null) {
                                if (mediaItem.mediaMetadata.extras?.getBoolean("isFromPersistentQueue") != true) {
                                    if (preferences.getBoolean(keepPlayerMinimizedKey, false))
                                        showPlayer = false
                                    else showPlayer = true
                                }
                            }
                        }
                    }

                    player.addListener(listener)

                    onDispose { player.removeListener(listener) }
                }

                InitDownloader()

            }

            LaunchedEffect(intentUriData) {
                val uri = intentUriData ?: return@LaunchedEffect

                SmartMessage( message ="${"RiMusic "}${getString(R.string.opening_url)}", durationLong = true, context = this@MainActivity)

                lifecycleScope.launch(Dispatchers.Main) {
                    when (val path = uri.pathSegments.firstOrNull()) {
                        "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                            val browseId = "VL$playlistId"

                            if (playlistId.startsWith("OLAK5uy_")) {
                                Innertube.playlistPage(BrowseBody(browseId = browseId))?.getOrNull()?.let {
                                    it.songsPage?.items?.firstOrNull()?.album?.endpoint?.browseId?.let { browseId ->
                                        navController.navigate(route = "${NavRoutes.album.name}/$browseId")

                                    }
                                }
                            } else {
                                navController.navigate(route = "${NavRoutes.playlist.name}/$browseId")
                            }
                        }

                        "channel", "c" -> uri.lastPathSegment?.let { channelId ->
                            try {
                                navController.navigate(route = "${NavRoutes.artist.name}/$channelId")
                            } catch (e:Exception) {
                                Timber.e("MainActivity.onCreate intentUriData ${e.stackTraceToString()}")
                            }
                        }

                        "search" -> uri.getQueryParameter("q")?.let { query ->
                                navController.navigate(route = "${NavRoutes.searchResults.name}/$query")
                        }

                        else -> when {
                            path == "watch" -> uri.getQueryParameter("v")
                            uri.host == "youtu.be" -> path
                            else -> null
                        }?.let { videoId ->
                            Innertube.song(videoId)?.getOrNull()?.let { song ->
                                val binder = snapshotFlow { binder }.filterNotNull().first()
                                withContext(Dispatchers.Main) {
                                    if (!song.explicit && !preferences.getBoolean(parentalControlEnabledKey, false))
                                        binder.player.forcePlay(song.asMediaItem)
                                    else
                                        SmartMessage("Parental control is enabled", PopupType.Warning, context = this@MainActivity)
                                }
                            }
                        }
                    }
                }
                intentUriData = null
            }

        }

        //throw RuntimeException("This is a simulated exception to crash");
    }


    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            if (preferences.getBoolean(shakeEventEnabledKey, false)) {
                // Fetching x,y,z values
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                lastAcceleration = currentAcceleration

                // Getting current accelerations
                // with the help of fetched x,y,z values
                currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta: Float = currentAcceleration - lastAcceleration
                acceleration = acceleration * 0.9f + delta

                // Display a Toast message if
                // acceleration value is over 12
                if (acceleration > 12) {
                    shakeCounter++
                    //Toast.makeText(applicationContext, "Shake event detected", Toast.LENGTH_SHORT).show()
                }
                if (shakeCounter >= 1) {
                    //Toast.makeText(applicationContext, "Shaked $shakeCounter times", Toast.LENGTH_SHORT).show()
                    shakeCounter = 0
                    binder?.player?.playNext()
                }

            }

        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        super.onResume()
        kotlin.runCatching {
            sensorManager?.registerListener(
                sensorListener, sensorManager!!.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER
                ), SensorManager.SENSOR_DELAY_NORMAL
            )
        }.onFailure {
            Timber.e("MainActivity.onResume registerListener sensorManager ${it.stackTraceToString()}")
        }
        appRunningInBackground = false
    }

    override fun onPause() {
        super.onPause()
        runCatching {
            sensorListener.let { sensorManager?.unregisterListener(it) }
                //sensorManager!!.unregisterListener(sensorListener)
        }.onFailure {
            Timber.e("MainActivity.onPause unregisterListener sensorListener ${it.stackTraceToString()}")
        }
        appRunningInBackground = true
    }

    @UnstableApi
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentUriData = intent.data ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri()

    }

    //@Deprecated("Deprecated in Java", ReplaceWith("persistMap"))
    //override fun onRetainCustomNonConfigurationInstance() = persistMap

    override fun onStop() {
        runCatching {
            unbindService(serviceConnection)
        }.onFailure {
            Timber.e("MainActivity.onStop unbindService ${it.stackTraceToString()}")
        }
        super.onStop()
    }

    @UnstableApi
    override fun onDestroy() {
        super.onDestroy()

        runCatching {
            monet.removeMonetColorsChangedListener(this)
            _monet = null
        }.onFailure {
            Timber.e("MainActivity.onDestroy removeMonetColorsChangedListener ${it.stackTraceToString()}")
        }

    }

    private fun setSystemBarAppearance(isDark: Boolean) {
        with(WindowCompat.getInsetsController(window, window.decorView.rootView)) {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }

        if (!isAtLeastAndroid6) {
            window.statusBarColor =
                (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }

        if (!isAtLeastAndroid8) {
            window.navigationBarColor =
                (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
    }

    companion object {
        const val action_search = "it.fast4x.rimusic.action.search"
        const val action_songs = "it.fast4x.rimusic.action.songs"
        const val action_albums = "it.fast4x.rimusic.action.albums"
        const val action_library = "it.fast4x.rimusic.action.library"
    }


    override fun onMonetColorsChanged(
        monet: MonetCompat,
        monetColors: ColorScheme,
        isInitialChange: Boolean
    ) {
        val colorPaletteName =
            preferences.getEnum(colorPaletteNameKey, ColorPaletteName.Dynamic)
        if (!isInitialChange && colorPaletteName == ColorPaletteName.MaterialYou) {
            /*
            monet.updateMonetColors()
            monet.invokeOnReady {
                startApp()
            }
             */
            this@MainActivity.recreate()
        }
    }


}

val LocalPlayerServiceBinder = staticCompositionLocalOf<PlayerService.Binder?> { null }

val LocalPlayerAwareWindowInsets = staticCompositionLocalOf<WindowInsets> { TODO() }

val LocalDownloader = staticCompositionLocalOf<MyDownloadHelper> { error("No Downloader provided") }

@OptIn(ExperimentalMaterial3Api::class)
val LocalPlayerSheetState = staticCompositionLocalOf<SheetState> { error("No player sheet state provided") }



