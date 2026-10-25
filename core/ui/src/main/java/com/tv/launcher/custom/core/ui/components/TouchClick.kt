package com.tv.launcher.custom.core.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/** Lets TV controls also activate from touchscreen input without replacing D-pad handling. */
fun Modifier.touchClick(onClick: () -> Unit): Modifier = pointerInput(onClick) {
    detectTapGestures(onTap = { onClick() })
}
