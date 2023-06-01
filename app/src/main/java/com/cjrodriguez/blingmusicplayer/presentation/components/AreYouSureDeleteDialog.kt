package com.cjrodriguez.blingmusicplayer.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cjrodriguez.blingmusicplayer.presentation.theme.DarkGreen

@Composable
@Preview
fun AreYouSureDeleteDialog(
    onDismiss: () -> Unit = {},
    title: String = "Are You Sure?",
    description: String = "Are You Sure You Want To Delete?",
    onNegativeAction: () -> Unit = {},
    onPositiveAction: () -> Unit = {}
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 14.sp) },
        text = {
            Text(text = description)
        },
        dismissButton = {
            Button(
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red),
                onClick = onNegativeAction,
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colors.background
                )
            }
        },
        confirmButton = {
            Button(
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                onClick = {
                    onPositiveAction()
                },
            ) {
                Text(
                    text = "Confirm",
                    color = MaterialTheme.colors.background
                )
            }
        }
    )
}