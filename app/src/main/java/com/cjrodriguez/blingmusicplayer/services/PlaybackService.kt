package com.cjrodriguez.blingmusicplayer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getBroadcast
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.AudioAttributes
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.cjrodriguez.blingmusicplayer.MainActivity
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
import com.cjrodriguez.blingmusicplayer.datastore.SettingsDataStore
import com.cjrodriguez.blingmusicplayer.interactors.PlayingStateIndicator
import com.cjrodriguez.blingmusicplayer.util.SORTED_SPACED_TITLE
import com.cjrodriguez.blingmusicplayer.util.prepareAndPlay
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val REWIND = "REWIND"
private const val FAST_FWD = "FAST_FWD"
private const val CHANNEL_NAME = "BlingMusicChannel"
private const val CHANNEL_ID = "BlingMusicID"
private const val CHANNEL_DESCRIPTION = "BlingMusicChannel"

@androidx.media3.common.util.UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaLibraryService() {

    @Inject
    lateinit var songDao: SongDao

    @Inject
    lateinit var playingStateIndicator: PlayingStateIndicator

    @Inject
    lateinit var dataStore: SettingsDataStore

    private lateinit var modifiedPlayer: ForwardingPlayer
    private var mediaSession: MediaLibrarySession? = null
    private var playerListener: Player.Listener? = null
    private var callback: MediaSession.Callback? = null
    private lateinit var customCommands: List<CommandButton>
    private var customLayout = ImmutableList.of<CommandButton>()

    private var rewindBroadcastReceiver: BroadcastReceiver? = null
    private var playPauseBroadcastReceiver: BroadcastReceiver? = null
    private var fastForwardBroadcastReceiver: BroadcastReceiver? = null

    private var notificationManager: NotificationManager? = null

    @OptIn(
        ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
        ExperimentalMaterial3Api::class
    )
    override fun onCreate() {
        super.onCreate()
        val customCallback = CustomMediaSessionCallback()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true).build()
        customCommands = listOf(
            getRewindCommandButton(
                SessionCommand(REWIND, Bundle.EMPTY)
            ), getFastForwardCommandButton(
                SessionCommand(FAST_FWD, Bundle.EMPTY)
            )
        )
        customLayout = ImmutableList.copyOf(customCommands)
        playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (!isPlaying) {
                    if (player.playbackState == Player.STATE_ENDED) {
                        if (playingStateIndicator.isPlaying.value) {
                            CoroutineScope(Dispatchers.Main).launch {
                                val shouldRepeat = dataStore.shouldRepeatFlow.first()
                                if (!shouldRepeat) {
                                    songDao.getNextSong(
                                        player.mediaMetadata.extras?.getString(SORTED_SPACED_TITLE)
                                            .toString(), 0L, dataStore.isShuffleFlow.first()
                                    ).first().let {
                                        it?.let { song ->
                                            player.setMediaItem(song.song.mediaItem)
                                            if (player.isPlaying) {
                                                player.prepareAndPlay()
                                            }
                                        }
                                    }
                                } else {
                                    player.seekTo(0L)
                                }
                            }
                        }
                    }
                }

            }
        }
        modifiedPlayer = object : ForwardingPlayer(player) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon().remove(COMMAND_SEEK_TO_PREVIOUS)
                    .remove(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM).remove(COMMAND_SEEK_TO_NEXT)
                    .remove(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM).build()
            }
        }
        modifiedPlayer.addListener(playerListener!!)

        val sessionActivityPendingIntent =
            TaskStackBuilder.create(this).run {
                addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))

                val immutableFlag = if (Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE else 0
                getPendingIntent(0, immutableFlag or FLAG_UPDATE_CURRENT)
            }
        mediaSession =
            MediaLibrarySession.Builder(this, modifiedPlayer, customCallback)
                .setSessionActivity(sessionActivityPendingIntent!!).build()
        if (!customLayout.isEmpty()) {
            // Send custom layout to legacy session.
            mediaSession?.setCustomLayout(customLayout)
        }

        rewindBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                CoroutineScope(Dispatchers.Main).launch {
//                    songDao.getPreviousSong(
//                        player.mediaMetadata.extras?.getString("sortedUnSpacedTitle")
//                            .toString(), 0L
//                    ).collectLatest {
//                        it?.let { song ->
//                            player.setMediaItem(song.song.mediaItem)
//                            if (player.isPlaying) {
//                                if (player.playbackState == Player.STATE_IDLE) {
//                                    player.prepare()
//                                }
//                                player.play()
//                            }
//                        }
//                    }
                    songDao.getPreviousSong(
                        player.mediaMetadata.extras?.getString(SORTED_SPACED_TITLE)
                            .toString(), 0L
                    ).first()?.let {
                        it.let { song ->
                            player.setMediaItem(song.song.mediaItem)
                            if (player.isPlaying) {
                                player.prepareAndPlay()
                            }
                        }
                    }
                }
            }
        }

        playPauseBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }

        fastForwardBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                CoroutineScope(Dispatchers.Main).launch {
                    songDao.getNextSong(
                        player.currentMediaItem?.mediaMetadata?.extras?.getString(
                            SORTED_SPACED_TITLE)
                            .toString(), 0L, dataStore.isShuffleFlow.first()
                    ).first()?.let {
                        it.let { song ->
                            player.setMediaItem(song.song.mediaItem)
                            if (player.isPlaying) {
                                player.prepareAndPlay()
                            }
                        }
                    }
                }
            }
        }

        registerReceiver(rewindBroadcastReceiver, IntentFilter("REWIND"))
        registerReceiver(playPauseBroadcastReceiver, IntentFilter("PLAY"))
        registerReceiver(fastForwardBroadcastReceiver, IntentFilter("FAST_FWD"))

        setMediaNotificationProvider(object : MediaNotification.Provider {
            override fun createNotification(
                mediaSession: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                // This run every time when I press buttons on notification bar:
                return makeStatusNotification(mediaSession)
            }

            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: Bundle
            ): Boolean {
                return false
            }
        })
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, startInForegroundRequired)
        //super.onUpdateNotification(session)
        //makeStatusNotification(session)
        //if(startInForegroundRequired){
        //makeStatusNotification(session)
        //startForeground(notification.notificationId, notification.notification)
        //}
    }

