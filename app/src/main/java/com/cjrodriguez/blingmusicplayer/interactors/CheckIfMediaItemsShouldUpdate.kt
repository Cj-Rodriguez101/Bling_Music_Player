package com.cjrodriguez.blingmusicplayer.interactors

import android.content.Context
import android.provider.MediaStore
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.util.DataState
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.cjrodriguez.blingmusicplayer.util.NegativeAction
import com.cjrodriguez.blingmusicplayer.util.PositiveAction
import com.cjrodriguez.blingmusicplayer.util.UIComponentType
import com.cjrodriguez.blingmusicplayer.util.Util
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CheckIfMediaItemsShouldUpdate @Inject constructor(
    private val context: Context,
    private val songDao: SongDao
) {

    fun execute(isPermanentlyDeclined: Boolean): Flow<DataState<Boolean>> = flow {

        var shouldUpdateMusicList = false
        try {
            if (!Util.checkReadPermission(context) && isPermanentlyDeclined) {
                emit(
                    DataState.error(
                        message = GenericMessageInfo.Builder()
                            .id("CheckIfMediaItemsShouldUpdate")
                            .uiComponentType(UIComponentType.Dialog)
                            .extraMessage("FirstPermission")
                            .title(context.getString(R.string.info))
                            .description(context.getString(R.string.must_grant_read_permission))
                            .positive(
                                positiveAction = PositiveAction(
                                    positiveBtnTxt = context.getString(
                                        R.string.request
                                    )
                                )
                            )
                            .negative(
                                negativeAction = NegativeAction(
                                    negativeBtnTxt = context.getString(
                                        R.string.close
                                    )
                                )
                            )
                    )
                )
                return@flow
            }
            if (!Util.checkReadPermission(context) && !isPermanentlyDeclined) {
                emit(
                    DataState.error(
                        message = GenericMessageInfo.Builder()
                            .id("CheckIfMediaItemsShouldUpdate")
                            .uiComponentType(UIComponentType.Dialog)
                            .extraMessage("PermanentlyDeclinedPermission")
                            .title(context.getString(R.string.info)).description(
                                context.getString(R.string.it_seems_you_permanently_declined_storage) + " " +
                                        context.getString(R.string.you_can_go_to_the_app_settings_to_grant_it)
                            )
                            .positive(
                                positiveAction = PositiveAction(
                                    positiveBtnTxt = context.getString(
                                        R.string.ok
                                    )
                                )
                            )
                    )
                )
                return@flow
            }

            val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val projection = arrayOf(MediaStore.Audio.Media.DATE_MODIFIED)
            val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

            val query = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            query?.use { cursor ->

                if (cursor.moveToFirst()) {
                    val dateModifiedColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                    val dateModified = cursor.getLong(dateModifiedColumn) * 1000

                    songDao.getLastSong().first()?.let { lastSong ->
                        shouldUpdateMusicList = (dateModified > lastSong.updatedAt)
                    }
                }
            }

            emit(DataState.data(data = shouldUpdateMusicList))
        } catch (ex: Exception) {
            emit(
                DataState.error(
                    message = GenericMessageInfo.Builder()
                        .id("CheckIfMediaItemsShouldUpdate")
                        .title(context.getString(R.string.error)).description(ex.toString())
                )
            )
        }
    }
}