package com.tv.launcher.custom.core.ui.components

import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ICON_SIZE_PX = 256

/** Icons survive recompositions and row recycling; 48 decoded icons is ~12 MB. */
private val iconCache = object : LruCache<String, ImageBitmap>(48) {}

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var icon by remember(packageName) { mutableStateOf(iconCache.get(packageName)) }
    LaunchedEffect(packageName) {
        if (icon == null) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    context.packageManager.getApplicationIcon(packageName)
                        .toBitmap(ICON_SIZE_PX, ICON_SIZE_PX)
                        .asImageBitmap()
                }.getOrNull()
            }
            if (loaded != null) {
                iconCache.put(packageName, loaded)
                icon = loaded
            }
        }
    }
    icon?.let { Image(bitmap = it, contentDescription = null, modifier = modifier) }
}
