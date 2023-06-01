package com.cjrodriguez.blingmusicplayer.model

import androidx.room.Embedded

data class SongWithFavourite(
    @Embedded
    val song: Song,
    val isFavourite: Int,
)
