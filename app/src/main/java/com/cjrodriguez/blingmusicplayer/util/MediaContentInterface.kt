package com.cjrodriguez.blingmusicplayer.util

import com.cjrodriguez.blingmusicplayer.datastore.SettingsDataStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaContentInterface {
    val settingsDataStore: SettingsDataStore
}