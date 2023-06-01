package com.cjrodriguez.blingmusicplayer.dataSource.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cjrodriguez.blingmusicplayer.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayListDao {

    @Query("SELECT * FROM playlistCacheTable")
    fun getAllPlaylist(): Flow<List<Playlist>>

    @Query("SELECT COUNT(id) FROM playlistCacheTable")
    fun getCountOfPlayList(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylist(playlist: Playlist)

    @Query("DELETE FROM playlistCacheTable")
    fun deleteAllPlayList()

    @Query("DELETE FROM playlistCacheTable WHERE id = (:idList)")
    fun deleteSpecificPlaylist(idList: List<String>)
}