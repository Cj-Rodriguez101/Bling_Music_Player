package com.cjrodriguez.blingmusicplayer.presentation.navigation

sealed class Screen(
    val route: String
){
    object SongList: Screen("songList")
}