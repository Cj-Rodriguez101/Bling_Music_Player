package com.cjrodriguez.blingmusicplayer.dataSource.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cjrodriguez.blingmusicplayer.model.SongPlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface SongPlaylistDao {

    @Query("DELETE FROM songPlaylistTable WHERE songId IN (:songId)")
    fun deleteSongPlaylistFromFavorites(songId: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongPlaylist(songPlaylist: SongPlaylist)

    @Query("SELECT * FROM songPlaylistTable WHERE songId =:idToSearch OR playlistId =:idToSearch")
    fun getSpecificSongPlaylist(idToSearch: Long): Flow<List<SongPlaylist>>

    @Query("SELECT * FROM songPlaylistTable")
    fun getAllSongPlayList(): Flow<List<SongPlaylist>>


}