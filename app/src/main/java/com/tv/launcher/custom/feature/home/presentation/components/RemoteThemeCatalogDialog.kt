package com.tv.launcher.custom.feature.home.presentation.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.tv.launcher.custom.R
import com.tv.launcher.custom.core.theme.LocalLauncherStyle
import com.tv.launcher.custom.feature.theme.data.RemoteTheme

@Composable
fun RemoteThemeCatalogDialog(
    catalogUrl: String,
    onCatalogUrlChange: (String) -> Unit,
    directZipUrl: String,
    onDirectZipUrlChange: (String) -> Unit,
    themes: List<RemoteTheme>,
    installedThemeVersions: Map<String, String?>,
    isLoading: Boolean,
    errorMessage: String?,
    onLoadCatalog: () -> Unit,
    onDownloadDirectZip: () -> Unit,
    onDownloadCatalogTheme: (RemoteTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalLauncherStyle.current.palette
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 900.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = SurfaceDefaults.colors(containerColor = palette.surface, contentColor = palette.onBackground)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.online_themes_title), fontSize = 30.sp)
                Text(stringResource(R.string.catalog_url_label))
                BasicTextField(
                    value = catalogUrl,
                    onValueChange = onCatalogUrlChange,
                    singleLine = true,
                    textStyle = TextStyle(color = palette.onBackground, fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(12.dp),
                    decorationBox = { field ->
                        if (catalogUrl.isBlank()) Text(stringResource(R.string.catalog_url_hint), color = palette.onMuted)
                        field()
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onLoadCatalog, enabled = !isLoading) {
                        Text(stringResource(R.string.search_online_themes))
                    }
                    Button(onClick = onDismiss) { Text(stringResource(R.string.close)) }
                }
                if (errorMessage != null) Text(errorMessage, color = palette.accent)
                if (isLoading) Text(stringResource(R.string.downloading_theme))
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(themes, key = RemoteTheme::id) { theme ->
                        val isInstalled = installedThemeVersions.containsKey(theme.id)
                        val installedVersion = installedThemeVersions[theme.id]
                        val isCurrentVersion = isInstalled && installedVersion == theme.version
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(theme.name)
                                Text("v${theme.version} · ${theme.description}", fontSize = 14.sp, color = palette.onMuted)
                            }
                            Button(onClick = { onDownloadCatalogTheme(theme) }, enabled = !isLoading && !isCurrentVersion) {
                                Text(
                                    stringResource(
                                        when {
                                            isCurrentVersion -> R.string.theme_installed
                                            isInstalled -> R.string.reinstall_theme
                                            else -> R.string.download_theme
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
                Text(stringResource(R.string.direct_zip_url_label))
                BasicTextField(
                    value = directZipUrl,
                    onValueChange = onDirectZipUrlChange,
                    singleLine = true,
                    textStyle = TextStyle(color = palette.onBackground, fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(12.dp),
                    decorationBox = { field ->
                        if (directZipUrl.isBlank()) Text(stringResource(R.string.direct_zip_url_hint), color = palette.onMuted)
                        field()
                    }
                )
                Button(onClick = onDownloadDirectZip, enabled = !isLoading && directZipUrl.isNotBlank()) {
                    Text(stringResource(R.string.download_direct_theme))
                }
            }
        }
    }
}