//        @OptIn(ExperimentalComposeUiApi::class)
//    override fun onUpdateNotification(session: MediaSession) {
//            super.onUpdateNotification(session)
//        makeStatusNotification(session)
//    }


//    @OptIn(ExperimentalComposeUiApi::class)
//    @SuppressLint("UnsafeOptInUsageError")
//    private fun updateNotification(session: MediaSession): MediaNotification {
//        return makeStatusNotification(session)
//    }


    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @ExperimentalComposeUiApi
    @androidx.media3.common.util.UnstableApi
    fun makeStatusNotification(session: MediaSession): MediaNotification {

        // Make a channel if necessary
        val context = applicationContext
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            //val description = "ITEM_TRACKER_CHANNEL_DESCRIPTION"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.description = CHANNEL_DESCRIPTION

            notificationManager?.createNotificationChannel(channel)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

//        val pendingIntent =
//            TaskStackBuilder.create(this@PlaybackService).run {
//                addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))
//
//                val immutableFlag = if (Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE else 0
//                getPendingIntent(0, immutableFlag or FLAG_UPDATE_CURRENT)
//            }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(
                try {
                    session.player.mediaMetadata.artworkUri?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(
                                    applicationContext.contentResolver,
                                    it
                                )
                            )
                        } else {
                            MediaStore.Images.Media.getBitmap(
                                applicationContext.contentResolver,
                                it
                            )
                        }
                    }
                } catch (ex: Exception) {
                    Bitmap.createBitmap(
                        context.resources.getDrawable(R.drawable.music_item_icon).toBitmap()
                    )
                }
            )
            .setContentTitle(session.player.mediaMetadata.title)
            .setContentText(session.player.mediaMetadata.artist)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1, 2)
            ).setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pendingIntent)
            .setOngoing(session.player.isPlaying).addAction(
                R.drawable.skip_backward,
                getString(R.string.previous),
                getBroadcast(context, 4, Intent(REWIND),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
            ).addAction(
                if (!session.player.isPlaying && session.player.playbackState
                    != Player.STATE_BUFFERING) R.drawable.baseline_play_arrow_24
                else androidx.media3.ui.R.drawable.exo_icon_pause,
                getString(R.string.play) + "/" + getString(R.string.pause),
                getBroadcast(context, 5, Intent("PLAY"),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
            ).addAction(
                R.drawable.skip_forward,
                getString(R.string.next),
                getBroadcast(context, 6, Intent(FAST_FWD)
                    , FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
            )
            .setAutoCancel(false)

        return MediaNotification(1, builder.build())
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //super.onTaskRemoved(rootIntent)
        mediaSession?.let { session ->
//            if (!session.player.playWhenReady) {
//                stopSelf()
//            }
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.removeListener(playerListener!!)
            player.release()
            release()
            mediaSession = null
        }
        callback = null
        unregisterReceiver(rewindBroadcastReceiver)
        unregisterReceiver(playPauseBroadcastReceiver)
        unregisterReceiver(fastForwardBroadcastReceiver)

        rewindBroadcastReceiver = null
        playPauseBroadcastReceiver = null
        fastForwardBroadcastReceiver = null
        notificationManager = null
        super.onDestroy()
    }


    private inner class CustomMediaSessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession, controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands = connectionResult.availableSessionCommands.buildUpon()

            customCommands.forEach { commandButton ->
                commandButton.sessionCommand?.let { sessionCommands.add(it) }
            }
            return MediaSession.ConnectionResult.accept(
                sessionCommands.build(), connectionResult.availablePlayerCommands
            )
        }

//        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
//            if (!customLayout.isEmpty()) {
//                //mediaSession?.setCustomLayout(customLayout)
//                //mediaSession?.setCustomLayout(controller, customLayout)
////                 Let Media3 controller (for instance the MediaNotificationProvider) know about the custom
////                 layout right after it connected.
//                //ignoreFuture(mediaSession?.setCustomLayout(controller, customLayout))
//            }
//        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == REWIND) {
                val player = session.player

                CoroutineScope(Dispatchers.Main).launch {
//                    songDao.getPreviousSong(player.mediaMetadata.title.toString(), 0L)
//                        .collectLatest {

                    songDao.getPreviousSong(
                        player.mediaMetadata.extras?.getString("sortedUnSpacedTitle")
                            .toString(), 0L
                    ).first()?.let {
                        it.let { song ->
                            player.setMediaItem(song.song.mediaItem)
                            if (player.isPlaying) {
                                player.prepareAndPlay()
                            }
                        }
                    }
                    //}
                }
            } else {
                val player = session.player
                CoroutineScope(Dispatchers.Main).launch {
                    songDao.getNextSong(
                        player.mediaMetadata.extras?.getString(SORTED_SPACED_TITLE)
                            .toString(), 0L, dataStore.isShuffleFlow.first()
                    ).first()?.let { song ->
                        player.setMediaItem(song.song.mediaItem)
                        if (player.isPlaying) {
                            player.prepareAndPlay()
                        }

                    }
                }
            }
            session.setCustomLayout(customLayout)
            return Futures.immediateFuture(
                SessionResult(SessionResult.RESULT_SUCCESS)
            )
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map {
                it.buildUpon().setUri(it.mediaId).build()
            }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }
    }

    private fun getRewindCommandButton(sessionCommand: SessionCommand): CommandButton {
        return CommandButton.Builder().setDisplayName(
            applicationContext.getString(R.string.previous)
        ).setSessionCommand(sessionCommand)
            .setIconResId(androidx.media3.ui.R.drawable.exo_ic_skip_previous).build()
    }

    private fun getFastForwardCommandButton(sessionCommand: SessionCommand): CommandButton {
        return CommandButton.Builder().setDisplayName(
            applicationContext.getString(R.string.next)
        ).setSessionCommand(sessionCommand)
            .setIconResId(androidx.media3.ui.R.drawable.exo_ic_skip_next).build()
    }
}