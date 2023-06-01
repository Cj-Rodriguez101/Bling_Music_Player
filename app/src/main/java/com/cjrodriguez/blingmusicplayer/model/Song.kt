package com.cjrodriguez.blingmusicplayer.model

import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cjrodriguez.blingmusicplayer.util.getMediaItemWithMetaData
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "songCacheTable")
data class Song(
    @PrimaryKey val id: Long = 0L,
    val title: String = "",
    val sortedUnSpacedTitle: String = "",
    val displayName: String = "",
    val artist: String = "",
    val data: String = "",
    val album: String = "",
    val genre: String = "",
    val albumId: String = "",
    val duration: Long = 0L,
    val updatedAt: Long = 0L,
    val mediaItem: MediaItem = MediaItem.EMPTY
): Parcelable{

    constructor(parcel: Parcel): this(
        id = parcel.readLong(),
        title = parcel.readString()?:"",
        artist = parcel.readString()?:"",
        data = parcel.readString()?:"",
        album = parcel.readString()?:"",
        genre = parcel.readString()?:"",
        albumId = parcel.readString()?:"",
        duration = parcel.readLong(),
        updatedAt = parcel.readLong(),
        mediaItem = getMediaItemWithMetaData(
            album = parcel.readString()?:"",
            title = parcel.readString()?:"",
            artist = parcel.readString()?:"",
            albumId = parcel.readString()?:"",
            data = parcel.readString()?:"",
            sortedUnSpacedTitle = parcel.readString()?:"",
            id = parcel.readString()?:""
        )
    )

    companion object : Parceler<Song> {

        override fun Song.write(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(title)
            parcel.writeString(artist)
            parcel.writeString(data)
            parcel.writeString(album)
            parcel.writeString(genre)
            parcel.writeString(albumId)
            parcel.writeLong(duration)
            parcel.writeLong(updatedAt)
            parcel.writeString(mediaItem.mediaId)
        }

        override fun create(parcel: Parcel): Song {
            return Song(id = parcel.readLong(),
                title = parcel.readString()?:"",
                artist = parcel.readString()?:"",
                data = parcel.readString()?:"",
                album = parcel.readString()?:"",
                genre = parcel.readString()?:"",
                albumId = parcel.readString()?:"",
                duration = parcel.readLong(),
                updatedAt = parcel.readLong(),
                mediaItem = getMediaItemWithMetaData(
                    album = parcel.readString()?:"",
                    title = parcel.readString()?:"",
                    artist = parcel.readString()?:"",
                    albumId = parcel.readString()?:"",
                    data = parcel.readString()?:"",
                    sortedUnSpacedTitle = parcel.readString()?:"",
                    id = parcel.readString()?:""
                )
            )
        }

    }
}
