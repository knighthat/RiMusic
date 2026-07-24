package app.kreate.utils

import com.eygraber.uri.Uri


expect object ImageProcessor {

    /**
     * Processes an image from a given Uri:
     *
     * a. Verifies if it's an image by its MIME type.
     * b. Checks if dimensions exceed MAX_DIMENSION or file size exceeds MAX_FILE_SIZE_BYTES.
     * c. If limits are exceeded, scales and compresses the image to meet specs.
     * d. If not an image, throws IllegalArgumentException.
     *
     * @param context The application context
     * @param artworkUri The Uri of the image file (must be a local path)
     *
     * @return A Uri to the processed image (original local, compressed/scaled local, or original remote Uri)
     */
    fun compressArtwork( artworkUri: Uri, maxWidth: Int, maxHeight: Int, maxSize: Long ): Uri
}