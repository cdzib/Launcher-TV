package com.tv.launcher.custom.core.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_THEME_IMAGE_SIZE_PX = 1440

@Composable
fun ThemeAssetImage(
    uri: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val parsedUri = Uri.parse(uri ?: return@runCatching null)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(parsedUri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
                var sampleSize = 1
                while (bounds.outWidth / sampleSize > MAX_THEME_IMAGE_SIZE_PX ||
                    bounds.outHeight / sampleSize > MAX_THEME_IMAGE_SIZE_PX
                ) {
                    sampleSize *= 2
                }
                val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                context.contentResolver.openInputStream(parsedUri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, options)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    if (image != null) {
        Image(bitmap = image!!, contentDescription = null, modifier = modifier, contentScale = contentScale)
    } else {
        Box(modifier)
    }
}
