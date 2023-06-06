package com.cjrodriguez.blingmusicplayer.presentation.screens.songList

import android.content.res.TypedArray
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.palette.graphics.Palette
import com.cjrodriguez.blingmusicplayer.BaseApplication
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.datastore.SettingsDataStore
import com.cjrodriguez.blingmusicplayer.interactors.PlayingStateIndicator
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.presentation.screens.songList.components.adjustAlpha
import com.cjrodriguez.blingmusicplayer.presentation.screens.songList.events.SongListEvents
import com.cjrodriguez.blingmusicplayer.presentation.screens.songList.events.SongListEvents.*
import com.cjrodriguez.blingmusicplayer.presentation.theme.Purple80
import com.cjrodriguez.blingmusicplayer.repository.SongRepository
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@androidx.media3.common.util.UnstableApi
@HiltViewModel
class SongViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val settingsDataStore: SettingsDataStore,
    private val playingStateIndicator: PlayingStateIndicator,
    private val baseApplication: BaseApplication
) : ViewModel() {

    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _currentSong: MutableStateFlow<SongWithFavourite?> = MutableStateFlow(null)
    val currentSong: StateFlow<SongWithFavourite?> = _currentSong.asStateFlow()

    private val _shouldRepeat: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val shouldRepeat: StateFlow<Boolean> = _shouldRepeat.asStateFlow()

    private val _isSliderDragged: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSliderDragged: StateFlow<Boolean> = _isSliderDragged.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isPermissionGranted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted

    private val _currentVolume: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentVolume: StateFlow<Int> = _currentVolume

    private val _messageSet: MutableStateFlow<Set<GenericMessageInfo>> = MutableStateFlow(setOf())

    val messageSet: StateFlow<Set<GenericMessageInfo>> = _messageSet

    private val _nextSong: MutableStateFlow<SongWithFavourite?> = MutableStateFlow(null)
    val nextSong: StateFlow<SongWithFavourite?> = _nextSong

    private val _previousSong: MutableStateFlow<SongWithFavourite?> = MutableStateFlow(null)
    val previousSong: StateFlow<SongWithFavourite?> = _previousSong

    val isPlaying: StateFlow<Boolean> = playingStateIndicator.isPlaying.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        false
    )

    private val _isShuffle: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle

    private val _currentPosition: MutableStateFlow<Float> = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> = _currentPosition.asStateFlow()

    private val _globalColorBackgroundInt: MutableStateFlow<Int?> = MutableStateFlow(null)
    val globalColorBackgroundInt = _globalColorBackgroundInt.asStateFlow()

    private val _isButtonsLight: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isButtonsLight = _isButtonsLight.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val songsPagingFlow: Flow<PagingData<SongWithFavourite>> =
        _query.flatMapLatest {
            songRepository.getSearchedSongs(it)
        }.cachedIn(viewModelScope)

    private fun updateQuery(query: String) {
        _query.value = query
    }

    fun updateSliderDragged(isSliderDragged: Boolean) {
        _isSliderDragged.value = isSliderDragged
    }

    init {
        viewModelScope.launch {

            launch {
                settingsDataStore.firstTimePermissionFlow.collectLatest {
                    _isPermissionGranted.value = it != "ACCEPT"
                }
            }

            launch {
                settingsDataStore.isShuffleFlow.collectLatest {
                    _isShuffle.value = it
                    currentSong.value?.let { song ->
                        _previousSong.value =
                            songRepository.getPreviousSong(
                                song.song.sortedUnSpacedTitle,
                                song.song.id
                            )
                                .first()
                        _nextSong.value = songRepository.getNextSong(
                            song.song.sortedUnSpacedTitle,
                            song.song.id,
                            isShuffle.value
                        ).first()

                    }
                }
            }

            launch {
                _currentSong.collectLatest { song ->
                    song?.let {

                        setBackgroundColorBasedOnCurrentSong(song)

                        _previousSong.value =
                            songRepository.getPreviousSong(it.song.sortedUnSpacedTitle, it.song.id)
                                .first()
                        _nextSong.value = songRepository.getNextSong(
                            it.song.sortedUnSpacedTitle,
                            it.song.id,
                            isShuffle.value
                        ).first()
                    }
                }
            }

            launch {
                settingsDataStore.shouldUpdateFlow.collectLatest { shouldUpdate ->
                    if (shouldUpdate) {
                        onTriggerEvent(GetSongs)
                        settingsDataStore.writeShouldUpdate(false)
                    }
                }
            }

            launch {
                settingsDataStore.shouldRepeatFlow.collectLatest {
                    _shouldRepeat.value = it
                }
            }
        }

        checkIfMediaItemsShouldUpdate(!settingsDataStore.readShouldUpdate())
        updateCurrentSong()
    }

    private fun setBackgroundColorBasedOnCurrentSong(song: SongWithFavourite) {
        _globalColorBackgroundInt.value = try {
            var colorToReturn: Int? = null
            val map = MediaStore.Images.Media.getBitmap(
                baseApplication.applicationContext.contentResolver,
                Uri.parse(song.song.albumId)
            )
            map?.let { ap ->
                Palette.from(ap).generate().dominantSwatch?.let { swatch ->
                    colorToReturn = adjustAlpha(swatch.rgb, 2f)
                }
            }
            colorToReturn
        } catch (ex: Exception) {
            null
        }

        val primaryColorIfExist = if (DynamicColors.isDynamicColorAvailable()) {
            val dynamicColorContext = DynamicColors.wrapContextIfAvailable(
                baseApplication.applicationContext,
                R.style.Theme_BlingMusicPlayer
            )

            val attrsToResolve = intArrayOf(
                android.R.attr.colorPrimary
            )

            val ta: TypedArray =
                dynamicColorContext.obtainStyledAttributes(attrsToResolve)
            val primary = ta.getColor(0, 0)
            ta.recycle()
            primary
        } else {
            Purple80.toArgb()
        }

        val nullableColor = if (_globalColorBackgroundInt.value != null) {
            _globalColorBackgroundInt.value
        } else {
            primaryColorIfExist
        }

        _isButtonsLight.value = nullableColor?.let {
            ColorUtils.calculateLuminance(
                Color(it).toArgb()
            ) > 0.5
        } ?: true
    }

    private fun checkIfMediaItemsShouldUpdate(isPermanentlyDeclined: Boolean) {
        viewModelScope.launch {
            songRepository.checkReadPermission(isPermanentlyDeclined).collectLatest { dataState ->

                dataState.data?.let { shouldUpdate ->
                    if (shouldUpdate) {
                        settingsDataStore.writeShouldUpdate(true)
                    }
                }

                dataState.message?.let { error ->
                    appendToMessageQueue(error)
                }
            }
        }
    }

    private fun updateCurrentSong() {
        val longId = settingsDataStore.readLastPlayedSongId()
        if (longId != 0L) {
            viewModelScope.launch {
                CoroutineScope(Dispatchers.IO).launch {
                    songRepository.getSingleSong(longId).collectLatest { dataState ->

                        setCurrentSong(dataState.data)

                        dataState.message?.let { error ->
                            appendToMessageQueue(error)
                        }

                    }
                }
            }
        }
    }

    fun getAndUpdateCurrentSongIfExists(songId: Long) {
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch {
                songRepository.getSingleSong(songId).collectLatest { dataState ->
                    dataState.data?.let {
                        withContext(Dispatchers.Main) {
                            _currentSong.value = it
                            settingsDataStore.writeLastPlayedSongId(it.song.id)
                        }
                    }
                }
            }
        }
    }

    private fun deleteSong(
        songList: List<Song>,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch {
                songRepository.deleteSong(songList, launcher).collectLatest { dataState ->

                    dataState.data?.let {
                        if (it) {
                            onTriggerEvent(GetSongs)
                        }
                    }
                    dataState.message?.let { error ->
                        appendToMessageQueue(error)
                    }
                }
            }
        }
    }

    private fun completeDeleteIfDialogComplete(
        songList: List<Song>,
    ) {
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch {
                songRepository.completeDeleteIfDialogComplete(songList).collectLatest { dataState ->
                    dataState.message?.let { error ->
                        appendToMessageQueue(error)
                        onTriggerEvent(GetSongs)
                    }
                }
            }
        }
    }

    private fun setShouldUpdate(shouldUpdate: Boolean) {
        settingsDataStore.writeShouldUpdate(shouldUpdate)
    }

    private fun updateIsPlaying(isPlaying: Boolean) {
        playingStateIndicator.isPlaying.value = isPlaying
    }

    fun updateCurrentVolume(currentVolume: Int) {
        _currentVolume.value = currentVolume
    }

    private fun updateIsShuffle(isShuffle: Boolean) {
        settingsDataStore.writeIsShuffle(isShuffle)
    }

    private fun updateShouldRepeat(shouldRepeat: Boolean) {
        settingsDataStore.writeIsRepeat(shouldRepeat)
    }

    fun onTriggerEvent(events: SongListEvents) {
        when (events) {

            GetSongs -> {
                updateSongs()
            }

            is DeleteSong -> {
                deleteSong(events.songList, events.launcher)
            }

            is CompleteDeleteSong -> {
                completeDeleteIfDialogComplete(events.songList)
            }

            is SetUnSetFavourite -> {
                setUnFavourite(events.song)
            }

            is UpdateShouldUpdate -> {
                setShouldUpdate(events.shouldUpdate)
            }

            is UpdateIsPlaying -> {
                updateIsPlaying(events.isPlaying)
            }

            is UpdateIsShuffle -> {
                updateIsShuffle(events.isShuffle)
            }

            is UpdateIsRepeat -> {
                updateShouldRepeat(events.shouldRepeat)
            }

            is SetCurrentSong -> {
                setCurrentSong(events.song)
            }

            is SetIsAcceptedPermission -> {
                settingsDataStore.writeFirstTimePermission(events.isFirstPermission)
            }

            is UpdateCurrentSeekPosition -> {
                updatePosition(events.position)
            }

            is UpdateQuery -> {
                updateQuery(events.query)
            }

            is UpdateCurrentVolume -> {
                updateCurrentVolume(events.currentVolume)
            }

            OnRemoveHeadMessageFromQueue -> {
                removeHeadMessageFromQueue()
            }
        }
    }

    private fun setUnFavourite(song: SongWithFavourite) {
        viewModelScope.launch {
//            CoroutineScope(Dispatchers.IO).launch {
//                songRepository.setUnsetFavourite(song)
//                setCurrentSong(song)
//            }
            CoroutineScope(Dispatchers.IO).launch {
                songRepository.setUnsetFavourite(song).collectLatest { dataState ->

                    dataState.data?.let {
                        setCurrentSong(it)
                    }

                    dataState.message?.let { error ->
                        //Log.e("TAG", "newSearch: insert")
                        appendToMessageQueue(error)
                        //messageQueue.add(error.build())
                    }
                }
            }
        }
    }

    fun updatePosition(position: Float) {
        _currentPosition.value = position
    }

