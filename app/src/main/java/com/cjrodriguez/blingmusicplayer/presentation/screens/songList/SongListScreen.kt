import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.model.SongWrapper
import com.cjrodriguez.blingmusicplayer.presentation.components.AreYouSureDeleteDialog
import com.cjrodriguez.blingmusicplayer.presentation.components.Music_Item
import com.cjrodriguez.blingmusicplayer.presentation.components.SearchAppBar
import com.cjrodriguez.blingmusicplayer.presentation.components.VolumeDialog
import com.cjrodriguez.blingmusicplayer.presentation.screens.songList.components.SingleSongScreen
import com.cjrodriguez.blingmusicplayer.presentation.screens.songList.events.SongListEvents
import com.cjrodriguez.blingmusicplayer.presentation.theme.BlingMusicPlayerTheme
import com.cjrodriguez.blingmusicplayer.util.ACCEPT
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.cjrodriguez.blingmusicplayer.util.REJECT
import com.cjrodriguez.blingmusicplayer.util.toSongFavourite
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@androidx.media3.common.util.UnstableApi
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun SongListScreen(
    isPlaying: Boolean,
    isShuffle: Boolean,
    shouldRepeat: Boolean,
    currentSongMediaItem: SongWrapper? = null,
    itemSongs: LazyPagingItems<SongWithFavourite>,
    isLoading: Boolean = false,
    globalColorBackground: Int?,
    isButtonsLight: Boolean,
    sliderPosition: Float,
    onUpdateSliderPosition: (Long) -> Unit,
    skipForward: () -> Unit,
    skipBackward: () -> Unit,
    seekToPosition: (Long) -> Unit,
    updateSliderDraggedState: (Boolean) -> Unit,
    requestFocusAndPlay: (Long) -> Unit,
    abandonFocusAndPause: () -> Unit,
    query: String,
    showVolumeDialog: () -> Unit,
    showDeleteDialog: () -> Unit,
    currentVolume: Int,
    updateVolumeSeekbarVm: (Int) -> Unit,
    updateDeviceVolume: (Int) -> Unit,
    maxVolume: Int,
    isPermissionGranted: Boolean,
    messageSet: ImmutableSet<GenericMessageInfo>,
    itemToDelete: Song?,
    setItemToDelete: (Song) -> Unit,
    dialogVolumeState: Boolean,
    hideVolumeDialog: () -> Unit,
    dialogDeleteState: Boolean,
    hideDeleteDialog: () -> Unit,
    onTriggerEvent: (SongListEvents) -> Any = {}
) {
    val context = LocalContext.current

    val snackBarHostState = remember { SnackbarHostState() }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var shouldShowSearch by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.StartIntentSenderForResult(), onResult = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (it.resultCode == RESULT_OK) {
                itemToDelete?.let { song ->
                    onTriggerEvent(SongListEvents.CompleteDeleteSong(listOf(song)))
                }
            }
        }
    })

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
            for ((key, isGranted) in permissionMap) {
                if (isGranted) {
                    if (key == Manifest.permission.READ_EXTERNAL_STORAGE || key == Manifest.permission.READ_MEDIA_AUDIO) {
                        onTriggerEvent(SongListEvents.SetIsAcceptedPermission(ACCEPT))
                        onTriggerEvent(SongListEvents.GetSongs)
                    } else {
                        //write permission to delete file
                        itemToDelete?.let {
                            onTriggerEvent(SongListEvents.DeleteSong(listOf(it), launcher))
                        }
                    }
                } else {
                    if (key == Manifest.permission.READ_EXTERNAL_STORAGE || key == Manifest.permission.READ_MEDIA_AUDIO) {
                        onTriggerEvent(SongListEvents.SetIsAcceptedPermission(REJECT))
                        (context as? Activity)?.finish()
                    } else {
                        itemToDelete?.let {
                            onTriggerEvent(SongListEvents.DeleteSong(listOf(it), launcher))
                        }
                    }
                }
            }
        }

    BlingMusicPlayerTheme(
        snackBarHostState = snackBarHostState,
        displayProgressBar = isLoading,
        getReadPermission = {
            requestPermissionLauncher.launch(
                arrayOf(
                    if (Build.VERSION.SDK_INT < 32)
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    else Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        },
        isCurrentSongPresent = currentSongMediaItem != null,
        openAppSettings = {
            (context as? Activity)?.finish()
        },
        globalColorBackground = globalColorBackground,
        useDarkIconsNew = isButtonsLight,
        messageSet = messageSet,
        onRemoveHeadMessageFromQueue = { onTriggerEvent(SongListEvents.OnRemoveHeadMessageFromQueue) }) {
        BackHandler(onBack = {
            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                collapseExpandBottomScaffold(
                    shouldCollapse = true,
                    coroutineScope = coroutineScope,
                    bottomSheetScaffoldState = bottomSheetScaffoldState
                )
            } else {
                (context as Activity).finish()
            }
        })

        if (dialogVolumeState) {
            VolumeDialog(
                onDismiss = hideVolumeDialog, maxVolume = maxVolume,
                updateVolumeSeekBarVm = updateVolumeSeekbarVm,
                updateDeviceVolume = updateDeviceVolume, volumeLevel = currentVolume
            )
        }

        if (dialogDeleteState) {
            AreYouSureDeleteDialog(onDismiss = hideDeleteDialog,
                onPositiveAction = {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    hideDeleteDialog()
                }, onNegativeAction = {
                    hideDeleteDialog()
                })
        }
        BottomSheetScaffold(
            sheetPeekHeight = if (currentSongMediaItem == null) 0.dp else 56.dp,
            sheetContent = {
                currentSongMediaItem?.let {
                    SingleSongScreen(
                        currentSong = currentSongMediaItem,
                        bottomSheetScaffoldState = bottomSheetScaffoldState,
                        sliderPosition = sliderPosition,
                        duration = currentSongMediaItem.song.duration,
                        setUnSetFavourite = {
                            onTriggerEvent(
                                SongListEvents.SetUnSetFavourite(
                                    currentSongMediaItem.toSongFavourite()
                                )
                            )
                        },
                        skipForward = skipForward,
                        skipBackward = skipBackward,
                        playOrPause = {
                            if (!isPlaying) {
                                requestFocusAndPlay(sliderPosition.toLong())
                            } else {
                                abandonFocusAndPause()
                            }
                        },
                        onUpdateSliderPosition = {
                            onUpdateSliderPosition(it.toLong())
                        },
                        seekToPosition = seekToPosition,
                        onClick = {
                            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                collapseExpandBottomScaffold(
                                    shouldCollapse = false,
                                    coroutineScope = coroutineScope,
                                    bottomSheetScaffoldState = bottomSheetScaffoldState
                                )
                            }
                        },
                        collapseExpandBottomSheet = {
                            collapseExpandBottomScaffold(
                                it,
                                coroutineScope,
                                bottomSheetScaffoldState
                            )
                        },
                        isPlaying = isPlaying, isShuffle = isShuffle, shouldRepeat = shouldRepeat,
                        updateShuffle = {
                            onTriggerEvent(
                                SongListEvents.UpdateIsShuffle(
                                    !isShuffle
                                )
                            )
                        },
                        updateRepeat = {
                            onTriggerEvent(
                                SongListEvents.UpdateIsRepeat(
                                    !shouldRepeat
                                )
                            )
                        },
                        updateSliderDraggedState = updateSliderDraggedState,
                        displayVolumeDialog = {
                            if (dialogVolumeState) {
                                hideVolumeDialog()
                            } else {
                                showVolumeDialog()
                            }
                        }, currentVolume = currentVolume,
                        globalDynamicBackgroundColor = globalColorBackground?.let { Color(it) }
                            ?: MaterialTheme.colorScheme.primary,
                        globalOnIconColor = if (isButtonsLight) Color.Black else Color.White
                    )
                }
            }, scaffoldState = bottomSheetScaffoldState
        ) {
            Scaffold(
                topBar = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        TopAppBar(title = {
                            Text(text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.bling) + " ")
                                }
                                append(stringResource(R.string.music))
                            }, fontSize = 16.sp)
                        }, actions = {
                            if (!shouldShowSearch) {
                                IconButton(onClick = { shouldShowSearch = !shouldShowSearch }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(
                                            R.string.search
                                        )
                                    )
                                }
                            }
                        })

                        if (shouldShowSearch) {
                            TopAppBar(
                                title = {},
                                modifier = Modifier.fillMaxWidth(),
                                actions = {
                                    Column(
                                        modifier = Modifier
                                            .height(48.dp)
                                            .fillMaxWidth()
                                    ) {
                                        SearchAppBar(query = query,
                                            closeSearch = {
                                                onTriggerEvent(
                                                    SongListEvents.UpdateQuery(
                                                        ""
                                                    )
                                                )
                                                shouldShowSearch = false
                                            },
                                            searchMusic = {
                                                onTriggerEvent(
                                                    SongListEvents.UpdateQuery(
                                                        it
                                                    )
                                                )
                                            })
                                    }
                                }
                            )
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackBarHostState) },
            ) { paddingValues ->
                when (itemSongs.loadState.refresh) {
                    LoadState.Loading -> {
                        Log.e("state", "loading")
                    }

                    is LoadState.Error -> {
                        Log.e("state", "error")
                    }

                    else -> {
                        when {
                            itemSongs.itemCount > 0 -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = paddingValues.calculateTopPadding(),
                                        )
                                ) {
                                    itemsIndexed(items = itemSongs, key = { _, item ->
                                        item.song.id
                                    }) { index, item ->
                                        item?.let { itemSong ->
                                            Music_Item(song = item,
                                                isSelected = currentSongMediaItem?.let { itemSong.song.id == it.song.id }
                                                    ?: false,
                                                isPlaying = isPlaying,
                                                deleteSong = {
                                                    setItemToDelete(itemSong.song)
                                                    showDeleteDialog()
                                                }
                                            ) {
                                                onUpdateSliderPosition(0L)
                                                onTriggerEvent(
                                                    SongListEvents.SetCurrentSong(
                                                        itemSong
                                                    )
                                                )
                                                requestFocusAndPlay(0L)
                                            }
                                        }
                                    }
                                }
                            }

                            itemSongs.itemCount == 0 && !isPermissionGranted -> {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = paddingValues.calculateTopPadding(),
                                            bottom = paddingValues.calculateBottomPadding()
                                        )
                                ) {
                                    Text(
                                        stringResource(R.string.no_items_found),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun collapseExpandBottomScaffold(
    shouldCollapse: Boolean,
    coroutineScope: CoroutineScope,
    bottomSheetScaffoldState: BottomSheetScaffoldState
) {
    coroutineScope.launch {
        if (shouldCollapse) {
            bottomSheetScaffoldState.bottomSheetState.collapse()
        } else {
            bottomSheetScaffoldState.bottomSheetState.expand()
        }
    }
}