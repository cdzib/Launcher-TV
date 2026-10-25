package com.tv.launcher.custom.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tv.launcher.custom.core.model.theme.ThemeBlock

/** Render input; the theme renderer never queries packages, repositories or navigation hosts. */
interface CanvasData {
    fun itemCount(block: ThemeBlock): Int
    fun label(block: ThemeBlock, index: Int): String
    @Composable fun Icon(block: ThemeBlock, index: Int, modifier: Modifier)
    @Composable fun HeroBackground(block: ThemeBlock, modifier: Modifier) {}
    fun isItemClickable(block: ThemeBlock, index: Int): Boolean = true
    fun onItemClick(block: ThemeBlock, index: Int) {}
    fun onNavClick(label: String) {}
    fun onSystemSettingsClick() {}
    fun onPersonalizationClick() {}
    fun withThemeAssets(assetUris: Map<String, String>): CanvasData = this
}
