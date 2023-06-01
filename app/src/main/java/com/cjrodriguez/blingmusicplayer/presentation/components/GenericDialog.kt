package com.cjrodriguez.blingmusicplayer.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.cjrodriguez.blingmusicplayer.presentation.theme.DarkGreen
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.cjrodriguez.blingmusicplayer.util.NegativeAction
import com.cjrodriguez.blingmusicplayer.util.PositiveAction
import com.cjrodriguez.blingmusicplayer.util.UIComponentType

@Preview
@Composable
fun GenericDialog(
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    title: String = "Success",
    getReadPermission: () -> Unit = {},
    openAppSettings: () -> Unit = {},
    description: String? = null,
    positiveAction: PositiveAction? = PositiveAction("Yes"),
    negativeAction: NegativeAction? = NegativeAction("No"),
    onRemoveHeadFromQueue: () -> Unit = {},
    info: GenericMessageInfo = GenericMessageInfo.Builder().id("")
        .uiComponentType(UIComponentType.None)
        .title("").build()
) {

    val isPermissionDialog = info.extraMessage?.contains("Permission") ?: false
    val isPermanentlyDeclinedDialog =
        info.extraMessage?.contains("PermanentlyDeclinedPermission") ?: false

    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onDismiss?.invoke()
            onRemoveHeadFromQueue()
        },
        title = { Text(title, fontSize = 14.sp) },
        text = {
            if (description != null) {
                Text(text = description)
            }
        },
        dismissButton = {
            if (negativeAction != null) {
                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        if (isPermissionDialog && !isPermanentlyDeclinedDialog) {
                            openAppSettings()
                        }
                        negativeAction.onNegativeAction()
                        onRemoveHeadFromQueue()
                    }
                ) {
                    Text(
                        text = negativeAction.negativeBtnTxt,
                        color = MaterialTheme.colors.background
                    )
                }
            }
        },
        confirmButton = {
            if (positiveAction != null) {
                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    onClick = {
                        if (isPermissionDialog) {
                            if (!isPermanentlyDeclinedDialog) {
                                getReadPermission()
                            } else {
                                openAppSettings()
                            }
                        }
                        positiveAction.onPositiveAction()
                        onRemoveHeadFromQueue()
                    },
                ) {
                    Text(
                        text = positiveAction.positiveBtnTxt,
                        color = MaterialTheme.colors.background
                    )
                }
            }
        }, properties = DialogProperties(
            dismissOnBackPress = !isPermissionDialog,
            dismissOnClickOutside = !isPermissionDialog
        )
    )
}
