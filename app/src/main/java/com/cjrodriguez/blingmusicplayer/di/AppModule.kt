package com.cjrodriguez.blingmusicplayer.di

import android.content.Context
import com.cjrodriguez.blingmusicplayer.BaseApplication
import com.cjrodriguez.blingmusicplayer.datastore.SettingsDataStore
import com.cjrodriguez.blingmusicplayer.interactors.PlayingStateIndicator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): BaseApplication {
        return app as BaseApplication
    }

    @Singleton
    @Provides
    fun provideSettingsDatastore(baseApplication: BaseApplication): SettingsDataStore {
        return SettingsDataStore(baseApplication)
    }

    @Singleton
    @Provides
    fun providePlayingStateIndicator(): PlayingStateIndicator{
        return PlayingStateIndicator
    }
}