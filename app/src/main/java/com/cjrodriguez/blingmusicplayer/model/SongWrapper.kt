package com.cjrodriguez.blingmusicplayer.model

data class SongWrapper(
    val song: Song,
    val isFavourite: Int,
    val isSelectedSong: Boolean = false,
    val isCurrentlyPlaying: Boolean = false
)
