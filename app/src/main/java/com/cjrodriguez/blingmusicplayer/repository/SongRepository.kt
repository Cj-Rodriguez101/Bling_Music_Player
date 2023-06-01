package com.cjrodriguez.blingmusicplayer.repository

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.interactors.AddRemoveSongFromToPlaylist
import com.cjrodriguez.blingmusicplayer.interactors.CheckIfMediaItemsShouldUpdate
import com.cjrodriguez.blingmusicplayer.interactors.DeleteSong
import com.cjrodriguez.blingmusicplayer.interactors.GetAllSongs
import com.cjrodriguez.blingmusicplayer.interactors.GetSingleSong
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.util.DataState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SongRepository @Inject constructor(
    private val getAllSongs: GetAllSongs,
    private val getSingleSong: GetSingleSong,
    private val checkIfMediaItemsShouldUpdate: CheckIfMediaItemsShouldUpdate,
    private val addRemoveSongFromToPlaylist: AddRemoveSongFromToPlaylist,
    private val deleteSong: DeleteSong,
    private val songDao: SongDao,
) {

    fun checkReadPermission(isPermanentlyDeclined: Boolean): Flow<DataState<Boolean>> {
        return checkIfMediaItemsShouldUpdate.execute(isPermanentlyDeclined)
    }

    fun updateAllSongs(isPermanentlyDeclined: Boolean): Flow<DataState<List<Song>>> {
        return getAllSongs.getAndStoreMusic(isPermanentlyDeclined)
    }

    fun setUnsetFavourite(
        songWithFavourite: SongWithFavourite,
        selectedPlaylistId: Long = 0L
    ): Flow<DataState<SongWithFavourite?>> {
        return addRemoveSongFromToPlaylist.execute(
            songId = songWithFavourite.song.id,
            isAlreadyAdded = songWithFavourite.isFavourite == 1,
            selectedPlaylistId = selectedPlaylistId
        )
    }

    fun getSearchedSongs(query: String): Flow<PagingData<SongWithFavourite>> {
        return Pager(config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            maxSize = 20 * 3
        ),
            pagingSourceFactory = { songDao.searchSongsAndFavourite(query, 0) }).flow
    }

    fun getNextSong(title: String, id: Long, isShuffle: Boolean): Flow<SongWithFavourite?> {
        return songDao.getNextSong(
            title, id,
            isShuffle
        )
    }

    fun getPreviousSong(title: String, id: Long): Flow<SongWithFavourite?> {
        return songDao.getPreviousSong(title, id)
    }

    fun getSingleSong(id: Long): Flow<DataState<SongWithFavourite?>> {
        return getSingleSong.execute(id)
    }

    fun deleteSong(
        songList: List<Song>,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ): Flow<DataState<Boolean>> {
        return deleteSong.execute(songList, launcher)
    }

    fun completeDeleteIfDialogComplete(songList: List<Song>): Flow<DataState<Boolean>>{
        return deleteSong.completeDeleteIfDialogComplete(songList)
    }
}