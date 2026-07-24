package app.kreate.utils

import co.touchlab.kermit.Logger
import com.eygraber.uri.Uri
import okio.BufferedSource


internal expect fun Uri.readFile(): BufferedSource?

/**
 * Verifies that this [Uri] instance is pointing to a local file.
 */
expect fun Uri.isLocalFile(): Boolean

expect fun Uri.guessMimetype(): String?

fun Uri.readByteArray(): ByteArray? =
    try {
        readFile()?.use( BufferedSource::readByteArray )
    } catch( err: Exception ) {
        Logger.e( "Failed to read file \"$this\"", err, "UriUtil" )
        null
    }