package com.cjrodriguez.blingmusicplayer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@ExperimentalMaterial3Api
@Preview
@Composable
fun SearchAppBar(
    closeSearch: () -> Unit = {}, query: String = "",
    searchMusic: (String) -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp,)
    ) {
        TextField(
            value = query, textStyle = TextStyle(fontSize = 14.sp),
            label = {
                Text(text = "Search Music")
            },
            colors = androidx.compose.material.TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                textColor = MaterialTheme.colorScheme.onBackground
            ),
            onValueChange = {
                searchMusic(it)
            }, modifier = Modifier.fillMaxWidth(0.85f))
        IconButton(onClick = closeSearch, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.Close,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "",
            )
        }
    }
}