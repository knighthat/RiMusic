package it.fast4x.rimusic

import coil3.Uri
import coil3.toUri

const val PINNED_PREFIX = "pinned:"
const val MODIFIED_PREFIX = "modified:"
const val MONTHLY_PREFIX = "monthly:"
const val PIPED_PREFIX = "piped:"
const val EXPLICIT_PREFIX = "e:"
const val LOCAL_KEY_PREFIX = "local:"

fun cleanPrefix(text: String): String {
    var cleanText = text.substringAfter(PINNED_PREFIX)
    cleanText = cleanText.substringAfter(MONTHLY_PREFIX)
    cleanText = cleanText.substringAfter(PIPED_PREFIX)
    cleanText = cleanText.substringAfter(EXPLICIT_PREFIX)
    cleanText = cleanText.substringAfter(MODIFIED_PREFIX)
    return cleanText
}

fun String?.thumbnail(size: Int): String? {
    return when {
        this?.startsWith("https://lh3.googleusercontent.com") == true -> "$this-w$size-h$size"
        this?.startsWith("https://yt3.ggpht.com") == true -> "$this-w$size-h$size-s$size"
        else -> this
    }
}
fun String?.thumbnail(): String? {
    return this
}
fun Uri?.thumbnail(size: Int): Uri? {
    return toString().thumbnail(size)?.toUri()
}