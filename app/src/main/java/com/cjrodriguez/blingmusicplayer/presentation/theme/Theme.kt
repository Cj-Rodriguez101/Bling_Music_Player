package com.cjrodriguez.blingmusicplayer.presentation.theme

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.cjrodriguez.blingmusicplayer.presentation.components.CircularIndeterminateProgressBar
import com.cjrodriguez.blingmusicplayer.presentation.components.DefaultSnackbar
import com.cjrodriguez.blingmusicplayer.presentation.components.GenericDialog
import com.cjrodriguez.blingmusicplayer.util.GenericMessageInfo
import com.cjrodriguez.blingmusicplayer.util.UIComponentType
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@ExperimentalComposeUiApi
@Composable
fun BlingMusicPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    snackBarHostState: SnackbarHostState,
    displayProgressBar: Boolean,
    globalColorBackground: Int?,
    useDarkIconsNew: Boolean,
    getReadPermission: () -> Unit,
    openAppSettings: () -> Unit,
    messageSet: Set<GenericMessageInfo>,
    isCurrentSongPresent: Boolean,
    onRemoveHeadMessageFromQueue: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    DisposableEffect(systemUiController, useDarkIcons) {
        systemUiController.setStatusBarColor(
            color = if (useDarkIcons) Color.Transparent else colorScheme.background,
            darkIcons = useDarkIcons
        )

//        systemUiController.setNavigationBarColor(
//            color = globalColorBackground?.let { Color(it) }?:colorScheme.background,//if (useDarkIcons) Color.Transparent else colorScheme.background,
//            darkIcons = useDarkIconsNew
//        )

//        systemUiController.setNavigationBarColor(
//            color = if (useDarkIcons) Color.Transparent else colorScheme.background,
//            darkIcons = useDarkIcons
//        )

        onDispose {}
    }

    val config = LocalConfiguration.current.orientation
    //val context = LocalContext.current
    //if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    LaunchedEffect(globalColorBackground) {
        setNavigationBarBasedOnInput(
            systemUiController,
            config,
            isCurrentSongPresent,
            globalColorBackground,
            colorScheme,
            useDarkIconsNew,
            useDarkIcons
        )
    }

    LaunchedEffect(isCurrentSongPresent) {
        setNavigationBarBasedOnInput(
            systemUiController,
            config,
            isCurrentSongPresent,
            globalColorBackground,
            colorScheme,
            useDarkIconsNew,
            useDarkIcons
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column {
                content()
            }
            CircularIndeterminateProgressBar(isDisplayed = displayProgressBar)
            DefaultSnackbar(
                snackbarHostState = snackBarHostState,
                onDismiss = {
                    snackBarHostState.currentSnackbarData?.dismiss()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            ProcessDialogQueue(
                messageSet = messageSet,
                getReadPermission = getReadPermission,
                openAppSettings = openAppSettings,
                onRemoveHeadMessageFromQueue = onRemoveHeadMessageFromQueue
            )
        }
    }
}

private fun setNavigationBarBasedOnInput(
    systemUiController: SystemUiController,
    config: Int,
    isCurrentSongPresent: Boolean,
    globalColorBackground: Int?,
    colorScheme: ColorScheme,
    useDarkIconsNew: Boolean,
    useDarkIcons: Boolean
) {
    systemUiController.setNavigationBarColor(
        color = if (config == Configuration.ORIENTATION_PORTRAIT && isCurrentSongPresent) globalColorBackground?.let {
            Color(it)
        }
            ?: colorScheme.primary else Color.Transparent,//if (useDarkIcons) Color.Transparent else colorScheme.background,
        darkIcons = if (config == Configuration.ORIENTATION_PORTRAIT && isCurrentSongPresent) useDarkIconsNew else useDarkIcons
    )
}

@Composable
fun ProcessDialogQueue(
    messageSet: Set<GenericMessageInfo>,
    onRemoveHeadMessageFromQueue: () -> Unit,
    getReadPermission: () -> Unit,
    openAppSettings: () -> Unit,
) {
    messageSet.isNotEmpty().let {
        messageSet.lastOrNull().let { info ->
            info?.let {
                if (info.uiComponentType == UIComponentType.Dialog) {
                    GenericDialog(
                        onDismiss = info.onDismiss,
                        title = info.title,
                        description = info.description,
                        positiveAction = info.positiveAction,
                        negativeAction = info.negativeAction,
                        onRemoveHeadFromQueue = onRemoveHeadMessageFromQueue,
                        getReadPermission = getReadPermission,
                        openAppSettings = openAppSettings,
                        info = info
                    )
                } else {
                    onRemoveHeadMessageFromQueue()
                }
            }
        }
    }
}