package com.cjrodriguez.blingmusicplayer.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cjrodriguez.blingmusicplayer.BaseApplication
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore
@Inject
constructor(private val application: BaseApplication) {
    private val firstTimePermissionKey = stringPreferencesKey("check_permission_first")
    private val lastPlayedSongKey = longPreferencesKey("lastPlayedKey")
    private val isShuffledKey = booleanPreferencesKey("isShuffleKey")
    private val isRepeatKey = booleanPreferencesKey("isRepeatKey")
    private val shouldUpdateKey = booleanPreferencesKey("shouldUpdateKey")

    val firstTimePermissionFlow: Flow<String> = application.applicationContext.dataStore.data
        .map { preferences ->
            preferences[firstTimePermissionKey] ?: ""
        }.flowOn(IO)

    private val lastPlayedSongFlow: Flow<Long> = application.applicationContext.dataStore.data
        .map { preferences ->
            preferences[lastPlayedSongKey] ?: 0L
        }.flowOn(IO)

    val isShuffleFlow: Flow<Boolean> = application.applicationContext.dataStore.data
        .map { preferences ->
            preferences[isShuffledKey] ?: false
        }.flowOn(IO)

    val shouldRepeatFlow: Flow<Boolean> = application.applicationContext.dataStore.data
        .map { preferences ->
            preferences[isRepeatKey] ?: false
        }.flowOn(IO)

    val shouldUpdateFlow: Flow<Boolean> = application.applicationContext.dataStore.data
        .map { preferences ->
            preferences[shouldUpdateKey] ?: false
        }.flowOn(IO)

    fun writeFirstTimePermission(isAccepted: String) {
        runBlocking {
            application.applicationContext.dataStore.edit { settings ->
                settings[firstTimePermissionKey] = isAccepted
            }
        }
    }

    fun writeLastPlayedSongId(songID: Long) {
        runBlocking {
            application.applicationContext.dataStore.edit { settings ->
                settings[lastPlayedSongKey] = songID
            }
        }
    }

    fun writeIsShuffle(isShuffle: Boolean) {
        runBlocking {
            application.applicationContext.dataStore.edit { settings ->
                settings[isShuffledKey] = isShuffle
            }
        }
    }

    fun writeIsRepeat(isRepeat: Boolean) {
        runBlocking {
            application.applicationContext.dataStore.edit { settings ->
                settings[isRepeatKey] = isRepeat
            }
        }
    }

    fun writeShouldUpdate(shouldUpdate: Boolean) {
        runBlocking {
            application.applicationContext.dataStore.edit { settings ->
                settings[shouldUpdateKey] = shouldUpdate
            }
        }
    }

    fun readIfPermissionIsGranted(): Boolean{
        var isPermissionGranted = false
        runBlocking {
            isPermissionGranted = firstTimePermissionFlow.first() != "ACCEPT"
        }
        return isPermissionGranted
    }

    fun readLastPlayedSongId(): Long {
        var songID: Long
        runBlocking {
            songID = lastPlayedSongFlow.first()
        }
        return songID
    }

    fun readShouldUpdate(): Boolean {
        var shouldUpdate: Boolean
        runBlocking {
            shouldUpdate = shouldUpdateFlow.first()
        }
        return shouldUpdate
    }
}