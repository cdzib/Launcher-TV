package com.tv.launcher.custom.files

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.components.SettingsButton
import com.tv.launcher.custom.core.ui.components.SettingsColors
import com.tv.launcher.custom.core.ui.components.SettingsContentButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class BrowserRoot(val label: String, val directory: File)

/**
 * In-app replacement for ACTION_OPEN_DOCUMENT, for Android TV builds without DocumentsUI.
 * Navigates real directories with the D-pad and filters entries for the requested mode.
 */
@Composable
fun FileBrowserDialog(
    mode: FileBrowserMode,
    roots: List<BrowserRoot>,
    onDismiss: () -> Unit,
    onFileSelected: (File) -> Unit,
    storageAccess: Boolean = true,
    onRequestStorage: (() -> Unit)? = null,
    onOpenSystemPicker: (() -> Unit)? = null,
    onOpenBundledThemes: (() -> Unit)? = null
) {
    val rootDirectories = remember(roots) { roots.map { it.directory } }
    var current by remember(mode, roots) { mutableStateOf(roots.firstOrNull()?.directory) }
    val parent = current?.let { FileBrowser.parentWithin(it, rootDirectories) }
    BackHandler { if (parent != null) current = parent else onDismiss() }

    val entries by produceState(emptyList<File>(), current, mode) {
        value = withContext(Dispatchers.IO) {
            current?.let { FileBrowser.list(it, mode) }.orEmpty()
        }
    }
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(current, entries, parent) {
        runCatching { firstFocus.requestFocus() }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(SettingsColors.background)
        ) {
            Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    stringResource(
                        when (mode) {
                            FileBrowserMode.THEMES -> R.string.file_browser_title_themes
                            FileBrowserMode.IMAGES -> R.string.file_browser_title_images
                            FileBrowserMode.ASSETS -> R.string.file_browser_title_assets
                        }
                    ),
                    color = SettingsColors.text, fontSize = 26.sp
                )
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        current?.absolutePath ?: stringResource(R.string.file_browser_no_roots),
                        modifier = Modifier.weight(1f),
                        color = SettingsColors.muted, fontSize = 13.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    onOpenBundledThemes?.let {
                        SettingsButton(label = stringResource(R.string.file_browser_bundled), onClick = it)
                    }
                    onOpenSystemPicker?.let {
                        SettingsButton(label = stringResource(R.string.file_browser_system_picker), onClick = it)
                    }
                }
                if (roots.size > 1) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roots.forEach { root ->
                            SettingsButton(
                                label = root.label,
                                selected = current?.let { FileBrowser.isInside(it, root.directory) } == true,
                                onClick = { current = root.directory }
                            )
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(SettingsColors.divider))
                if (!storageAccess && onRequestStorage != null) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.file_browser_access_limited),
                            modifier = Modifier.weight(1f),
                            color = SettingsColors.muted, fontSize = 13.sp
                        )
                        SettingsButton(
                            label = stringResource(R.string.file_browser_grant_access),
                            primary = true,
                            onClick = onRequestStorage
                        )
                    }
                }
                if (current == null) {
                    Text(stringResource(R.string.file_browser_no_roots), color = SettingsColors.muted)
                } else {
                    if (parent != null) {
                        BrowserRow(
                            icon = Icons.Filled.ArrowUpward,
                            label = stringResource(R.string.file_browser_up),
                            muted = true,
                            modifier = Modifier.focusRequester(firstFocus)
                        ) { current = parent }
                    }
                    LazyColumn(
                        Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(entries) { index, file ->
                            val isDirectory = file.isDirectory
                            BrowserRow(
                                icon = when {
                                    isDirectory -> Icons.Filled.Folder
                                    mode == FileBrowserMode.THEMES -> Icons.Filled.Description
                                    else -> Icons.Filled.Image
                                },
                                label = file.name,
                                detail = if (isDirectory) null else FileBrowser.formatSize(file.length()),
                                modifier = if (parent == null && index == 0) Modifier.focusRequester(firstFocus) else Modifier
                            ) {
                                if (isDirectory) current = file else onFileSelected(file)
                            }
                        }
                        if (entries.isEmpty()) {
                            item {
                                Text(
                                    stringResource(
                                        if (current?.canRead() == false) R.string.file_browser_unreadable
                                        else R.string.file_browser_empty
                                    ),
                                    color = SettingsColors.muted, fontSize = 15.sp,
                                    modifier = Modifier.padding(vertical = 20.dp)
                                )
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    SettingsButton(label = stringResource(R.string.cancel), onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun BrowserRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    muted: Boolean = false,
    onClick: () -> Unit
) {
    SettingsContentButton(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = if (muted) SettingsColors.muted else SettingsColors.accent,
            modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = if (muted) SettingsColors.muted else SettingsColors.text,
            fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        detail?.let {
            Text(it, color = SettingsColors.muted, fontSize = 13.sp)
        }
    }
}
