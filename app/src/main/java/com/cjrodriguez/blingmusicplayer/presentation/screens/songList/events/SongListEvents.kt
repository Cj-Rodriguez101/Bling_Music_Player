package com.cjrodriguez.blingmusicplayer.presentation.screens.songList.events

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite

sealed class SongListEvents {

    data class SetCurrentSong(
        val song: SongWithFavourite?,
        val shouldUpdateMediaItems: Boolean = false
    ) : SongListEvents()

    data class SetUnSetFavourite(val song: SongWithFavourite) : SongListEvents()

    data class DeleteSong(
        val songList: List<Song>,
        val launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) : SongListEvents()

    data class CompleteDeleteSong(
        val songList: List<Song>
    ): SongListEvents()

    data class GetMediaItemBasedOnUri(val song: Song) : SongListEvents()

    data class SetIsAcceptedPermission(val isFirstPermission: String) : SongListEvents()

    data class UpdateCurrentSeekPosition(val position: Float) : SongListEvents()

    data class UpdateCurrentVolume(val currentVolume: Int) : SongListEvents()

    data class UpdateIsPlaying(val isPlaying: Boolean) : SongListEvents()

    data class UpdateShouldUpdate(val shouldUpdate: Boolean) : SongListEvents()

    data class UpdateIsShuffle(val isShuffle: Boolean) : SongListEvents()

    data class UpdateIsRepeat(val shouldRepeat: Boolean) : SongListEvents()

    data class UpdateQuery(val query: String) : SongListEvents()

    object GetSongs : SongListEvents()

    object OnRemoveHeadMessageFromQueue : SongListEvents()
}
