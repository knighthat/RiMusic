package it.fast4x.rimusic.utils

import android.content.Context
import android.net.Uri
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.LogType
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

fun saveImageToInternalStorage(context: Context, imageUri: Uri, dirPath: String, thumbnailName: String): Uri? {
    try {
        // Open input stream from the URI
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

        // Create a new file in the app's internal storage
        if ( !createDirIfNotExists(context, dirPath)) {
            Timber.e("Failed to create directory: $dirPath")
            return null
        }
        val outputFile = File(context.filesDir, "$dirPath/$thumbnailName")

        val outputStream = FileOutputStream(outputFile)

        // Copy the data from the input stream to the output stream (internal storage)
        inputStream?.copyTo(outputStream)

        // Close the streams
        inputStream?.close()
        outputStream.flush()
        outputStream.close()

        // Return the URI to the saved file in internal storage
        return Uri.fromFile(outputFile)
    } catch (e: IOException) {
        Timber.e(e)
        return null
    }
}

fun checkFileExists(context: Context, filePath: String): String? {
    val file = File(context.filesDir, filePath)

    return if (file.exists()) {
        file.toURI().toString()
    } else {
        null
    }
}

fun deleteFileIfExists(context: Context, filePath: String): Boolean {
    val file = File(context.filesDir, filePath)

    return if (file.exists()) {
        file.delete()
    } else {
        false
    }
}

fun createDirIfNotExists(context: Context, dirPath: String): Boolean {
    val directory = File(context.filesDir, dirPath)

    return if (!directory.exists()) {
        directory.mkdirs()
    } else {
        true
    }
}

/*
fun tryMoveDir() {
    val from = File("/path/to/src")
    val to = File("/path/to/dest")
    val success = moveDir(from.toPath(), to.toPath())
    if (success) {
        println("File Moved Successfully")
    } else {
        println("File Moved Failed")
    }
}
 */

/*
fun tryCopyDir() {
    val from = File("/var/kotlin/")
    val to = File("/var/bak/kotlin/")
    try {
        copyDir(from.toPath(), to.toPath())
        println("Copying succeeded.")
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}
 */

fun loadAppLog(context: Context, type: LogType): String? {
    val file = File(context.filesDir.resolve("logs"),
        when (type) {
            LogType.Default ->  "RiMusic_log.txt"
            LogType.Crash ->    "RiMusic_crash_log.txt"
        }
    )
    if (file.exists()) {
        SmartMessage(context.resources.getString(R.string.value_copied), type = PopupType.Info, context = context)
        return file.readText()
    } else
        SmartMessage(context.resources.getString(R.string.no_log_available), type = PopupType.Info, context = context)
    return null
}