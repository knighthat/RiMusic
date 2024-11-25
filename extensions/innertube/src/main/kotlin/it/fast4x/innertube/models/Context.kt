package it.fast4x.innertube.models

import io.ktor.http.headers
import io.ktor.http.parameters
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.utils.LocalePreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null,
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val platform: String? = null,
        val hl: String? = "en",
        val gl: String? = "US",
        //val hl: String = Locale.getDefault().toLanguageTag(), //"en",
        //val hl: String = Innertube.localeHl,
        val visitorData: String = Innertube.DEFAULT_VISITOR_DATA,
        val androidSdkVersion: Int? = null,
        val userAgent: String? = null,
        val referer: String? = null,
        val deviceMake: String? = null,
        val deviceModel: String? = null,
        val osName: String? = null,
        val osVersion: String? = null,
        val acceptHeader: String? = null,
        val timeZone: String? = "UTC",
        val utcOffsetMinutes: Int? = 0,
        @Transient
        val api_key: String? = null
    )

    @Serializable
    data class ThirdParty(
        val embedUrl: String,
    )

    @Serializable
    data class User(
        val lockedSafetyMode: Boolean = false
    )

    fun apply() {
        client.userAgent

        headers {
            client.referer?.let { append("Referer", it) }
            append("X-Youtube-Bootstrap-Logged-In", "false")
            append("X-YouTube-Client-Name", client.clientName)
            append("X-YouTube-Client-Version", client.clientVersion)
            client.api_key?.let { append("X-Goog-Api-Key", it) }
        }

        parameters {
            client.api_key?.let { append("key", it) }
        }
    }

    companion object {

        private const val USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"
        private const val USER_AGENT_ANDROID = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
        private const val USER_AGENT_ANDROID_MUSIC = "com.google.android.youtube/19.29.1  (Linux; U; Android 11) gzip"
        private const val USER_AGENT_PLAYSTATION = "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
        private const val USER_AGENT_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
        private const val USER_AGENT_IOS = "com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X;)"

        private const val REFERER_YOUTUBE_MUSIC = "https://music.youtube.com/"
        private const val REFERER_YOUTUBE = "https://www.youtube.com/"

        val DefaultWeb = Context(
            client = Client(
                clientName = "WEB_REMIX",
                //clientVersion = "1.20220606.03.00",
                clientVersion = "1.20230731.00.00",
                platform = "DESKTOP",
                userAgent = USER_AGENT_WEB,
                referer = REFERER_YOUTUBE_MUSIC,
                visitorData = Innertube.visitorData,
                api_key = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8", // Youtube web
               // api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30" //Youtube web music
            )
        )


        //val hl = if (LocalePreferences.preference?.useLocale == true) LocalePreferences.preference!!.hl else ""
        val hl = LocalePreferences.preference?.hl
        //val gl = LocalePreferences.preference?.gl


        val DefaultWebWithLocale = DefaultWeb.copy(
            client = DefaultWeb.client.copy(hl = hl)
        )
        /*
        val DefaultWebWithLocale = Context(
            client = Client(
                clientName = "WEB_REMIX",
                //clientVersion = "1.20220606.03.00",
                clientVersion = "1.20230731.00.00",
                userAgent = USER_AGENT_WEB,
                referer = REFERER_YOUTUBE_MUSIC,
                hl = hl,
                visitorData = Innertube.visitorData,
                api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
            )
        )
         */


        val DefaultAndroid = Context(
            client = Client(
                clientName = "ANDROID_MUSIC",
                clientVersion = "6.33.52",
                //clientVersion = "5.28.1",
                //clientVersion = "5.22.1",
                androidSdkVersion = 30,
                platform = "MOBILE",
                userAgent = USER_AGENT_ANDROID_MUSIC,
                referer = REFERER_YOUTUBE_MUSIC,
                visitorData = Innertube.visitorData,
                api_key = "AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI"
            )
        )

        val DefaultIOS = Context(
            client = Client(
                clientName = "IOS",
                clientVersion = "19.29.1",
                deviceMake = "Apple",
                deviceModel = "iPhone16,2",
                osName = "iOS",
                osVersion = "17.5.1.21F90",
                acceptHeader = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                userAgent = USER_AGENT_IOS,
                api_key = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc"
            )
        )

        val DefaultRestrictionBypass = Context(
            client = Client(
                clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                clientVersion = "2.0",
                platform = "TV",
                userAgent = USER_AGENT_PLAYSTATION
            )
        )

    }
}
