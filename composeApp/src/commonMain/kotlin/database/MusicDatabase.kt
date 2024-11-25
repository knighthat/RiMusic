package database

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.RoomWarnings.Companion.QUERY_MISMATCH
import androidx.room.Upsert
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import database.entities.Album
import database.entities.Artist
import database.entities.Event
import database.entities.Format
import database.entities.Lyrics
import database.entities.Playlist
import database.entities.SearchQuery
import database.entities.Song
import database.entities.SongAlbumMap
import database.entities.SongArtistMap
import database.entities.SongEntity
import database.entities.SongPlaylistMap
import it.fast4x.rimusic.LOCAL_KEY_PREFIX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        Album::class,
        Artist::class,
        Event::class,
        Format::class,
        Lyrics::class,
        //QueuedMediaItem::class, //TODO: implement
        Playlist::class,
        SearchQuery::class,
        Song::class,
        SongAlbumMap::class,
        SongArtistMap::class,
        SongPlaylistMap::class
    ],
    version = 23,
    exportSchema = true
)
@ConstructedBy(MusicDatabaseConstructor::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun getDao(): MusicDatabaseDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MusicDatabaseConstructor : RoomDatabaseConstructor<MusicDatabase>

@Dao
interface MusicDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Song): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Album): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SongAlbumMap): Long

    @Upsert
    suspend fun upsert(item: Song): Long

    @Upsert
    suspend fun upsert(item: Album): Long

    @Upsert
    suspend fun upsert(item: SongAlbumMap): Long

    @Delete
    suspend fun delete(item: Song)

    @Query("SELECT * FROM Song")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM Album")
    fun getAllAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM Song WHERE id = :id")
    suspend fun getSong(id: String): Song?

    @SuppressWarnings(QUERY_MISMATCH)
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.title COLLATE NOCASE ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleAsc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM Album WHERE id = :id")
    fun album(id: String): Flow<Album?>
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<MusicDatabase>
): MusicDatabase {
    return builder
        //.addMigrations(MIGRATIONS)
        //.fallbackToDestructiveMigrationOnDowngrade()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

