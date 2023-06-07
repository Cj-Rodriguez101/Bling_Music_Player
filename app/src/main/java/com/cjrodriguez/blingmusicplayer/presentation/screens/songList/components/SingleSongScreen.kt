package com.cjrodriguez.blingmusicplayer.presentation.screens.songList.components

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.ShuffleOn
import androidx.compose.material.icons.outlined.VolumeMute
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.model.SongWrapper
import com.cjrodriguez.blingmusicplayer.presentation.components.SongImage
import com.cjrodriguez.blingmusicplayer.util.Util
import com.cjrodriguez.blingmusicplayer.util.toSongFavourite

@androidx.media3.common.util.UnstableApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun SingleSongScreen(
    currentSong: SongWrapper,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    collapseExpandBottomSheet: (Boolean) -> Unit,
    skipForward: () -> Unit,
    displayVolumeDialog: () -> Unit,
    skipBackward: () -> Unit,
    playOrPause: () -> Unit,
    setUnSetFavourite: (SongWithFavourite) -> Unit,
    updateShuffle: () -> Unit,
    updateRepeat: () -> Unit,
    seekToPosition: (Long) -> Unit,
    updateSliderDraggedState: (Boolean) -> Unit,
    sliderPosition: Float,
    onUpdateSliderPosition: (Float) -> Unit,
    currentVolume: Int,
    isPlaying: Boolean,
    isShuffle: Boolean,
    globalDynamicBackgroundColor: Color,
    globalOnIconColor: Color,
    shouldRepeat: Boolean,
    duration: Long,
    onClick: () -> Unit,
) {
    val config = LocalConfiguration.current
    val isSystemInDarkMode = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        globalDynamicBackgroundColor,
                    ), tileMode = TileMode.Clamp, startY = 600f
                )
            )
    ) {
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
            if (config.orientation != ORIENTATION_LANDSCAPE) {
                PortraitExpandedScreen(
                    collapseExpandBottomSheet,
                    displayVolumeDialog,
                    currentVolume,
                    currentSong,
                    setUnSetFavourite,
                    sliderPosition,
                    seekToPosition,
                    onUpdateSliderPosition,
                    duration,
                    updateShuffle,
                    updateRepeat,
                    updateSliderDraggedState,
                    isShuffle,
                    shouldRepeat,
                    skipBackward,
                    playOrPause,
                    isPlaying,
                    globalOnIconColor,
                    skipForward,
                    isSystemInDarkMode
                )
            } else {
                LandScapeExpandedScreen(
                    collapseExpandBottomSheet,
                    displayVolumeDialog,
                    currentVolume,
                    currentSong,
                    setUnSetFavourite,
                    sliderPosition,
                    seekToPosition,
                    onUpdateSliderPosition,
                    duration,
                    updateShuffle,
                    updateRepeat,
                    updateSliderDraggedState,
                    isShuffle,
                    shouldRepeat,
                    skipBackward,
                    playOrPause,
                    isPlaying,
                    globalOnIconColor,
                    skipForward,
                    isSystemInDarkMode
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .clickable { onClick() }
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                SongImage(
                    imageUrl = currentSong.song.albumId,
                    size = 30,
                    shape = RoundedCornerShape(50)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Text(
                        text = currentSong.song.title,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .fillMaxWidth(),
                        color = globalOnIconColor
                    )
                    Text(
                        text = currentSong.song.artist,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        color = globalOnIconColor
                    )
                }

                IconButton(onClick = skipBackward) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(id =R.string.previous),
                        tint = globalOnIconColor
                    )
                }

                IconButton(
                    onClick = playOrPause
                ) {
                    Icon(
                        imageVector = if (!isPlaying)
                            Icons.Default.PlayArrow
                        else Icons.Default.Pause,
                        contentDescription = "",
                        tint = globalOnIconColor,
                        modifier = Modifier.size(50.dp)
                    )
                }

                IconButton(onClick = skipForward) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(id =R.string.next), tint = globalOnIconColor
                    )

                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun LandScapeExpandedScreen(
    collapseExpandBottomSheet: (Boolean) -> Unit,
    displayVolumeDialog: () -> Unit,
    currentVolume: Int,
    currentSong: SongWrapper,
    setUnSetFavourite: (SongWithFavourite) -> Unit,
    sliderPosition: Float,
    controllerSeekTo: (Long) -> Unit,
    onUpdateSliderPosition: (Float) -> Unit,
    duration: Long,
    updateShuffle: () -> Unit,
    updateRepeat: () -> Unit,
    updateSliderDraggedState: (Boolean) -> Unit,
    isShuffle: Boolean,
    shouldRepeat: Boolean,
    skipBackward: () -> Unit,
    playOrPause: () -> Unit,
    isPlaying: Boolean,
    globalColorOfSurface: Color,
    skipForward: () -> Unit,
    isSystemInDarkMode: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                collapseExpandBottomSheet(true)
            }) {
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = "",
                    tint = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
                )
            }

            Row {
                IconButton(onClick = displayVolumeDialog) {
                    Icon(
                        imageVector = if (currentVolume > 0) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeMute,
                        contentDescription = "Mute Or Unmute",
                        tint = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SongImage(currentSong.song.albumId)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentSong.song.title,
                        fontSize = 16.sp,
                        maxLines = 1,
                        color = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .padding(top = 16.dp, bottom = 8.dp)
                    )

                    Text(
                        text = currentSong.song.artist,
                        color = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .padding(bottom = 8.dp)
                    )
                }

                MusicControlsSegment(
                    setUnSetFavourite,
                    currentSong,
                    sliderPosition,
                    controllerSeekTo,
                    onUpdateSliderPosition,
                    duration,
                    updateShuffle,
                    updateRepeat,
                    updateSliderDraggedState,
                    isShuffle,
                    shouldRepeat,
                    skipBackward,
                    playOrPause,
                    isPlaying,
                    globalColorOfSurface,
                    skipForward
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun PortraitExpandedScreen(
    collapseExpandBottomSheet: (Boolean) -> Unit,
    displayVolumeDialog: () -> Unit,
    currentVolume: Int,
    currentSong: SongWrapper,
    setUnSetFavourite: (SongWithFavourite) -> Unit,
    sliderPosition: Float,
    controllerSeekTo: (Long) -> Unit,
    onUpdateSliderPosition: (Float) -> Unit,
    duration: Long,
    updateShuffle: () -> Unit,
    updateRepeat: () -> Unit,
    updateSliderDraggedState: (Boolean) -> Unit,
    isShuffle: Boolean,
    shouldRepeat: Boolean,
    skipBackward: () -> Unit,
    playOrPause: () -> Unit,
    isPlaying: Boolean,
    globalColorOfSurface: Color,
    skipForward: () -> Unit,
    isSystemInDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(onClick = {
                    collapseExpandBottomSheet(true)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = "",
                        tint = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(onClick = displayVolumeDialog) {
                    Icon(
                        imageVector = if (currentVolume > 0) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeMute,
                        contentDescription = stringResource(R.string.change_volume),
                        tint = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SongImage(currentSong.song.albumId)

            Text(
                text = currentSong.song.title,
                color = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            Text(
                text = currentSong.song.artist,
                color = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .basicMarquee(iterations = Int.MAX_VALUE)
                    .padding(bottom = 8.dp)
            )
        }

        MusicControlsSegment(
            setUnSetFavourite,
            currentSong,
            sliderPosition,
            controllerSeekTo,
            onUpdateSliderPosition,
            duration,
            updateShuffle,
            updateRepeat,
            updateSliderDraggedState,
            isShuffle,
            shouldRepeat,
            skipBackward,
            playOrPause,
            isPlaying,
            globalColorOfSurface,
            skipForward
        )
    }
}

@Composable
private fun MusicControlsSegment(
    setUnSetFavourite: (SongWithFavourite) -> Unit,
    currentSong: SongWrapper,
    sliderPosition: Float,
    controllerSeekTo: (Long) -> Unit,
    onUpdateSliderPosition: (Float) -> Unit,
    duration: Long,
    updateShuffle: () -> Unit,
    updateRepeat: () -> Unit,
    updateSliderDraggedState: (Boolean) -> Unit,
    isShuffle: Boolean,
    shouldRepeat: Boolean,
    skipBackward: () -> Unit,
    playOrPause: () -> Unit,
    isPlaying: Boolean,
    globalColorOfSurface: Color,
    skipForward: () -> Unit,
) {
    var pos by remember { mutableFloatStateOf(0f) }
    val timeString by remember(sliderPosition) {
        derivedStateOf {
            Util.convertMillisecondLongToFormattedTimeString(sliderPosition.toLong())
        }
    }
    val durationString by remember(duration) {
        derivedStateOf {
            Util.convertMillisecondLongToFormattedTimeString(duration)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { setUnSetFavourite(currentSong.toSongFavourite()) },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            if (currentSong.isFavourite == 0) {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = stringResource(R.string.favourite)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    tint = Color.White,
                    contentDescription = stringResource(R.string.unfavourite)
                )
            }

        }

        Slider(
            value = sliderPosition,
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            onValueChange = {
                updateSliderDraggedState(true)
                onUpdateSliderPosition(it)
                pos = it
            },
            valueRange = 0f..duration.toFloat(),
            onValueChangeFinished = {
                controllerSeekTo(pos.toLong())
                updateSliderDraggedState(false)
            },
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = timeString,
                color = globalColorOfSurface
            )
            Text(
                text = durationString,
                color = globalColorOfSurface
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = updateShuffle,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = if (isShuffle)
                        Icons.Outlined.ShuffleOn
                    else Icons.Outlined.Shuffle,
                    contentDescription = stringResource(R.string.shuffle),
                    tint = globalColorOfSurface
                )
            }

            IconButton(
                onClick = skipBackward,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = stringResource(id = R.string.previous),
                    tint = globalColorOfSurface
                )
            }

            IconButton(
                onClick = playOrPause,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = if (!isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    tint = globalColorOfSurface,
                    contentDescription = stringResource(R.string.play_or_pause),
                    modifier = Modifier.size(50.dp)
                )
            }

            IconButton(
                onClick = skipForward,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = stringResource(id = R.string.next),
                    tint = globalColorOfSurface
                )
            }

            IconButton(
                onClick = updateRepeat,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = if (!shouldRepeat) Icons.Outlined.Repeat else Icons.Outlined.RepeatOne,
                    contentDescription = stringResource(R.string.repeat),
                    tint = globalColorOfSurface
                )
            }
        }
    }
}

fun adjustAlpha(color: Int, factor: Float): Int {
    val alpha = (android.graphics.Color.alpha(color) * factor).toInt()
    val red = android.graphics.Color.red(color)
    val green = android.graphics.Color.green(color)
    val blue = android.graphics.Color.blue(color)
    return android.graphics.Color.argb(alpha, red, green, blue)
}