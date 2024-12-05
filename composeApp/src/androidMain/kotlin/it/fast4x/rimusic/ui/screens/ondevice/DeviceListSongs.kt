package it.fast4x.rimusic.ui.screens.ondevice

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.enums.OnDeviceSongSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.models.OnDeviceSong
import it.fast4x.rimusic.service.LOCAL_KEY_PREFIX
import it.fast4x.rimusic.utils.OnDeviceBlacklist
import it.fast4x.rimusic.utils.isAtLeastAndroid10
import it.fast4x.rimusic.utils.isAtLeastAndroid11
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val mediaScope = CoroutineScope(Dispatchers.IO + CoroutineName("MediaStore worker"))
fun Context.musicFilesAsFlow(sortBy: OnDeviceSongSortBy, order: SortOrder, context: Context): StateFlow<List<OnDeviceSong>> = flow {
    var version: String? = null

    while (currentCoroutineContext().isActive) {
        val newVersion = MediaStore.getVersion(applicationContext)
        if (version != newVersion) {
            version = newVersion
            val collection =
                if (isAtLeastAndroid10) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            var projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                if (isAtLeastAndroid10) {
                    MediaStore.Audio.Media.RELATIVE_PATH
                } else {
                    MediaStore.Audio.Media.DATA
                },
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_MODIFIED
            )

            if (isAtLeastAndroid11)
                projection += MediaStore.Audio.Media.BITRATE

            projection += MediaStore.Audio.Media.SIZE

            val sortOrderSQL = when (order) {
                SortOrder.Ascending -> "ASC"
                SortOrder.Descending -> "DESC"
            }

            val sortBySQL = when (sortBy) {
                OnDeviceSongSortBy.Title -> "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE $sortOrderSQL"
                OnDeviceSongSortBy.DateAdded -> "${MediaStore.Audio.Media.DATE_ADDED} $sortOrderSQL"
                OnDeviceSongSortBy.Artist -> "${MediaStore.Audio.Media.ARTIST} COLLATE NOCASE $sortOrderSQL"
                OnDeviceSongSortBy.Duration -> "${MediaStore.Audio.Media.DURATION} COLLATE NOCASE $sortOrderSQL"
                OnDeviceSongSortBy.Album -> "${MediaStore.Audio.Media.ALBUM} COLLATE NOCASE $sortOrderSQL"
            }

            val albumUriBase = Uri.parse("content://media/external/audio/albumart")

            contentResolver.query(collection, projection, null, null, sortBySQL)
                ?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    Timber.i(" DeviceListSongs colums idIdx $idIdx")
                    val nameIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    Timber.i(" DeviceListSongs colums nameIdx $nameIdx")
                    val durationIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    Timber.i(" DeviceListSongs colums durationIdx $durationIdx")
                    val artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    Timber.i(" DeviceListSongs colums artistIdx $artistIdx")
                    val albumIdIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    Timber.i(" DeviceListSongs colums albumIdIdx $albumIdIdx")
                    val relativePathIdx = if (isAtLeastAndroid10) {
                        cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                    } else {
                        cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    }
                    Timber.i(" DeviceListSongs colums relativePathIdx $relativePathIdx")
                    val titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    Timber.i(" DeviceListSongs colums titleIdx $titleIdx")
                    val isMusicIdx = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
                    Timber.i(" DeviceListSongs colums isMusicIdx $isMusicIdx")
                    val mimeTypeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                    Timber.i(" DeviceListSongs colums mimeTypeIdx $mimeTypeIdx")
                    val bitrateIdx = if (isAtLeastAndroid11) cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE) else -1
                    Timber.i(" DeviceListSongs colums bitrateIdx $bitrateIdx")
                    val fileSizeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                    Timber.i(" DeviceListSongs colums fileSizeIdx $fileSizeIdx")
                    val dateModifiedIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                    Timber.i(" DeviceListSongs colums dateModifiedIdx $dateModifiedIdx")


                    val blacklist = OnDeviceBlacklist(context = context)

                    Timber.i(" DeviceListSongs SDK ${Build.VERSION.SDK_INT} initialize columns complete")

                    buildList {
                        while (cursor.moveToNext()) {
                            if (cursor.getInt(isMusicIdx) == 0) continue
                            val id = cursor.getLong(idIdx)
                            val name = cursor.getString(nameIdx).substringBeforeLast(".")
                            val trackName = cursor.getString(titleIdx)
                            Timber.i(" DeviceListSongs trackName $trackName loaded")
                            val duration = cursor.getInt(durationIdx)
                            if (duration == 0) continue
                            val artist = cursor.getString(artistIdx)
                            val albumId = cursor.getLong(albumIdIdx)

                            val mimeType = cursor.getString(mimeTypeIdx)
                            val bitrate = if (isAtLeastAndroid11) cursor.getInt(bitrateIdx) else 0
                            val fileSize = cursor.getInt(fileSizeIdx)
                            val dateModified = cursor.getLong(dateModifiedIdx)

                            val relativePath = if (isAtLeastAndroid10) {
                                cursor.getString(relativePathIdx)
                            } else {
                                cursor.getString(relativePathIdx).substringBeforeLast("/")
                            }
                            val exclude = blacklist.contains(relativePath)

                            if (!exclude) {
                                runCatching {
                                    val albumUri = ContentUris.withAppendedId(albumUriBase, albumId)
                                    val durationText =
                                        duration.milliseconds.toComponents { minutes, seconds, _ ->
                                            "$minutes:${seconds.toString().padStart(2, '0')}"
                                        }
                                    val song = OnDeviceSong(
                                        id = "$LOCAL_KEY_PREFIX$id",
                                        title = trackName ?: name,
                                        artistsText = artist,
                                        durationText = durationText,
                                        thumbnailUrl = albumUri.toString(),
                                        relativePath = relativePath
                                    )
                                    Database.insert(
                                        song.toSong()
                                    )
                                    
                                    Database.insert(
                                        it.fast4x.rimusic.models.Format(
                                            songId = song.id,
                                            itag = 0,
                                            mimeType = mimeType,
                                            bitrate = bitrate.toLong(),
                                            contentLength = fileSize.toLong(),
                                            lastModified = dateModified
                                        )
                                    )

                                    add(
                                        song
                                    )
                                }.onFailure {
                                    Timber.e("DeviceListSongs addSong error ${it.stackTraceToString()}")
                                }
                            }
                        }
                    }
                }?.let {
                    runCatching {
                        emit(it)
                    }.onFailure {
                        Timber.e("DeviceListSongs emit error ${it.stackTraceToString()}")
                    }
                }
        }
        runCatching {
            delay(5.seconds)
        }
    }
}.distinctUntilChanged()
    .stateIn(mediaScope, SharingStarted.Eagerly, listOf())
