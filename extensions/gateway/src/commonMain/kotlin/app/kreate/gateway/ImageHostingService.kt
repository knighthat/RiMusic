package app.kreate.gateway

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Clock


object ImageHostingService : KoinComponent {

    private const val LITTER_BOX_ENDPOINT = "https://litterbox.catbox.moe/resources/internals/api.php"

    private val logger = Logger.withTag( "ImageHostingService" )
    private val client: HttpClient get() = get()

    suspend fun uploadToLitterBox(
        mimetype: String,
        fileData: ByteArray
    ): Result<String> = runCatching {
        logger.v { "Uploading local artwork to LitterBox. Mimetype: $mimetype, size: ${fileData.size}  bytes" }

        val formData = formData {
            val timestamp = Clock.System.now().toEpochMilliseconds()

            append("reqtype", "fileupload")
            append("time", "1h")
            append("fileToUpload", fileData, Headers.build {
                append( HttpHeaders.ContentDisposition, "filename=\"$timestamp\"" )
                append( HttpHeaders.ContentType, mimetype )
            })
        }

        client.submitFormWithBinaryData( LITTER_BOX_ENDPOINT, formData )
              .bodyAsText()
    }.onSuccess {
        logger.d { "Local artwork uploaded successfully. Online url: $it" }
    }.onFailure {
        logger.e( it ) { "Error occurs while uploading local artwork" }
    }
}