package it.fast4x.rimusic.utils

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.IntDef
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Marker interface that marks a class as Bundle accessor
 */
interface BundleAccessor

private inline fun <T> Bundle.bundleDelegate(
    name: String? = null,
    crossinline get: Bundle.(String) -> T,
    crossinline set: Bundle.(k: String, v: T) -> Unit
) = PropertyDelegateProvider<BundleAccessor, ReadWriteProperty<BundleAccessor, T>> { _, property ->
    val actualName = name ?: property.name

    object : ReadWriteProperty<BundleAccessor, T> {
        override fun getValue(thisRef: BundleAccessor, property: KProperty<*>) =
            get(this@Bundle, actualName)

        override fun setValue(thisRef: BundleAccessor, property: KProperty<*>, value: T) =
            set(this@Bundle, actualName, value)
    }
}

//context(BundleAccessor)
val Bundle.boolean get() = boolean()

//context(BundleAccessor)
fun Bundle.boolean(name: String? = null) = bundleDelegate(
    name = name,
    get = { getBoolean(it) },
    set = { k, v -> putBoolean(k, v) }
)

//context(BundleAccessor)
val Bundle.byte get() = byte()

//context(BundleAccessor)
fun Bundle.byte(name: String? = null) = bundleDelegate(
    name = name,
    get = { getByte(it) },
    set = { k, v -> putByte(k, v) }
)

//context(BundleAccessor)
val Bundle.char get() = char()

//context(BundleAccessor)
fun Bundle.char(name: String? = null) = bundleDelegate(
    name = name,
    get = { getChar(it) },
    set = { k, v -> putChar(k, v) }
)

//context(BundleAccessor)
val Bundle.short get() = short()

//context(BundleAccessor)
fun Bundle.short(name: String? = null) = bundleDelegate(
    name = name,
    get = { getShort(it) },
    set = { k, v -> putShort(k, v) }
)

//context(BundleAccessor)
val Bundle.int get() = int()

//context(BundleAccessor)
fun Bundle.int(name: String? = null) = bundleDelegate(
    name = name,
    get = { getInt(it) },
    set = { k, v -> putInt(k, v) }
)

//context(BundleAccessor)
val Bundle.long get() = long()

//context(BundleAccessor)
fun Bundle.long(name: String? = null) = bundleDelegate(
    name = name,
    get = { getLong(it) },
    set = { k, v -> putLong(k, v) }
)

//context(BundleAccessor)
val Bundle.float get() = float()

//context(BundleAccessor)
fun Bundle.float(name: String? = null) = bundleDelegate(
    name = name,
    get = { getFloat(it) },
    set = { k, v -> putFloat(k, v) }
)

//context(BundleAccessor)
val Bundle.double get() = double()

//context(BundleAccessor)
fun Bundle.double(name: String? = null) = bundleDelegate(
    name = name,
    get = { getDouble(it) },
    set = { k, v -> putDouble(k, v) }
)

//context(BundleAccessor)
val Bundle.string get() = string()

//context(BundleAccessor)
fun Bundle.string(name: String? = null) = bundleDelegate(
    name = name,
    get = { getString(it) },
    set = { k, v -> putString(k, v) }
)

//context(BundleAccessor)
val Bundle.intList get() = intList()

//context(BundleAccessor)
fun Bundle.intList(name: String? = null) = bundleDelegate(
    name = name,
    get = { getIntegerArrayList(it) },
    set = { k, v -> putIntegerArrayList(k, v) }
)

//context(BundleAccessor)
val Bundle.stringList get() = stringList()

//context(BundleAccessor)
fun Bundle.stringList(name: String? = null) = bundleDelegate<List<String>?>(
    name = name,
    get = { getStringArrayList(it) },
    set = { k, v -> putStringArrayList(k, v?.let { ArrayList(it) }) }
)

//context(BundleAccessor)
val Bundle.booleanArray get() = booleanArray()

//context(BundleAccessor)
fun Bundle.booleanArray(name: String? = null) = bundleDelegate(
    name = name,
    get = { getBooleanArray(it) },
    set = { k, v -> putBooleanArray(k, v) }
)

