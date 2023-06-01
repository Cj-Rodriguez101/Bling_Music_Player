package com.cjrodriguez.blingmusicplayer.interactors

import kotlinx.coroutines.flow.MutableStateFlow

object PlayingStateIndicator
{
    val isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
}