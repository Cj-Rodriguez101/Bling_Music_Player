package com.cjrodriguez.blingmusicplayer.util

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import com.cjrodriguez.blingmusicplayer.datastore.SettingsDataStore
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

class MediaContentObserver @Inject constructor(
    handler: Handler = Handler(Looper.getMainLooper()),
    applicationContext: Context
) : ContentObserver(handler) {

    var settingsDataStore: SettingsDataStore

    init {
        val factory = EntryPointAccessors.fromApplication(
            applicationContext,
            MediaContentInterface::class.java
        )
        settingsDataStore = factory.settingsDataStore
    }

    override fun onChange(selfChange: Boolean) {
        settingsDataStore.writeShouldUpdate(true)
        super.onChange(selfChange)
    }
}