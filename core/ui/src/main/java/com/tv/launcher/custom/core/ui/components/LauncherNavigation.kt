package com.tv.launcher.custom.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.model.apps.AppEntry

val LocalOpenApps = staticCompositionLocalOf<() -> Unit> { {} }
val LocalOpenSearch = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun LauncherNavigation(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val onOpenSearch = LocalOpenSearch.current
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onOpenSearch) { Text(stringResource(R.string.search_apps)) }
        Button(onClick = onOpenSettings) { Text(stringResource(R.string.launcher_settings)) }
    }
}

@Composable
fun AppSelectionDialog(apps: List<AppEntry>, onLaunch: (String) -> Unit, onDismiss: () -> Unit, title: String? = null, onClear: (() -> Unit)? = null) {
    var query by remember { mutableStateOf("") }
    val palette = LocalLauncherStyle.current.palette
    val filteredApps = remember(apps, query) { apps.filter { it.label.contains(query.trim(), ignoreCase = true) } }
    val resultsFocus = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(colors = SurfaceDefaults.colors(containerColor = palette.surface)) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(title ?: stringResource(R.string.search_apps), fontSize = 24.sp)
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = palette.onBackground, fontSize = 22.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(12.dp)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.DirectionDown) {
                                if (event.type == KeyEventType.KeyDown) resultsFocus.requestFocus()
                                true
                            } else false
                        },
                    decorationBox = { field ->
                        if (query.isEmpty()) Text(stringResource(R.string.search_apps_hint), color = palette.onMuted)
                        field()
                    }
                )
                LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredApps, key = AppEntry::packageName) { app ->
                        Button(onClick = { onLaunch(app.packageName) }, modifier = Modifier.fillMaxWidth()
                            .then(if (app == filteredApps.firstOrNull()) Modifier.focusRequester(resultsFocus) else Modifier)) {
                            Text(app.label)
                        }
                    }
                }
                if (filteredApps.isEmpty()) Text(stringResource(R.string.no_matching_apps))
                if (onClear != null) Button(onClick = onClear) { Text(stringResource(R.string.clear_shortcut)) }
                Button(onClick = onDismiss, modifier = if (filteredApps.isEmpty()) Modifier.focusRequester(resultsFocus) else Modifier) { Text(stringResource(R.string.close)) }
            }
        }
        if (title != null) LaunchedEffect(Unit) { resultsFocus.requestFocus() }
    }
}
