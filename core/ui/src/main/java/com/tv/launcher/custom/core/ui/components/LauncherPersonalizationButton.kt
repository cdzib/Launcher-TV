package com.tv.launcher.custom.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle

@Composable
fun LauncherPersonalizationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalLauncherStyle.current.palette
    val description = stringResource(R.string.launcher_personalization)
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp).touchClick(onClick).semantics { contentDescription = description },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = palette.surface,
            contentColor = palette.onBackground,
            focusedContainerColor = palette.accent,
            focusedContentColor = palette.onBackground
        )
    ) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

@Preview
@Composable
private fun LauncherPersonalizationButtonPreview() = LauncherTheme(ThemeId.WEBOS) {
    LauncherPersonalizationButton({})
}
