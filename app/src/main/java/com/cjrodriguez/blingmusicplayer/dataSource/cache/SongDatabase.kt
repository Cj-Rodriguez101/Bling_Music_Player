package com.cjrodriguez.blingmusicplayer.dataSource.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cjrodriguez.blingmusicplayer.model.Playlist
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.model.SongPlaylist

@TypeConverters(ConverterHelper::class)
@Database(
    entities = [Song::class, Playlist::class, SongPlaylist::class],
    version = 5,
    exportSchema = false
)
abstract class SongDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    abstract fun playlistDao(): PlayListDao

    abstract fun songPlaylistDao(): SongPlaylistDao
}