package com.tv.launcher.custom.core.ui

import com.tv.launcher.custom.core.model.theme.*

fun AccentChoice.labelResource(): Int = when (this) {
    AccentChoice.PROFILE -> R.string.appearance_profile_default
    AccentChoice.BLUE -> R.string.accent_blue
    AccentChoice.PURPLE -> R.string.accent_purple
    AccentChoice.GREEN -> R.string.accent_green
    AccentChoice.ORANGE -> R.string.accent_orange
}
fun BackgroundChoice.labelResource(): Int = when (this) {
    BackgroundChoice.PROFILE -> R.string.appearance_profile_default
    BackgroundChoice.MIDNIGHT -> R.string.background_midnight
    BackgroundChoice.GRAPHITE -> R.string.background_graphite
    BackgroundChoice.PLUM -> R.string.background_plum
}

fun TileShapeChoice.labelResource(): Int = when (this) {
    TileShapeChoice.PROFILE -> R.string.appearance_profile_default
    TileShapeChoice.ROUNDED -> R.string.tile_shape_rounded
    TileShapeChoice.SQUARE -> R.string.tile_shape_square
}
