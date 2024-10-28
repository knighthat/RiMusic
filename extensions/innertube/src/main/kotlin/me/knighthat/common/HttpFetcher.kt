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

    /**
     * **Connect timeout** is the maximum time (in milliseconds)
     * to wait for the connection to be established between client
     * and server before giving up
     */
    internal const val CONNECT_TIMEOUT = 2000L

    /**
     * **Socket timeout** is the maximum time (in milliseconds)
     * the client will wait for the server to response
     * before giving up and throwing [java.net.SocketTimeoutException]
     */
    internal const val SOCKET_TIMEOUT = 2000L

    /**
     * **Request timeout** is the maximum time (in milliseconds)
     * the client is allowed to wait. Meaning, at any stage of
     * the connection, if the wait time exceeds this number,
     * the client will give up.
     *
     * This number should be larger than [CONNECT_TIMEOUT] or [SOCKET_TIMEOUT]
     * to prevent the client from closing the connection.
     * 2/3 of total time is sufficient for current use.
     */
    internal const val REQUEST_TIMEOUT = 2 * (CONNECT_TIMEOUT + SOCKET_TIMEOUT) / 3

    // START - Regex's
    private val CAPTURE_DOMAIN_REGEX = Regex( "^(?:https?://)?([^/]+)(?:/.*)?\$" )
    private val TLD_REGEX = Regex("([a-zA-Z0-9-]+\\.[a-zA-Z]{2,})$")
    // END - Regex's

    val CLIENT = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT
            connectTimeoutMillis = CONNECT_TIMEOUT
            socketTimeoutMillis = SOCKET_TIMEOUT
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

    /**
     * Extract domain name from a full length URL.
     *
     * For example, url **https://sub1.example.org/path/to/file** will
     * return **sub1.example.org**
     *
     * If the provided is not a url, it will return the url
     *
     * @param url to extract domain name from
     */
    internal fun getDomainName( url: String ) =
        CAPTURE_DOMAIN_REGEX.find(url)?.groups?.get(1)?.value ?: url

    /**
     * Extract top-level domain name from a URL.
     *
     * For example, **sub1.example.org** will become **example.org**
     *
     * If the provided is not a url, it will return the url
     *
     * @param url to extract top-level domain name
     */
    internal fun getTld( url: String ): String {
        val domain = getDomainName( url )
        return TLD_REGEX.find( domain )?.value ?: domain
    }

    /**
     * This pattern is design to match all sub domains (whether it's there or not) of a domain.
     *
     * I.E. **_(?:[a-zA-Z0-9-]+\.)*example.org_** matches:
     * - example.org
     * - sub1.example.org
     * - extra.sub1.example.org
     */
    internal fun genMatchAllTld( url: String ) =
        Regex( "(?:[a-zA-Z0-9-]+\\.)*${getTld(url)}" )
}