package com.cjrodriguez.blingmusicplayer.di

import com.cjrodriguez.blingmusicplayer.BaseApplication
import com.cjrodriguez.blingmusicplayer.dataSource.cache.PlayListDao
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongPlaylistDao
import com.cjrodriguez.blingmusicplayer.interactors.AddRemoveSongFromToPlaylist
import com.cjrodriguez.blingmusicplayer.interactors.CheckIfMediaItemsShouldUpdate
import com.cjrodriguez.blingmusicplayer.interactors.DeleteSong
import com.cjrodriguez.blingmusicplayer.interactors.GetAllSongs
import com.cjrodriguez.blingmusicplayer.interactors.GetSingleSong
import com.cjrodriguez.blingmusicplayer.repository.SongRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object InteractorsModule {

    @ViewModelScoped
    @Provides
    fun provideGetAllSongs(
        baseApplication: BaseApplication,
        songDao: SongDao,
        playListDao: PlayListDao,
        songPlaylistDao: SongPlaylistDao
    ): GetAllSongs {
        return GetAllSongs(
            baseApplication.applicationContext,
            songDao,
            playListDao,
            songPlaylistDao
        )
    }

    @ViewModelScoped
    @Provides
    fun provideGetSingleSong(baseApplication: BaseApplication, songDao: SongDao): GetSingleSong {
        return GetSingleSong(baseApplication.applicationContext, songDao)
    }

    @ViewModelScoped
    @Provides
    fun provideCheckIfMediaItemsShouldUpdate(
        baseApplication: BaseApplication,
        songDao: SongDao
    ): CheckIfMediaItemsShouldUpdate {
        return CheckIfMediaItemsShouldUpdate(baseApplication.applicationContext, songDao)
    }

    @ViewModelScoped
    @Provides
    fun provideAddRemoveSongFromToPlaylist(
        baseApplication: BaseApplication,
        songDao: SongDao,
        songPlaylistDao: SongPlaylistDao
    ): AddRemoveSongFromToPlaylist {
        return AddRemoveSongFromToPlaylist(baseApplication, songDao, songPlaylistDao)
    }

    @ViewModelScoped
    @Provides
    fun provideDeleteSongs(
        baseApplication: BaseApplication,
        songDao: SongDao,
        songPlaylistDao: SongPlaylistDao
    ): DeleteSong {
        return DeleteSong(baseApplication.applicationContext, songDao, songPlaylistDao)
    }

    @ViewModelScoped
    @Provides
    fun provideSongRepository(
        getAllSongs: GetAllSongs,
        getSingleSong: GetSingleSong,
        checkIfMediaItemsShouldUpdate: CheckIfMediaItemsShouldUpdate,
        addRemoveSongFromToPlaylist: AddRemoveSongFromToPlaylist,
        deleteSong: DeleteSong,
        songDao: SongDao,
    ): SongRepository {
        return SongRepository(
            getAllSongs,
            getSingleSong,
            checkIfMediaItemsShouldUpdate,
            addRemoveSongFromToPlaylist,
            deleteSong,
            songDao
        )
    }

//    @androidx.media3.common.util.UnstableApi
//    @ViewModelScoped
//    @Provides
//    fun provideSessionToken(baseApplication: BaseApplication): SessionToken{
//        return SessionToken(baseApplication.applicationContext,
//            ComponentName(baseApplication.applicationContext, MusicLibraryService::class.java))
//    }

    //remove
//    @ViewModelScoped
//    @Provides
//    fun provideListenerFuture(
//        baseApplication: BaseApplication,
//        sessionToken: SessionToken
//    ): ListenableFuture<MediaController> {
//        return MediaController.Builder(baseApplication.applicationContext, sessionToken).buildAsync()
//    }
//
//    @ViewModelScoped
//    @Provides
//    fun provideMediaController(controllerFuture: ListenableFuture<MediaController>): MediaController?{
//    var controller: MediaController? = null
//    runBlocking {
//
//    }
//        return controllerFuture.await()
//    }

}