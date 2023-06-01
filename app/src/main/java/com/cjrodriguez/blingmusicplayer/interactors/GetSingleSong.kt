package com.cjrodriguez.blingmusicplayer.interactors

import android.content.Context
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.util.DataState
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.cjrodriguez.blingmusicplayer.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetSingleSong @Inject constructor(private val context: Context, private val songDao: SongDao) {

    fun execute(id: Long): Flow<DataState<SongWithFavourite?>> = flow {

        try {
            val singleSong = songDao.getSpecificSong(id).first()
            emit(DataState.data(data = singleSong))
        } catch (ex: Exception) {
            emit(
                DataState.error(
                    message = GenericMessageInfo.Builder().uiComponentType(UIComponentType.Dialog)
                        .id("GetSingleSong")
                        .title(context.getString(R.string.error)).description(ex.toString())
                )
            )
        }
    }
}