//context(BundleAccessor)
val Bundle.byteArray get() = byteArray()

//context(BundleAccessor)
fun Bundle.byteArray(name: String? = null) = bundleDelegate(
    name = name,
    get = { getByteArray(it) },
    set = { k, v -> putByteArray(k, v) }
)

//context(BundleAccessor)
val Bundle.shortArray get() = shortArray()

//context(BundleAccessor)
fun Bundle.shortArray(name: String? = null) = bundleDelegate(
    name = name,
    get = { getShortArray(it) },
    set = { k, v -> putShortArray(k, v) }
)

//context(BundleAccessor)
val Bundle.charArray get() = charArray()

//context(BundleAccessor)
fun Bundle.charArray(name: String? = null) = bundleDelegate(
    name = name,
    get = { getCharArray(it) },
    set = { k, v -> putCharArray(k, v) }
)

//context(BundleAccessor)
val Bundle.intArray get() = intArray()

//context(BundleAccessor)
fun Bundle.intArray(name: String? = null) = bundleDelegate(
    name = name,
    get = { getIntArray(it) },
    set = { k, v -> putIntArray(k, v) }
)

//context(BundleAccessor)
val Bundle.floatArray get() = floatArray()

//context(BundleAccessor)
fun Bundle.floatArray(name: String? = null) = bundleDelegate(
    name = name,
    get = { getFloatArray(it) },
    set = { k, v -> putFloatArray(k, v) }
)

//context(BundleAccessor)
val Bundle.doubleArray get() = doubleArray()

//context(BundleAccessor)
fun Bundle.doubleArray(name: String? = null) = bundleDelegate(
    name = name,
    get = { getDoubleArray(it) },
    set = { k, v -> putDoubleArray(k, v) }
)

//context(BundleAccessor)
val Bundle.stringArray get() = stringArray()

//context(BundleAccessor)
fun Bundle.stringArray(name: String? = null) = bundleDelegate(
    name = name,
    get = { getStringArray(it) },
    set = { k, v -> putStringArray(k, v) }
)

class SongBundleAccessor(val extras: Bundle = Bundle()) : BundleAccessor {
    companion object {
        fun bundle(block: SongBundleAccessor.() -> Unit) = SongBundleAccessor().apply(block).extras
    }

    var albumId by extras.string
    var durationText by extras.string
    var artistNames by extras.stringList
    var artistIds by extras.stringList
    var explicit by extras.boolean
    var isFromPersistentQueue by extras.boolean
}

class ActivityIntentBundleAccessor(val extras: Bundle = Bundle()) : BundleAccessor {

    var query by extras.string(SearchManager.QUERY)
    var text by extras.string(Intent.EXTRA_TEXT)

    var album by extras.string(MediaStore.EXTRA_MEDIA_ALBUM)
    var artist by extras.string(MediaStore.EXTRA_MEDIA_ARTIST)
    var playlist by extras.string("android.intent.extra.playlist")
    var title by extras.string(MediaStore.EXTRA_MEDIA_TITLE)
}

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.TYPE,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY
)
@IntDef(
    AudioEffect.CONTENT_TYPE_MUSIC,
    AudioEffect.CONTENT_TYPE_MOVIE,
    AudioEffect.CONTENT_TYPE_GAME,
    AudioEffect.CONTENT_TYPE_VOICE
)
annotation class ContentType

class EqualizerIntentBundleAccessor(val extras: Bundle = Bundle()) : BundleAccessor {
    companion object {
        fun bundle(block: EqualizerIntentBundleAccessor.() -> Unit) =
            EqualizerIntentBundleAccessor().apply(block).extras
    }

    var audioSession by extras.int(AudioEffect.EXTRA_AUDIO_SESSION)
    var packageName by extras.string(AudioEffect.EXTRA_PACKAGE_NAME)
    var contentType by extras.int(AudioEffect.EXTRA_CONTENT_TYPE)
        @ContentType
        get

        @SuppressLint("SupportAnnotationUsage")
        @ContentType
        set
}