//    fun setPreviousAndNextSong(previousSong: Song?, nextSong: Song?){
//        _previousSong.value = previousSong
//        _nextSong.value = nextSong
//    }

    private fun updateSongs() {
        viewModelScope.launch {
            val job = CoroutineScope(Dispatchers.IO).launch {
                songRepository.updateAllSongs(
                    settingsDataStore
                        .firstTimePermissionFlow.first() == ""
                ).collectLatest { dataState ->
                    //Log.e("longId", "controller is null ${controllerStateFlow.value == null}")

                    dataState.isLoading.let {
                        _isLoading.value = it
                    }

                    dataState.message?.let { error ->
                        appendToMessageQueue(error)
                    }
                }
            }
            job.join()
            updateCurrentSong()
        }
    }

    fun setCurrentSong(song: SongWithFavourite?) {
//        viewModelScope.launch {
//            _currentSong.value = null
//            _currentSong.value = song
//            song?.let {
//                if (shouldUpdate){
//                    _previousSong.value =
//                        songRepository.getPreviousSong(it.song.sortedUnSpacedTitle, it.song.id)
//                            .first()
//                    _nextSong.value = songRepository.getNextSong(
//                        it.song.sortedUnSpacedTitle,
//                        it.song.id,
//                        isShuffle.value
//                    ).first()
//                }
//                settingsDataStore.writeLastPlayedSongId(it.song.id)
//            }
//        }
        _currentSong.value = null
        _currentSong.value = song

        song?.let {
//            _globalColorBackgroundInt.value = try {
//                var colorToReturn: Int? = null
//                val map = MediaStore.Images.Media.getBitmap(
//                    baseApplication.applicationContext.contentResolver,
//                    Uri.parse(song.song.albumId)
//                )
//                map?.let { ap ->
//                    Palette.from(ap).generate().dominantSwatch?.let { swatch ->
//                        colorToReturn = adjustAlpha(swatch.rgb, 2f)
//                    }
//                }
//                colorToReturn
//            } catch (ex: Exception) {
//                null
//            }
//
//            val primaryColorIfExist = if (DynamicColors.isDynamicColorAvailable()) {
//                val dynamicColorContext = DynamicColors.wrapContextIfAvailable(baseApplication.applicationContext, com.cjrodriguez.blingmusicplayer.R.style.Theme_BlingMusicPlayer)
//
//                val attrsToResolve = intArrayOf(
//                    android.R.attr.colorPrimary
//                )
//
//                val ta: TypedArray =
//                    dynamicColorContext.obtainStyledAttributes(attrsToResolve)
//                val primary = ta.getColor(0, 0)
//                ta.recycle() // recycle TypedArray
//                primary
//
//            } else {
//                Purple80.toArgb()
//            }
//
//           val nullableColor =  if(_globalColorBackgroundInt.value!=null){
//                _globalColorBackgroundInt.value
//            } else {
//                primaryColorIfExist
//            }
//
//            _isButtonsLight.value = nullableColor?.let { ColorUtils.calculateLuminance(
//                Color(it).toArgb()
//            ) > 0.5 }?:true

            setBackgroundColorBasedOnCurrentSong(it)
        }

        settingsDataStore.writeLastPlayedSongId(song?.song?.id ?: 0L)
//        song?.let {
//            settingsDataStore.writeLastPlayedSongId(it.song.id)
//        }
    }

    private fun appendToMessageQueue(messageInfo: GenericMessageInfo.Builder) {
        if (!_messageSet.value.contains(messageInfo.build())) {
            val currentSet = _messageSet.value.toMutableSet()
            currentSet.add(messageInfo.build())
            _messageSet.value = currentSet
        }
    }

    private fun removeHeadMessageFromQueue() {

        try {
            if (_messageSet.value.isNotEmpty()) {
                val list = _messageSet.value.toMutableList()

                if (list.isNotEmpty()) {
                    list.removeAt(list.size - 1)
                }

                _messageSet.value = if (list.isEmpty()) setOf() else list.toSet()
            }
        } catch (ex: Exception) {
            Log.e("removeMessage", ex.toString())
        }
    }
}