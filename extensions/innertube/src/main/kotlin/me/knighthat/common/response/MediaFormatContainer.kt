package me.knighthat.common.response

import java.util.SortedSet

interface MediaFormatContainer<T> where T: AudioFormat{

    val formats: SortedSet<out T>

    val autoMaxQualityFormat: T
        get() = highestQualityFormat

    val highestQualityFormat: T
        get() = formats.last()

    val mediumQualityFormat: T
        get() = formats.toList()[formats.size / 2]

    val lowestQualityFormat: T
        get() = formats.first()
}