package com.cjrodriguez.blingmusicplayer.dataSource.cache

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songCacheTable ORDER BY title")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songCacheTable WHERE title LIKE '%' || :query || '%' OR  :query  = '' ORDER BY title COLLATE NOCASE ASC")
    fun searchSongs(query: String): PagingSource<Int, Song>

    @Query("SELECT s.*, IFNULL((SELECT 1 FROM songPlaylistTable sp INNER JOIN playlistCacheTable p ON sp.playlistId = p.id WHERE sp.songId = s.id AND p.id = :selectedPlaylist), 0) AS isFavourite FROM songCacheTable s WHERE title LIKE '%' || :query || '%' OR  :query  = '' ORDER BY title COLLATE NOCASE ASC")
    fun searchSongsAndFavourite(query: String, selectedPlaylist: Int): PagingSource<Int, SongWithFavourite>

    @Query("SELECT id FROM songCacheTable WHERE id NOT IN (:idList)")
    fun getSongIdListToBeDeleted(idList: List<Long>): List<Long>

    //Get Next Song, If Current Song Is At the End Return the Beginning (Circular Queue)
    @Query("SELECT * FROM (SELECT s.*, IFNULL((SELECT 1 FROM songPlaylistTable sp INNER JOIN playlistCacheTable p ON sp.playlistId = p.id WHERE sp.songId = s.id AND p.id = :selectedPlaylist), 0) AS isFavourite FROM songCacheTable s WHERE :isShuffle ORDER BY random() LIMIT 1) UNION ALL SELECT * FROM ( SELECT s.*, IFNULL((SELECT 1 FROM songPlaylistTable sp INNER JOIN playlistCacheTable p ON sp.playlistId = p.id WHERE sp.songId = s.id AND p.id = :selectedPlaylist), 0) AS isFavourite FROM songCacheTable s WHERE sortedUnSpacedTitle > :title AND id != :id AND NOT(:isShuffle) ORDER BY sortedUnSpacedTitle COLLATE NOCASE ASC LIMIT 1) UNION ALL SELECT * from (SELECT s.*, IFNULL((SELECT 1 FROM songPlaylistTable sp INNER JOIN playlistCacheTable p ON sp.playlistId = p.id WHERE sp.songId = s.id AND p.id = :selectedPlaylist), 0) AS isFavourite from songCacheTable s WHERE NOT(:isShuffle) ORDER BY sortedUnSpacedTitle LIMIT 1)")
    fun getNextSong(
        title: String, id: Long, isShuffle: Boolean? = null, selectedPlaylist: Int = 0
    ): Flow<SongWithFavourite?>

    @Query(
        "SELECT * FROM (SELECT s.*, IFNULL((SELECT 1 FROM songPlaylistTable sp INNER JOIN playlistCacheTable p ON sp.playlistId = p.id WHERE sp.songId = s.id AND p.id = :selectedPlaylist), 0) AS isFavourite FROM songCacheTable s WHERE sortedUnSpacedTitle < :title AND id != :id ORDER BY sortedUnSpacedTitle COLLATE NOCASE DESC LIMIT 1) UNION ALL SELECT * from (SELECT s.*, IFNULL((SELECT 1 FROM songPlaylistTable sp INNER JOIN playlistCacheTable p ON sp.playlistId = p.id WHERE sp.songId = s.id AND p.id = :selectedPlaylist), 0) AS isFavourite FROM songCacheTable s ORDER BY sortedUnSpacedTitle DESC LIMIT 1)"
    )
    fun getPreviousSong(title: String, id: Long=0L, selectedPlaylist: Int = 0): Flow<SongWithFavourite?>

    @Query("SELECT s.*, IFNULL((SELECT 1 FROM songPlaylistTable sp INNER JOIN playlistCacheTable p ON sp.playlistId = p.id WHERE sp.songId = s.id AND p.id = 0), 0) AS isFavourite FROM songCacheTable s WHERE id = :id")
    fun getSpecificSong(id: Long): Flow<SongWithFavourite?>

    @Query("SELECT * FROM songCacheTable ORDER BY updatedAt DESC LIMIT 1")
    fun getLastSong(): Flow<Song?>

    @Query("DELETE FROM songCacheTable")
    fun deleteAllSongs()

    @Query("DELETE FROM songCacheTable WHERE id IN (:id)")
    fun deleteSpecificSongs(id: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateSongs(vararg songs: Song)
}