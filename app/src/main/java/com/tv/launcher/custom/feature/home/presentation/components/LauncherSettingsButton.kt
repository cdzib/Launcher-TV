package com.tv.launcher.custom.feature.home.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.tv.launcher.custom.R

@Composable
fun LauncherSettingsButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(stringResource(R.string.launcher_settings))
    }
}
