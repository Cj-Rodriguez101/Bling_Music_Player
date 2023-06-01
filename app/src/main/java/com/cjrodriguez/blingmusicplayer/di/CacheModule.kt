package com.cjrodriguez.blingmusicplayer.di

import android.content.Context
import androidx.room.Room
import com.cjrodriguez.blingmusicplayer.dataSource.cache.PlayListDao
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDatabase
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongPlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Singleton
    @Provides
    fun provideSongDatabase(@ApplicationContext app: Context): SongDatabase {
        return Room.databaseBuilder(
            app, SongDatabase::class.java, "Songs.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideSongDao(songDatabase: SongDatabase): SongDao{
        return songDatabase.songDao()
    }

    @Singleton
    @Provides
    fun providePlayListDao(songDatabase: SongDatabase): PlayListDao{
        return songDatabase.playlistDao()
    }

    @Singleton
    @Provides
    fun provideSongPlaylistDao(songDatabase: SongDatabase): SongPlaylistDao{
        return songDatabase.songPlaylistDao()
    }
}