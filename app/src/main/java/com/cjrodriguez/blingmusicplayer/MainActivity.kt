package com.cjrodriguez.blingmusicplayer

import SongListScreen
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.media.MediaRouter
import android.media.MediaRouter.CALLBACK_FLAG_UNFILTERED_EVENTS
import android.media.MediaRouter.ROUTE_TYPE_USER
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.presentation.navigation.Screen
import com.cjrodriguez.blingmusicplayer.presentation.screens.songList.SongViewModel
import com.cjrodriguez.blingmusicplayer.services.PlaybackService
import com.cjrodriguez.blingmusicplayer.util.MediaContentObserver
import com.cjrodriguez.blingmusicplayer.util.prepareAndPlay
import com.cjrodriguez.blingmusicplayer.util.toSongWrapper
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@androidx.media3.common.util.UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) {
            try {
                controllerFuture.get()
            } catch (ex: Exception) {
                null
            }
        } else {
            null
        }

    private val viewModel: SongViewModel by viewModels()

    private var audioManager: AudioManager? = null
    private var mediaRouter: MediaRouter? = null
    private var callBack: MediaRouter.Callback? = null
    private var observer: ContentObserver? = null
    private var myNoisyAudioStreamReceiver: BroadcastReceiver? = null

    private var intentFilter: IntentFilter? = null

    private var updatePlayingState: Player.Listener? = null

    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    var mRunnable: Runnable? = null
    var mHandler: Handler? = null

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mHandler = Handler(Looper.getMainLooper())

        audioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mediaRouter =
            applicationContext.getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter

        observer = MediaContentObserver(mHandler!!, applicationContext)
        applicationContext.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer!!
        )

        intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        myNoisyAudioStreamReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    if (it.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                        controller?.pause()
                    }
                }
            }
        }

        setContent {
            val navController = rememberNavController()
            val displayVolumeDialog = remember { mutableStateOf(false) }
            val displayDeleteDialog = remember { mutableStateOf(false) }

            val currentVolume = viewModel.currentVolume.collectAsState().value
            val maxVolume = remember { mutableIntStateOf(0) }
            LaunchedEffect(Unit) {
                maxVolume.intValue = audioManager?.getStreamMaxVolume(STREAM_MUSIC) ?: 0
                viewModel.updateCurrentVolume(
                    audioManager?.getStreamVolume(STREAM_MUSIC) ?: 0
                )
            }

            audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        controller?.let {
                            controller?.prepareAndPlay()
                        }
                    }

                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        controller?.pause()
                        controller?.seekTo(0L)
                    }

                    AudioManager.AUDIOFOCUS_LOSS -> {
                        Toast.makeText(applicationContext, "FOCUS LOSS", Toast.LENGTH_SHORT).show()
                        controller?.pause()
                    }

                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        audioManager?.setStreamVolume(STREAM_MUSIC, 2, 0)
                        viewModel.updateCurrentVolume(2)
                    }

                    else -> {
                        controller?.pause()
                    }
                }
            }

            updatePlayingState = object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (controller?.playbackState != Player.STATE_BUFFERING) {
                        viewModel.updateIsPlaying(isPlaying)
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    viewModel.currentSong.value.let { currentSong ->
                        mediaItem?.let { mediaItem ->
                            if (currentSong?.song?.data != mediaItem.mediaId) {
                                viewModel
                                    .getAndUpdateCurrentSongIfExists(
                                        mediaItem
                                            .mediaId.substringAfterLast("/").toLongOrNull() ?: 0L
                                    )
                            }
                        }
                    }
                }
            }
            val itemToDelete: MutableState<Song?> = rememberSaveable {
                mutableStateOf(null)
            }
            val allSongs = viewModel.songsPagingFlow.collectAsLazyPagingItems()
            val sliderPosition by viewModel.currentPosition.collectAsState()

            val isPlaying by viewModel.isPlaying.collectAsState()
            val shouldUpdate by viewModel.shouldUpdateFlow.collectAsState()
            val currentSong by viewModel.currentSong.collectAsState()
            val nextSong by viewModel.nextSong.collectAsState()
            val previousSong by viewModel.previousSong.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val messageSet by viewModel.messageSet.collectAsState()
            val query by viewModel.query.collectAsState()
            val isShuffle by viewModel.isShuffle.collectAsState()
            val shouldRepeat by viewModel.shouldRepeat.collectAsState()
            NavHost(navController = navController, startDestination = Screen.SongList.route) {
                composable(route = Screen.SongList.route) {

                    SongListScreen(
                        onTriggerEvent = viewModel::onTriggerEvent,
                        isPlaying = isPlaying,
                        shouldUpdate = shouldUpdate,
                        currentSongMediaItem = currentSong?.toSongWrapper(),
                        nextSongMediaItem = nextSong,
                        prevSongMediaItem = previousSong,
                        itemSongs = allSongs,
                        isLoading = isLoading,
                        messageSet = messageSet,
                        query = query,
                        controller = controller,
                        isShuffle = isShuffle,
                        shouldRepeat = shouldRepeat,
                        onUpdateSliderPosition = {
                            viewModel.updatePosition(it.toFloat())
                        },
                        sliderPosition = sliderPosition,
                        requestFocusAndPlay = { pos ->

                            controller?.let {

                                viewModel.currentSong.value?.let {
                                    //if (pos != null) {
                                    controller?.setMediaItem(it.song.mediaItem)
                                    //} else {
                                    controller?.setMediaItem(
                                        it.song.mediaItem,
                                        pos
                                    )
                                    //}
                                    Log.e(
                                        "oppp",
                                        "pos ${pos.toString()} slider ${sliderPosition.toString()}"
                                    )
//                                    controller?.setMediaItem(
//                                        it.song.mediaItem,
//                                        sliderPosition.toLong()
//                                    )
                                    controller?.prepareAndPlay()
                                    viewModel.updateIsPlaying(true)
                                }
                            }
                        },
                        abandonFocusAndPause = {
                            controller?.let {

                                it.pause()
                                viewModel.updateIsPlaying(false)
                            }
                        },
                        dialogVolumeState = displayVolumeDialog,
                        dialogDeleteState = displayDeleteDialog,
                        openVolumeDialog = {
                            displayVolumeDialog.value = true
                        },
                        openDeleteDialog = {
                            displayDeleteDialog.value = true
                        },
                        updateSliderDraggedState = {
                            viewModel.updateSliderDragged(it)
                        },
                        currentVolume = currentVolume.toFloat(),
                        maxVolume = maxVolume.intValue.toFloat(),
                        updateSeekBar = {
                            audioManager?.setStreamVolume(STREAM_MUSIC, it.toInt(), 0)
                            viewModel.updateCurrentVolume(it.toInt())
                        },
                        itemToDelete = itemToDelete
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    private fun initializeController() {
        controllerFuture =
            MediaController.Builder(
                this,
                SessionToken(this, ComponentName(this, PlaybackService::class.java))
            ).buildAsync()
        controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }

    private fun setController() {
        val controller = this.controller ?: return
        updatePlayingState?.let {
            controller.addListener(it)
        }

        viewModel.updateIsPlaying(controller.isPlaying ?: false)

        lifecycleScope.launch {
//            viewModel.isPlaying.collectLatest { isPlaying ->
//                if (isPlaying) {
//                    mRunnable = object : Runnable {
//                        override fun run() {
//                            if (!viewModel.isSliderDragged.value) {
//                                controller.currentPosition.let { pos ->
//                                    //Log.e("firsthere", pos.toString()+"sliderPosition ${viewModel.currentPosition.value}")
//                                    val position = viewModel.currentPosition.value
//                                    pos.let {
//                                        viewModel.updatePosition(if (pos <= 0L) position else it.toFloat())
//                                    }
//                                }
//                            }
//                            mHandler?.postDelayed(this, 1000)
//                        }
//                    }
//                    mRunnable?.run()
//                } else {
//                    mRunnable?.let {
//                        mHandler?.removeCallbacks(it)
//                    }
//                }
//            }

            //new
            viewModel.isPlaying.collectLatest { isPlaying ->
                if (isPlaying) {
                    launch {
                        while(true){
                            if (!viewModel.isSliderDragged.value) {
                                controller.currentPosition.let { pos ->
                                    //Log.e("firsthere", pos.toString()+"sliderPosition ${viewModel.currentPosition.value}")
                                    val position = viewModel.currentPosition.value
                                    pos.let {
                                        viewModel.updatePosition(if (pos <= 0L) position else it.toFloat())
                                    }
                                }
                            }

                            delay(1000L)
                        }
                    }
                }
            }
        }



        viewModel.currentSong.value.let { currentSong ->
            controller.currentMediaItem?.let { mediaItem ->
                if (currentSong?.song?.data != mediaItem.mediaId) {
                    viewModel.getAndUpdateCurrentSongIfExists(
                        mediaItem.mediaId.substringAfterLast("/").toLongOrNull() ?: 0L
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        releaseController()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            myNoisyAudioStreamReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
        callBack = object : MediaRouter.Callback() {
            override fun onRouteSelected(
                router: MediaRouter?,
                type: Int,
                info: MediaRouter.RouteInfo?
            ) {
            }

            override fun onRouteUnselected(
                router: MediaRouter?,
                type: Int,
                info: MediaRouter.RouteInfo?
            ) {
            }

            override fun onRouteAdded(router: MediaRouter?, info: MediaRouter.RouteInfo?) {}

            override fun onRouteRemoved(router: MediaRouter?, info: MediaRouter.RouteInfo?) {}

            override fun onRouteChanged(router: MediaRouter?, info: MediaRouter.RouteInfo?) {}

            override fun onRouteGrouped(
                router: MediaRouter?,
                info: MediaRouter.RouteInfo?,
                group: MediaRouter.RouteGroup?,
                index: Int
            ) {
            }

            override fun onRouteUngrouped(
                router: MediaRouter?,
                info: MediaRouter.RouteInfo?,
                group: MediaRouter.RouteGroup?
            ) {
            }

            override fun onRouteVolumeChanged(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
                viewModel.updateCurrentVolume(audioManager?.getStreamVolume(STREAM_MUSIC) ?: 0)
            }

        }
        mediaRouter?.addCallback(ROUTE_TYPE_USER, callBack, CALLBACK_FLAG_UNFILTERED_EVENTS)

    }

    override fun onPause() {
        audioFocusChangeListener?.let {
            audioManager?.abandonAudioFocus(it)
        }
        unregisterReceiver(myNoisyAudioStreamReceiver)
        if (mHandler != null) {
            if (mRunnable != null) {
                mHandler?.removeCallbacks(mRunnable!!)
            }
        }
        mRunnable = null
        mediaRouter?.removeCallback(callBack!!)
        callBack = null
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //lifecycleScope.launch {
//            viewModel.isPlaying.collectLatest { isPlaying ->
//                if (isPlaying) {
//                    mRunnable = object : Runnable {
//                        override fun run() {
//                            if (!viewModel.isSliderDragged.value) {
//                                controller?.currentPosition.let { pos ->
//                                    //Log.e("firsthere", pos.toString()+"sliderPosition ${viewModel.currentPosition.value}")
//                                    val position = viewModel.currentPosition.value
//                                    pos?.let {
//                                        viewModel.updatePosition(if (pos <= 0L) position else it.toFloat())
//                                    }
//                                }
//                            }
//                            mHandler?.postDelayed(this, 1000)
//                        }
//                    }
//                    mRunnable?.run()
//                } else {
//                    mRunnable?.let {
//                        mHandler?.removeCallbacks(it)
//                    }
//                }
//            }


            //NEW
//            launch {
//                while(true){
//                    if (!viewModel.isSliderDragged.value) {
//                        controller?.currentPosition.let { pos ->
//                            //Log.e("firsthere", pos.toString()+"sliderPosition ${viewModel.currentPosition.value}")
//                            val position = viewModel.currentPosition.value
//                            pos?.let {
//                                viewModel.updatePosition(if (pos <= 0L) position else it.toFloat())
//                            }
//                        }
//                    }
//
//                    delay(1000L)
//                }
//            }
        //}
    }

    override fun onDestroy() {
        //looper = null
        audioManager = null
        myNoisyAudioStreamReceiver = null
        applicationContext.contentResolver.unregisterContentObserver(
            observer!!
        )
        intentFilter = null
        mHandler = null
        mRunnable = null
        observer = null
        super.onDestroy()
    }
}