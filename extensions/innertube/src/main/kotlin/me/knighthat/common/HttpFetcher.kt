package me.knighthat.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.brotli
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import it.fast4x.innertube.utils.ProxyPreferences
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.net.InetSocketAddress
import java.net.Proxy

internal object HttpFetcher {

    /**
     * Latest user agent string from [UserAgents.Me](https://useragents.me)
     *
     * TODO: Add a Github Action that can update this automatically
     */
    private const val LATEST_CHROME_ANDROID_UA =
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.3"

    val CLIENT = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 3000  // Total request timeout
            connectTimeoutMillis = 2000  // Connection timeout
            socketTimeoutMillis = 2000   // Socket timeout
        }
        install(ContentNegotiation) {
            @OptIn(ExperimentalSerializationApi::class)
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }
        install(ContentEncoding) {
            brotli(1.0F)
            gzip(0.9F)
            deflate(0.8F)
        }
        ProxyPreferences.preference?.let {
            engine {
                proxy = Proxy(
                    it.proxyMode,
                    InetSocketAddress( it.proxyHost, it.proxyPort )
                )
            }
        }
        defaultRequest {
            headers.append( HttpHeaders.Accept, "*/*" )
            headers.append( HttpHeaders.UserAgent, LATEST_CHROME_ANDROID_UA )
        }
    }
}