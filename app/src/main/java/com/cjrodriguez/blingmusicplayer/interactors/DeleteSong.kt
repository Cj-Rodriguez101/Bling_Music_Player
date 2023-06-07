package com.cjrodriguez.blingmusicplayer.interactors

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongPlaylistDao
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.util.DataState
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.cjrodriguez.blingmusicplayer.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DeleteSong(
    private val context: Context,
    private val songDao: SongDao,
    private val playListDao: SongPlaylistDao
) {

    //Only Support Single Item Delete For Now
    fun execute(songList: List<Song>,
                launcher: ManagedActivityResultLauncher<IntentSenderRequest,
                        ActivityResult>): Flow<DataState<Boolean>> = flow {
        try {
            emit(DataState.loading())
            val uriList: List<String> = songList.map { it.data }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (uriList.isNotEmpty()) {

                    val file = File(uriList[0])
                    file.delete()
                    context.contentResolver.delete(
                        Uri.parse(uriList[0]),
                        null,
                        null
                    )
                }
                val id = songList.map { it.id }
                playListDao.deleteSongPlaylistFromFavorites(id)
                songDao.deleteSpecificSongs(id)
            } else {
                val pendingIntent = MediaStore.createDeleteRequest(
                    context.contentResolver,
                    uriList.map { Uri.parse(it) })
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                launcher.launch(intentSenderRequest)
            }
            emit(
                DataState.data(data = Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q,
                    message = GenericMessageInfo.Builder()
                        .id("DeleteSong").title(context.getString(R.string.success))
                        .uiComponentType(UIComponentType.None)
                )
            )
        } catch (ex: Exception) {
            emit(
                DataState.error(
                    message = GenericMessageInfo.Builder()
                        .id("DeleteSong")
                        .title(context.getString(R.string.error)).description(ex.toString())
                        .uiComponentType(UIComponentType.Dialog)
                )
            )
        }
    }

    fun completeDeleteIfDialogComplete(songList: List<Song>):Flow<DataState<Boolean>> = flow {
        try {
            val id = songList.map { it.id }
            playListDao.deleteSongPlaylistFromFavorites(id)
            songDao.deleteSpecificSongs(id)

            emit(
                DataState.data(
                    message = GenericMessageInfo.Builder()
                        .id("DeleteSong").title(context.getString(R.string.success))
                        .uiComponentType(UIComponentType.None)
                )
            )
        } catch (ex: Exception){
            emit(
                DataState.error(
                    message = GenericMessageInfo.Builder()
                        .id("DeleteSong")
                        .title(context.getString(R.string.error)).description(ex.toString())
                        .uiComponentType(UIComponentType.None)
                )
            )
        }
    }
}