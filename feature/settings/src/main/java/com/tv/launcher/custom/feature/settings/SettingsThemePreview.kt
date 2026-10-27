package com.tv.launcher.custom.feature.settings

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.theme.CompositionCanvas
import com.tv.launcher.custom.core.theme.defaultCompositionFor
import com.tv.launcher.custom.core.ui.components.*
import com.tv.launcher.custom.core.model.theme.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Render at launcher size, then scale the whole scene, including typography and native blocks. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SettingsThemePreview(
    appearance: LauncherAppearance,
    composition: ThemeComposition?,
    name: String,
    apps: ImmutableList<AppEntry>,
    data: CanvasData,
    modifier: Modifier = Modifier,
    assetUris: Map<String, String> = emptyMap()
) {
    val previewData = remember(data, assetUris) { data.withThemeAssets(assetUris) }
    Layout(
        modifier = modifier.clipToBounds().clearAndSetSemantics { }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                    }
                }
            }
            .focusProperties { enter = { FocusRequester.Cancel } }.focusGroup(),
        content = {
            LauncherTheme(appearance) {
                // No navigation, app launch or shortcut mutation is reachable from a preview.
                CompositionLocalProvider(
                    LocalOpenApps provides {},
                    LocalShortcutPackages provides appearance.shortcuts,
                    LocalSetShortcut provides { _, _ -> }
                ) {
                    Box(Modifier.fillMaxSize().background(LocalLauncherStyle.current.palette.background)) {
                        SettingsBackgroundImage(appearance.backgroundImageUri, Modifier.fillMaxSize())
                        CompositionCanvas(
                            composition ?: defaultCompositionFor(appearance.themeId),
                            previewData, Modifier.fillMaxSize(), name,
                            interactive = false, transparentBackground = true
                        )
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val referenceWidth = 960.dp.roundToPx()
        val referenceHeight = 540.dp.roundToPx()
        val width = constraints.maxWidth
        val height = constraints.constrainHeight((width * 9f / 16f).toInt())
        val scene = measurables.single().measure(Constraints.fixed(referenceWidth, referenceHeight))
        layout(width, height) {
            scene.placeWithLayer(0, 0) {
                scaleX = width.toFloat() / referenceWidth
                scaleY = height.toFloat() / referenceHeight
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

@Composable
internal fun SettingsBackgroundImage(uri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val bitmap by produceState<ImageBitmap?>(null, uri, isPreview) {
        value = null
        if (uri != null && !isPreview) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    val parsed = Uri.parse(uri)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(parsed)?.use {
                        BitmapFactory.decodeStream(it, null, bounds)
                    }
                    val options = BitmapFactory.Options().apply {
                        while (bounds.outWidth / inSampleSize > 1280 || bounds.outHeight / inSampleSize > 1280) {
                            inSampleSize *= 2
                        }
                    }
                    context.contentResolver.openInputStream(parsed)?.use {
                        BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }
    bitmap?.let { Image(it, null, modifier, contentScale = ContentScale.Crop) }
}
