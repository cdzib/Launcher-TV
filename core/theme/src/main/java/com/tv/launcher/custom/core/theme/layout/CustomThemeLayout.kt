package com.tv.launcher.custom.core.theme.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.components.CanvasData
import com.tv.launcher.custom.core.theme.CompositionCanvas
import com.tv.launcher.custom.core.model.theme.ThemeComposition

@Composable
fun CustomThemeLayout(
    name: String,
    composition: ThemeComposition,
    canvasData: CanvasData,
    firstItemFocusRequester: FocusRequester,
    onOpenSettings: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        CompositionCanvas(
            composition = composition,
            data = canvasData,
            modifier = Modifier.fillMaxSize(),
            title = name,
            interactive = true,
            transparentBackground = true,
            firstItemFocusRequester = firstItemFocusRequester
        )
        Button(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(24.dp)
        ) {
            Text(stringResource(R.string.launcher_settings))
        }
    }
}
