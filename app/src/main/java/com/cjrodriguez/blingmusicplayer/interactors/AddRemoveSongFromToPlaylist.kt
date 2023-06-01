package com.cjrodriguez.blingmusicplayer.interactors

import android.content.Context
import android.util.Log
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongPlaylistDao
import com.cjrodriguez.blingmusicplayer.model.SongPlaylist
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.util.DataState
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AddRemoveSongFromToPlaylist @Inject constructor(
    private val context: Context,
    private val songDao: SongDao,
    private val songPlaylistDao: SongPlaylistDao
) {
    fun execute(isAlreadyAdded: Boolean, songId: Long, selectedPlaylistId: Long) =
        flow<DataState<SongWithFavourite?>> {

            try {
                if (isAlreadyAdded) {
                    songPlaylistDao.deleteSongPlaylistFromFavorites(listOf(songId))
                } else {
                    songPlaylistDao.insertSongPlaylist(
                        SongPlaylist(
                            songId = songId,
                            playlistId = selectedPlaylistId
                        )
                    )
                }
                val song = songDao.getSpecificSong(songId).first()
                emit(
                    DataState.data(
                        data = song
                    )
                )
            } catch (ex: Exception) {
                emit(
                    DataState.error(
                        message = GenericMessageInfo.Builder()
                            .id("AddRemoveSongFromToPlaylist")
                            .title(context.getString(R.string.error)).description(ex.toString())
                    )
                )
            }
        }
}