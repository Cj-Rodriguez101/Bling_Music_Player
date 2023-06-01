package com.cjrodriguez.blingmusicplayer.interactors

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.dataSource.cache.PlayListDao
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongPlaylistDao
import com.cjrodriguez.blingmusicplayer.model.Playlist
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.util.ART_WORK_URI
import com.cjrodriguez.blingmusicplayer.util.DataState
import com.cjrodriguez.blingmusicplayer.util.FAVOURITES
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.cjrodriguez.blingmusicplayer.util.NegativeAction
import com.cjrodriguez.blingmusicplayer.util.PositiveAction
import com.cjrodriguez.blingmusicplayer.util.UIComponentType
import com.cjrodriguez.blingmusicplayer.util.Util
import com.cjrodriguez.blingmusicplayer.util.getMediaItemWithMetaData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllSongs @Inject constructor(
    private val context: Context,
    private val songDao: SongDao,
    private val playListDao: PlayListDao, private val songPlaylistDao: SongPlaylistDao
) {

    fun getAndStoreMusic(isPermanentlyDeclined: Boolean): Flow<DataState<List<Song>>> = flow {

        emit(DataState.loading())

        try {
            if (!Util.checkReadPermission(context) && isPermanentlyDeclined) {
                emit(
                    DataState.error(
                        message = GenericMessageInfo.Builder()
                            .id("GetAllSongs").uiComponentType(UIComponentType.Dialog)
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
                            .id("GetAllSongs").uiComponentType(UIComponentType.Dialog)
                            .extraMessage("PermanentlyDeclinedPermission")
                            .title(context.getString(R.string.info)).description(
                                context.getString(R.string.it_seems_you_permanently_declined_storage) +" "+
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
            val retrievedSongs: ArrayList<Song> = arrayListOf()
            val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM
            )
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val query = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            val artworkUri = Uri.parse(ART_WORK_URI)

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val artistColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val titleColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val albumIdColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                while (cursor.moveToNext()) {
                    // Get values of columns for a given audio file
                    val id = cursor.getLong(idColumn)
                    val artist = cursor.getString(artistColumn)
                    val title = cursor.getString(titleColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val duration = cursor.getLong(durationColumn)
                    val dateModified = cursor.getLong(dateModifiedColumn) * 1000
                    val albumId = cursor.getLong(albumIdColumn)
                    val album = cursor.getString(albumColumn)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    retrievedSongs.add(
                        Song(
                            id = id,
                            artist = artist,
                            title = title,
                            sortedUnSpacedTitle = title.uppercase().trim(),
                            displayName = displayName,
                            data = contentUri.toString(),
                            duration = duration,
                            updatedAt = dateModified,
                            albumId = ContentUris.withAppendedId(artworkUri, albumId).toString(),
                            mediaItem = getMediaItemWithMetaData(
                                album = album, title = title,
                                artist = artist, albumId = ContentUris.withAppendedId(
                                    artworkUri,
                                    albumId
                                ).toString(), data = contentUri.toString(),
                                sortedUnSpacedTitle = title.uppercase().trim(), id = id.toString()
                            )
                        )
                    )
                }
            }
            retrievedSongs.sortBy {
                it.title
            }

            //only support favourite playlist for now
            if (playListDao.getCountOfPlayList() == 0L) {
                playListDao.insertPlaylist(Playlist(0L, FAVOURITES))
            }
            val idsToBeDeletedFromPlaylist =
                songDao.getSongIdListToBeDeleted(retrievedSongs.map { it.id })
            if (idsToBeDeletedFromPlaylist.isNotEmpty()) {
                songPlaylistDao.deleteSongPlaylistFromFavorites(idsToBeDeletedFromPlaylist)
            }
            songDao.deleteAllSongs()
            songDao.insertOrUpdateSongs(*retrievedSongs.toTypedArray())

            emit(DataState.data(data = songDao.getAllSongs().first()))
        } catch (ex: Exception) {
            emit(
                DataState.error(
                    message = GenericMessageInfo.Builder()
                        .id("GetAllSongs")
                        .title(context.getString(R.string.error)).description(ex.toString())
                )
            )
        }
    }
}