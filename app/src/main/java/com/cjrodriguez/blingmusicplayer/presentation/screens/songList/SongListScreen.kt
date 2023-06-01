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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.model.Song
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.presentation.components.AreYouSureDeleteDialog
import com.cjrodriguez.blingmusicplayer.presentation.components.Music_Item
import com.cjrodriguez.blingmusicplayer.presentation.components.SearchAppBar
import com.cjrodriguez.blingmusicplayer.presentation.components.VolumeDialog
import com.cjrodriguez.blingmusicplayer.presentation.screens.singleSong.PlaySongScreen
import com.cjrodriguez.blingmusicplayer.presentation.screens.songList.events.SongListEvents
import com.cjrodriguez.blingmusicplayer.presentation.theme.BlingMusicPlayerTheme
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.cjrodriguez.blingmusicplayer.util.prepareAndPlay
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
    currentSongMediaItem: SongWithFavourite? = null,
    nextSongMediaItem: SongWithFavourite? = null,
    prevSongMediaItem: SongWithFavourite? = null,
    itemSongs: LazyPagingItems<SongWithFavourite>,
    shouldUpdate: Boolean,
    isLoading: Boolean = false,
    sliderPosition: Float,
    onUpdateSliderPosition: (Long) -> Unit,
    requestFocusAndPlay: (Long?) -> Unit,
    abandonFocusAndPause: () -> Unit,
    query: String,
    controller: MediaController?,
    openVolumeDialog: () -> Unit,
    openDeleteDialog: () -> Unit,
    currentVolume: Float,
    updateSeekBar: (Float) -> Unit,
    maxVolume: Float,
    //messageQueue: Queue<GenericMessageInfo>,
    messageSet: Set<GenericMessageInfo>,
    itemToDelete: MutableState<Song?>,
    dialogVolumeState: MutableState<Boolean>,
    dialogDeleteState: MutableState<Boolean>,
    onTriggerEvent: (SongListEvents) -> Any = {}
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }

    if (shouldUpdate) {
        onTriggerEvent(SongListEvents.GetSongs)
        onTriggerEvent(SongListEvents.UpdateShouldUpdate(false))
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var lifecycle by remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }

    var shouldShowSearch by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.StartIntentSenderForResult(), onResult = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.e("sammm", it.toString())
            if (it.resultCode == RESULT_OK) {
                itemToDelete.value?.let { song ->
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
                        onTriggerEvent(SongListEvents.SetIsAcceptedPermission("ACCEPT"))
                        onTriggerEvent(SongListEvents.GetSongs)
                    } else {
                        //write permission to delete file
                        itemToDelete.value?.let {
                            onTriggerEvent(SongListEvents.DeleteSong(listOf(it), launcher))
                        }
                    }
                } else {
                    if (key == Manifest.permission.READ_EXTERNAL_STORAGE || key == Manifest.permission.READ_MEDIA_AUDIO) {
                        onTriggerEvent(SongListEvents.SetIsAcceptedPermission("REJECT"))
                        (context as? Activity)?.finish()
                    } else {
                        //toast to delete file
                        itemToDelete.value?.let {
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
        openAppSettings = {
            (context as? Activity)?.finish()
        },
        messageSet = messageSet,
        onRemoveHeadMessageFromQueue = { onTriggerEvent(SongListEvents.OnRemoveHeadMessageFromQueue) },

        ) {
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

        if (dialogVolumeState.value) {
            VolumeDialog(
                onDismiss = { dialogVolumeState.value = false }, maxVolume = maxVolume,
                updateSeekBar = updateSeekBar, volumeLevel = currentVolume
            )
        }

        if (dialogDeleteState.value) {
            AreYouSureDeleteDialog(onDismiss = { dialogDeleteState.value = false },
                onPositiveAction = {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    dialogDeleteState.value = false
                }, onNegativeAction = {
                    dialogDeleteState.value = false
                })
        }
        BottomSheetScaffold(
            sheetPeekHeight = if (currentSongMediaItem == null) 0.dp else 56.dp,
            sheetShape = if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) RoundedCornerShape(
                percent = 50
            ) else MaterialTheme.shapes.large,
            sheetContent = {
                currentSongMediaItem?.let {
                    PlaySongScreen(
                        currentSong = currentSongMediaItem,
                        bottomSheetScaffoldState = bottomSheetScaffoldState,
                        sliderPosition = sliderPosition,
                        duration = currentSongMediaItem.song.duration,
                        setUnSetFavourite = {
                            onTriggerEvent(
                                SongListEvents.SetUnSetFavourite(
                                    currentSongMediaItem
                                )
                            )
                        },
                        skipForward = {
                            onUpdateSliderPosition(0L)
                            nextSongMediaItem?.let {
                                onTriggerEvent(SongListEvents.SetCurrentSong(it))
                                controller?.let { controller ->
                                    controller.setMediaItem(it.song.mediaItem)
                                    if (controller.isPlaying) {
                                        controller.prepareAndPlay()
                                    }
                                }
                            }
                        },
                        skipBackward = {
                            onUpdateSliderPosition(0L)
                            prevSongMediaItem?.let {
                                onTriggerEvent(SongListEvents.SetCurrentSong(it))
                                controller?.let { controller ->
                                    controller.setMediaItem(it.song.mediaItem)
                                    if (controller.isPlaying) {
                                        controller?.prepareAndPlay()
                                    }
                                }
                            }
                        },
                        playOrPause = {
                            if (!isPlaying) {
                                requestFocusAndPlay(null)
                            } else {
                                abandonFocusAndPause()
                            }
                        },
                        onUpdateSliderPosition = {
                            onUpdateSliderPosition(it.toLong())
                        },
                        onClick = {
                            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                collapseExpandBottomScaffold(
                                    shouldCollapse = false,
                                    coroutineScope = coroutineScope,
                                    bottomSheetScaffoldState = bottomSheetScaffoldState
                                )
                            }
                        }, controller = controller,
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
                        displayVolumeDialog = {
                            if (dialogVolumeState.value) {
                                dialogVolumeState.value = false
                            } else {
                                openVolumeDialog()
                            }
                        }, currentVolume = currentVolume.toInt()
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
                                    append("BLING" + " ")
                                }
                                append("Music")
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

                when {

                    itemSongs.itemCount > 0 -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = paddingValues.calculateTopPadding()
                                )
                        ) {
                            itemsIndexed(items = itemSongs) { index, item ->
                                item?.let { itemSong ->
                                    Music_Item(item, deleteSong = {
                                        itemToDelete.value = itemSong.song
                                        openDeleteDialog() }
                                    ) {
                                        onUpdateSliderPosition(0L)
                                        onTriggerEvent(SongListEvents.SetCurrentSong(itemSong))
                                        requestFocusAndPlay(0L)
                                    }
                                }
                            }
                        }
                    }
                    itemSongs.itemCount == 0 -> {
                        Text("No items found")
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