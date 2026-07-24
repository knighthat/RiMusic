package app.kreate.utils

import com.eygraber.uri.Uri
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer


actual fun Uri.isLocalFile(): Boolean {
    TODO("Not yet implemented")
}

actual fun Uri.guessMimetype(): String? {
    TODO("Not yet implemented")
}

internal actual fun Uri.readFile(): BufferedSource? =
    FileSystem.SYSTEM.source( path!!.toPath() ).buffer()