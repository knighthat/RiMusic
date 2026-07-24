package app.kreate.gateway.discord

import co.touchlab.kermit.Logger
import com.eygraber.uri.Uri
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.get


object DiscordApi : KoinComponent {

    const val API_VERSION = "10"
    const val APPLICATION_ID = "1370148610158759966"

    private val logger = Logger.withTag( "DiscordApi" )
    private val client: HttpClient get() = get()

    suspend fun getExternalImageUrl(
        imageUrl: String,
        token: String
    ): Result<String> = runCatching {
        logger.v { "Posting \"$imageUrl\" to Discord to get external url" }

        if( imageUrl.startsWith( "mp:" ) ) {
            logger.w { "imageUrl already an external url" }
            return@runCatching imageUrl
        }
        require( token.isNotBlank() ) { "Can't post imageUrl to Discord without token" }
        val scheme = Uri.parse( imageUrl ).scheme
        require(
            scheme.equals( "http", true )
                    || scheme.equals( "https", true )
        ) { "Only \"http\" and \"https\" are supported!" }

        val postUrl = "https://discord.com/api/v${API_VERSION}/applications/$APPLICATION_ID/external-assets"
        client.post( postUrl ) {
            header( HttpHeaders.Authorization, token )
            // For some reason, this is required.
            // "java.lang.ClassCastException: kotlinx.serialization.json.JsonObject cannot be cast to io.ktor.http.content.OutgoingContent"
            // will be thrown otherwise
            header( HttpHeaders.ContentType, ContentType.Application.Json )

            setBody(
                // Use this to ensure syntax
                // {"urls":[imageUrl]}
                buildJsonObject {
                    putJsonArray( "urls" ) { add( imageUrl ) }
                }
            )
        }
            // The response JSON is not complex enough to make a separate data class
            .body<JsonArray>()
            .firstNotNullOf { it.jsonObject["external_asset_path"] }
            .jsonPrimitive
            .content
            .let { "mp:$it" }
    }.onSuccess {
        logger.d { "External url: $it" }
    }.onFailure {
        logger.e( it ) { "Error occurs while posting imageUrl for external url" }
    }
}