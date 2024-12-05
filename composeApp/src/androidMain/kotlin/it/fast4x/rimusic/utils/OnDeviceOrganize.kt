package it.fast4x.rimusic.utils

import it.fast4x.rimusic.enums.OnDeviceFolderSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.models.Folder
import it.fast4x.rimusic.models.OnDeviceSong
import it.fast4x.rimusic.models.SongEntity

class OnDeviceOrganize {
    companion object {
        fun sortSongs(sortOrder: SortOrder, sortBy: OnDeviceFolderSortBy, songs: List<SongEntity>): List<SongEntity> {
            return when (sortBy) {
                OnDeviceFolderSortBy.Title -> {
                    if (sortOrder == SortOrder.Ascending)
                        songs.sortedBy { it.song.title }
                    else
                        songs.sortedByDescending { it.song.title }
                }
                OnDeviceFolderSortBy.Artist -> {
                    if (sortOrder == SortOrder.Ascending)
                        songs.sortedBy { it.song.artistsText }
                    else
                        songs.sortedByDescending { it.song.artistsText }
                }
                OnDeviceFolderSortBy.Duration -> {
                    if (sortOrder == SortOrder.Ascending)
                        songs.sortedBy { durationToMillis(it.song.durationText ?: "0:00") }
                    else
                        songs.sortedByDescending { durationToMillis(it.song.durationText ?: "0:00") }
                }
            }
        }
        fun organizeSongsIntoFolders(songs: List<OnDeviceSong>): Folder {
            val rootFolder = Folder("/", fullPath = "/")

            for (song in songs) {
                if (song.relativePath == "/") {
                    rootFolder.addSong(song)
                }
                else {
                    val pathSegments = song.relativePath.split('/')
                    var currentFolder = rootFolder
                    var currentFullPath = ""

                    for (segment in pathSegments) {
                        if (segment.isNotBlank()) {
                            currentFullPath += "/$segment"
                            val existingFolder = currentFolder.subFolders.find { it.name == segment }
                            currentFolder = if (existingFolder != null) {
                                existingFolder
                            } else {
                                val newFolder = Folder(segment, currentFolder, fullPath = currentFullPath + "/")
                                currentFolder.addSubFolder(newFolder)
                                newFolder
                            }
                        }
                    }

                    currentFolder.addSong(song)
                }
            }

            return rootFolder
        }

        fun getFolderByPath(rootFolder: Folder, path: String): Folder? {
            if (path == "/") {
                return rootFolder;
            }

            val pathSegments = path.trim('/').split('/')

            var currentFolder = rootFolder

            for (segment in pathSegments) {
                val folder = currentFolder.subFolders.find { it.name == segment }

                if (folder != null) {
                    currentFolder = folder
                } else {
                    return null
                }
            }

            return currentFolder
        }
    }
}