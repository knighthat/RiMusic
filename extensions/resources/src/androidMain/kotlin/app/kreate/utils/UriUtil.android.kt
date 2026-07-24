package app.kreate.utils

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap


/**
 * Verifies that this [Uri] instance is
 * pointing to a local file by checking
 * its scheme.
 */
fun Uri.isLocalFile() =
    scheme.equals( ContentResolver.SCHEME_CONTENT, true )
            || scheme.equals( ContentResolver.SCHEME_FILE, true )

actual fun com.eygraber.uri.Uri.isLocalFile(): Boolean =
    scheme.equals( ContentResolver.SCHEME_CONTENT, true )
            || scheme.equals( ContentResolver.SCHEME_FILE, true )

actual fun com.eygraber.uri.Uri.guessMimetype(): String? {
    val extension = MimeTypeMap.getFileExtensionFromUrl( this.toString() ).lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension( extension )
}