package com.cjrodriguez.blingmusicplayer.presentation.screens.singleSong

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.VolumeMute
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import com.cjrodriguez.blingmusicplayer.R
import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
import com.cjrodriguez.blingmusicplayer.presentation.components.SongImage
import com.cjrodriguez.blingmusicplayer.presentation.theme.Purple80
import com.cjrodriguez.blingmusicplayer.util.Util

@androidx.media3.common.util.UnstableApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun PlaySongScreen(
    currentSong: SongWithFavourite,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    collapseExpandBottomSheet: (Boolean) -> Unit,
    skipForward: () -> Unit,
    displayVolumeDialog: () -> Unit,
    skipBackward: () -> Unit,
    playOrPause: () -> Unit,
    setUnSetFavourite: (SongWithFavourite) -> Unit,
    updateShuffle: () -> Unit,
    updateRepeat: () -> Unit,
    sliderPosition: Float,
    onUpdateSliderPosition: (Float) -> Unit,
    controller: MediaController?,
    currentVolume: Int,
    isPlaying: Boolean,
    isShuffle: Boolean,
    shouldRepeat: Boolean,
    duration: Long,
    onClick: () -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val config = LocalConfiguration.current
    val isSystemInDarkMode = isSystemInDarkTheme()
    val bitmap: MutableState<Int?> = remember {
        mutableStateOf(
            try {
                val map = MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.parse(currentSong.song.albumId)
                )
                map?.let { ap ->
                    Palette.from(ap).generate().dominantSwatch?.let {
                        adjustAlpha(it.rgb, 2f)
                    }
                }
            } catch (ex: Exception) {
                null
            }
        )
    }

    val colorOfText: MutableState<Color> = remember {
        mutableStateOf(if (ColorUtils.calculateLuminance(bitmap.value?.let { Color(it).toArgb() }
                ?: Purple80.toArgb()) < 0.5) Color.White else Color.Black)
    }

    LaunchedEffect(currentSong.song.albumId) {
        bitmap.value = try {
            val map = MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                Uri.parse(currentSong.song.albumId)
            )
            map?.let { ap ->
                Palette.from(ap).generate().dominantSwatch?.let {
                    adjustAlpha(it.rgb, 2f)
                }
            }
        } catch (ex: Exception) {
            null
        }

        colorOfText.value =
            if (ColorUtils.calculateLuminance(bitmap.value?.let { Color(it).toArgb() }
                    ?: Purple80.toArgb()) < 0.5) Color.White else Color.Black
    }
    //val yrs = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(currentSong.song.albumId.toString()))
    Column(modifier = Modifier//.clickable { onClick() }
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    bitmap.value?.let { Color(it) } ?: MaterialTheme.colorScheme.primary,
                ), tileMode = TileMode.Clamp, startY = 600f
            )
            //color = bitmap.value?.let { Color(it) }?:MaterialTheme.colorScheme.primary
        )
        //.clickable { onClick() }
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
                    { controller?.seekTo(it) },
                    onUpdateSliderPosition,
                    duration,
                    updateShuffle,
                    updateRepeat,
                    isShuffle,
                    shouldRepeat,
                    skipBackward,
                    playOrPause,
                    isPlaying,
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
                    { controller?.seekTo(it) },
                    onUpdateSliderPosition,
                    duration,
                    updateShuffle,
                    updateRepeat,
                    isShuffle,
                    shouldRepeat,
                    skipBackward,
                    playOrPause,
                    isPlaying,
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
                        color = colorOfText.value
                    )
                    Text(
                        text = currentSong.song.artist,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        color = colorOfText.value
                    )
                }

                IconButton(onClick = skipBackward) {
                    Icon(
                        painter = painterResource(id = R.drawable.skip_backward),
                        contentDescription = "",
                        tint = colorOfText.value
                    )
                }

                IconButton(
                    onClick = playOrPause
                ) {
                    Icon(
                        painter = if (!isPlaying)
                            painterResource(id = R.drawable.baseline_play_arrow_24)
                        else painterResource(
                            id = androidx.media3.ui.R.drawable.exo_icon_pause
                        ),
                        contentDescription = "",
                        tint = colorOfText.value
                    )
                }

                IconButton(onClick = skipForward) {
                    Icon(
                        painter = painterResource(id = R.drawable.skip_forward),
                        contentDescription = "", tint = colorOfText.value
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
    currentSong: SongWithFavourite,
    setUnSetFavourite: (SongWithFavourite) -> Unit,
    sliderPosition: Float,
    controllerSeekTo: (Long) -> Unit,
    onUpdateSliderPosition: (Float) -> Unit,
    duration: Long,
    updateShuffle: () -> Unit,
    updateRepeat: () -> Unit,
    isShuffle: Boolean,
    shouldRepeat: Boolean,
    skipBackward: () -> Unit,
    playOrPause: () -> Unit,
    isPlaying: Boolean,
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

        //
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
                    isShuffle,
                    shouldRepeat,
                    skipBackward,
                    playOrPause,
                    isPlaying,
                    skipForward, isSystemInDarkMode
                )
            }
        }
    }
}

