package com.cjrodriguez.blingmusicplayer.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cjrodriguez.blingmusicplayer.R

@Preview
@Composable
fun SongImage(
    imageUrl: String = "",
    isDarkTheme: Boolean = false, size: Int = 200, shape: Shape = RoundedCornerShape(10.dp)
) {
    if (imageUrl.isEmpty()) {
        Column(
            modifier = Modifier
                .height(size.dp)
                .background(
                    if (isDarkTheme) Color.DarkGray else Color.LightGray,
                    shape = RoundedCornerShape(10.dp)
                ), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_music_note_24),
                contentDescription = ""
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Uri.parse(imageUrl))
                .build(),
            error = painterResource(R.drawable.music_item_icon),
            contentDescription = stringResource(R.string.album_art),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(shape)
                .size(size.dp)
        )
    }
}