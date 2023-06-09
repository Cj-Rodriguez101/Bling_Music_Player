package com.cjrodriguez.blingmusicplayer.presentation.components

import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun VolumeDialog(
    volumeLevel: Int,
    maxVolume: Int,
    updateVolumeSeekBarVm: (Int) -> Unit,
    updateDeviceVolume: (Int) -> Unit,
    onDismiss: () -> Unit = {},
) {
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentSeekBarVolume by remember { mutableIntStateOf(volumeLevel) }

    Dialog(onDismissRequest = onDismiss) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.TOP)
        Column {

            Spacer(modifier = Modifier.height(80.dp))
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(100)
                    )
                    .height(40.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = volumeLevel.toFloat(),
                    onValueChange = {
                        val intConverted = it.roundToInt()
                        updateVolumeSeekBarVm(intConverted)
                        currentSeekBarVolume = intConverted
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onValueChangeFinished = {
                        updateDeviceVolume(currentSeekBarVolume)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Blue,
                        activeTrackColor = Color.Blue,
                    ),
                    valueRange = 0f..maxVolume.toFloat(), steps = maxVolume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastInteractionTime > 5000) {
                onDismiss()
            }
            delay(500)
        }
    }
}