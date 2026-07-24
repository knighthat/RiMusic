package app.kreate.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.eygraber.uri.toAndroidUri
import okio.BufferedSource
import okio.buffer
import okio.source
import org.koin.java.KoinJavaComponent


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

internal actual fun com.eygraber.uri.Uri.readFile(): BufferedSource? {
    val context: Context = KoinJavaComponent.get(Context::class.java)
    val uri = toAndroidUri()

    return context.contentResolver
                  .openInputStream( toAndroidUri() )
                  ?.source()
                  ?.buffer()
}