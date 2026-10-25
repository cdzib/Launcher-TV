package com.tv.launcher.custom.core.data.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.preferencesDataStore
import com.tv.launcher.custom.core.model.theme.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "launcher_prefs")
private val ThemeKey = stringPreferencesKey("theme")
private val AccentKey = stringPreferencesKey("appearance_accent")
private val BackgroundKey = stringPreferencesKey("appearance_background")
private val TileShapeKey = stringPreferencesKey("appearance_tile_shape")
private val BackgroundImageUriKey = stringPreferencesKey("appearance_background_image_uri")
private val BackgroundColorKey = stringPreferencesKey("appearance_background_color_argb")
private val AccentColorKey = stringPreferencesKey("appearance_accent_color_argb")
private val InstalledThemeIdKey = stringPreferencesKey("appearance_installed_theme_id")

@Singleton
class DataStoreThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : ThemeRepository {

    override fun observeTheme(): Flow<ThemeId> = context.dataStore.data.map { prefs ->
        prefs[ThemeKey]?.let { runCatching { ThemeId.valueOf(it) }.getOrNull() } ?: ThemeId.WEBOS
    }

    override suspend fun setTheme(themeId: ThemeId) {
        context.dataStore.edit { it[ThemeKey] = themeId.name }
    }

    override fun observeAppearance(): Flow<LauncherAppearance> = context.dataStore.data.map { prefs ->
        prefs.toLauncherAppearance()
    }

    override suspend fun setAppearance(appearance: LauncherAppearance) {
        context.dataStore.edit { it.setLauncherAppearance(appearance) }
    }
}

internal fun MutablePreferences.setLauncherAppearance(appearance: LauncherAppearance) {
    this[ThemeKey] = appearance.themeId.name
    this[AccentKey] = appearance.accent.name
    this[BackgroundKey] = appearance.background.name
    this[TileShapeKey] = appearance.tileShape.name
    appearance.backgroundImageUri?.let { this[BackgroundImageUriKey] = it } ?: remove(BackgroundImageUriKey)
    appearance.backgroundColorArgb?.let { this[BackgroundColorKey] = it.toString() } ?: remove(BackgroundColorKey)
    appearance.accentColorArgb?.let { this[AccentColorKey] = it.toString() } ?: remove(AccentColorKey)
    appearance.installedThemeId?.let { this[InstalledThemeIdKey] = it } ?: remove(InstalledThemeIdKey)
    for (slot in 0..4) {
        val key = stringPreferencesKey("shortcut_$slot")
        val packageName = appearance.shortcuts[slot]
        if (packageName == null) remove(key) else this[key] = packageName
    }
}

internal fun Preferences.toLauncherAppearance() = LauncherAppearance(
    themeId = enumPreference(ThemeKey, ThemeId.WEBOS),
    accent = enumPreference(AccentKey, AccentChoice.PROFILE),
    background = enumPreference(BackgroundKey, BackgroundChoice.PROFILE),
    tileShape = enumPreference(TileShapeKey, TileShapeChoice.PROFILE),
    shortcuts = (0..4).mapNotNull { slot -> this[stringPreferencesKey("shortcut_$slot")]?.let { slot to it } }.toMap(),
    backgroundImageUri = this[BackgroundImageUriKey],
    backgroundColorArgb = this[BackgroundColorKey]?.toLongOrNull(),
    accentColorArgb = this[AccentColorKey]?.toLongOrNull(),
    installedThemeId = this[InstalledThemeIdKey]
)

private inline fun <reified T : Enum<T>> Preferences.enumPreference(
    key: androidx.datastore.preferences.core.Preferences.Key<String>,
    default: T
): T = this[key]?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default
