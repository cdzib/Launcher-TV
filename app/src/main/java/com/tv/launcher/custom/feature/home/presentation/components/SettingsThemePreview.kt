package com.tv.launcher.custom.feature.home.presentation.components

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
import com.tv.launcher.custom.core.theme.LauncherTheme
import com.tv.launcher.custom.core.theme.LocalLauncherStyle
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.home.presentation.layout.*
import com.tv.launcher.custom.feature.theme.data.CanvasData
import com.tv.launcher.custom.feature.theme.data.CompositionCanvas
import com.tv.launcher.custom.feature.theme.data.ThemeComposition
import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
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
    modifier: Modifier = Modifier
) {
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
                        if (composition != null) {
                            CompositionCanvas(
                                composition, data, Modifier.fillMaxSize(), name,
                                interactive = false, transparentBackground = true
                            )
                        } else {
                            val focus = remember { FocusRequester() }
                            when (appearance.themeId) {
                                ThemeId.WEBOS -> WebOsLayout(apps, focus, {}, {})
                                ThemeId.FIRE_TV -> TopNavLayout(apps, focus, {}, {})
                                ThemeId.ROKU -> SideGridLayout(apps, focus, {}, {})
                                ThemeId.TIZEN -> TizenLayout(apps, focus, {}, {})
                                ThemeId.ANDROID_TV -> AndroidTvLayout(apps, focus, {}, {})
                                ThemeId.GOOGLE_TV -> GoogleTvLayout(apps, focus, {}, {})
                                ThemeId.APPLE_TV -> AppleTvLayout(apps, focus, {}, {})
                            }
                        }
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
