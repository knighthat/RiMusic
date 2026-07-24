package app.kreate.utils

import com.eygraber.uri.Uri


/**
 * Verifies that this [Uri] instance is pointing to a local file.
 */
expect fun Uri.isLocalFile(): Boolean

expect fun Uri.guessMimetype(): String?