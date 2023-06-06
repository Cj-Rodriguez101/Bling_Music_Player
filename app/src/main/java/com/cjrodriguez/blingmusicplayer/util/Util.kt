package com.cjrodriguez.blingmusicplayer.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.model.SongWrapper
import java.util.concurrent.TimeUnit

const val FAVOURITES = "Favourites"
const val ART_WORK_URI = "content://media/external/audio/albumart"
const val SORTED_SPACED_TITLE = "sortedUnSpacedTitle"
const val ACCEPT = "ACCEPT"
const val REJECT = "REJECT"
object Util {

    fun checkReadPermission(context: Context): Boolean {

        return when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT < 32) Manifest.permission.READ_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_AUDIO
            ) -> {
                // You can use the API that requires the permission.
                true
            }

            else -> {
                false
            }
        }
    }

    fun convertMillisecondLongToFormattedTimeString(milliseconds: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%d:%02d", minutes, seconds)
    }
}

fun getMediaItemWithMetaData(
    album: String, title: String, artist: String,
    albumId: String, data: String, sortedUnSpacedTitle: String, id: String
): MediaItem {

    val metadata =
        MediaMetadata.Builder()
            .setAlbumTitle(album)
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(Uri.parse(albumId))
            .setExtras(bundleOf(Pair("sortedUnSpacedTitle", sortedUnSpacedTitle), Pair("id", id)))
            .build()
    val item = MediaItem.Builder()
        .setMediaId(data)
        .setMediaMetadata(metadata)
        .setUri(Uri.parse(data))
        .build()
    return item
}

fun SongWithFavourite.toSongWrapper(): SongWrapper{
    return SongWrapper(this.song, isFavourite = isFavourite)
}

fun SongWrapper.toSongFavourite(): SongWithFavourite{
    return SongWithFavourite(this.song, isFavourite = isFavourite)
}

fun Player.prepareAndPlay(){
    if(this.playbackState == Player.STATE_IDLE){
        this.prepare()
    }
    this.play()
}
