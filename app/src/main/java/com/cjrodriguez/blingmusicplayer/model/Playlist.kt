package com.cjrodriguez.blingmusicplayer.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "playlistCacheTable")
data class Playlist(@PrimaryKey val id: Long, val title: String) : Parcelable