@Composable
private fun MusicControlsSegment(
    setUnSetFavourite: (SongWithFavourite) -> Unit,
    currentSong: SongWithFavourite,
    sliderPosition: Float,
    controllerSeekTo: (Long) -> Unit,
    onUpdateSliderPosition: (Float) -> Unit,
    duration: Long,
    updateShuffle: () -> Unit,
    updateRepeat: () -> Unit,
    isShuffle: Boolean,
    shouldRepeat: Boolean,
    skipBackward: () -> Unit,
    playOrPause: () -> Unit,
    isPlaying: Boolean,
    skipForward: () -> Unit,
    isSystemInDarkMode: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { setUnSetFavourite(currentSong) },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            if (currentSong.isFavourite == 0) {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = ""
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    tint = Color.White,
                    contentDescription = ""
                )
            }

        }

        Slider(
            value = sliderPosition,
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            onValueChange = {
                controllerSeekTo(it.toLong())
                onUpdateSliderPosition(it)
            },
            valueRange = 0f..duration.toFloat(),
            onValueChangeFinished = {
                //controllerSeekTo(sliderPosition.toLong())
                //prepare controller if not prepared
                //onUpdateSliderPosition(sliderPosition)
                //Log.e("rogan", sliderPosition.toString())
                //controller?.seekTo(sliderPosition.toLong())
                //seekToPosition()
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
                text = Util.convertMillisecondLongToFormattedTimeString(sliderPosition.toLong()),
                color = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = Util.convertMillisecondLongToFormattedTimeString(duration),
                color = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
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
                    painter = painterResource(id = if (isShuffle)
                        androidx.media3.ui.R.drawable.exo_legacy_controls_shuffle_on
                    else androidx.media3.ui.R.drawable.exo_icon_shuffle_off),
                    contentDescription = "",
                    tint = if (isSystemInDarkMode) Color.White
                    else MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = skipBackward,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_backward),
                    tint = if (isSystemInDarkMode) Color.White
                    else MaterialTheme.colorScheme.onBackground,
                    contentDescription = ""
                )
            }

            IconButton(
                onClick = playOrPause,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = if (!isPlaying) painterResource(id = R.drawable.baseline_play_arrow_24) else painterResource(
                        id = androidx.media3.ui.R.drawable.exo_icon_pause
                    ),
                    tint = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground,
                    contentDescription = ""
                )
            }

            IconButton(
                onClick = skipForward,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_forward),
                    contentDescription = "",
                    tint = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = updateRepeat,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (!shouldRepeat) androidx.media3.ui.R.drawable.exo_legacy_controls_repeat_all else androidx.media3.ui.R.drawable.exo_icon_repeat_one),
                    contentDescription = "",
                    tint = if (isSystemInDarkMode) Color.White else MaterialTheme.colorScheme.onBackground
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
    currentSong: SongWithFavourite,
    setUnSetFavourite: (SongWithFavourite) -> Unit,
    sliderPosition: Float,
    controllerSeekTo: (Long) -> Unit,
    onUpdateSliderPosition: (Float) -> Unit,
    duration: Long,
    updateShuffle: () -> Unit,
    updateRepeat: () -> Unit,
    isShuffle: Boolean,
    shouldRepeat: Boolean,
    skipBackward: () -> Unit,
    playOrPause: () -> Unit,
    isPlaying: Boolean,
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
                        contentDescription = "Mute Or Unmute",
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
            isShuffle,
            shouldRepeat,
            skipBackward,
            playOrPause,
            isPlaying,
            skipForward, isSystemInDarkMode
        )
    }
}

fun adjustAlpha(color: Int, factor: Float): Int {
    val alpha = (android.graphics.Color.alpha(color) * factor).toInt()
    val red = android.graphics.Color.red(color)
    val green = android.graphics.Color.green(color)
    val blue = android.graphics.Color.blue(color)
    return android.graphics.Color.argb(alpha, red, green, blue)
}