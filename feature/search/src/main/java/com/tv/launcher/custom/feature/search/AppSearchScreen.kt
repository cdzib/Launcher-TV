package com.tv.launcher.custom.feature.search

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.theme.ThemeId

@Composable
fun AppSearchDialog(
    apps: List<AppEntry>,
    onLaunch: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit = onDismiss
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        AppSearchScreen(apps, onLaunch, onDismiss, onOpenSettings = onOpenSettings)
    }
}

@Composable
internal fun AppSearchScreen(
    apps: List<AppEntry>,
    onLaunch: (String) -> Unit,
    onDismiss: () -> Unit,
    initialQuery: String = "",
    initialKeyboardVisible: Boolean = false,
    onOpenSettings: () -> Unit = onDismiss
) {
    val style = LocalLauncherStyle.current
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var selectedPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var keyboardVisible by rememberSaveable { mutableStateOf(initialKeyboardVisible) }
    var keyboardRequest by remember { mutableStateOf(0) }
    val filteredApps = remember(apps, query) { filterSearchApps(apps, query) }
    val selectedApp = resolveSelectedSearchApp(filteredApps, selectedPackage)
    val fieldFocus = remember { FocusRequester() }
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    LaunchedEffect(selectedApp?.packageName) { selectedPackage = selectedApp?.packageName }

    val showKeyboard: () -> Unit = {
        keyboardVisible = true
        keyboardRequest += 1
    }
    val toggleKeyboard: () -> Unit = {
        if (keyboardVisible) keyboardVisible = false else showKeyboard()
    }
    val voiceUnavailable: () -> Unit = {
        if (!isPreview) {
            Toast.makeText(
                context,
                context.getString(R.string.search_voice_unavailable),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box(
        Modifier.fillMaxSize()
            .background(style.palette.background)
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Escape && event.type == KeyEventType.KeyUp) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        if (shouldUseFocusSearchLayout(style.layout)) {
            SearchFocusLayout(
                apps = apps,
                filteredApps = filteredApps,
                query = query,
                keyboardVisible = keyboardVisible,
                keyboardRequest = keyboardRequest,
                fieldFocus = fieldFocus,
                onQueryChange = { query = it },
                onSelect = { selectedPackage = it },
                onLaunch = onLaunch,
                onDismiss = onDismiss,
                onOpenSettings = onOpenSettings,
                onVoice = voiceUnavailable,
                onShowKeyboard = showKeyboard,
                onToggleKeyboard = toggleKeyboard
            )
        } else {
            SearchBaseLayout(
                apps = apps,
                filteredApps = filteredApps,
                selectedApp = selectedApp,
                query = query,
                keyboardVisible = keyboardVisible,
                keyboardRequest = keyboardRequest,
                fieldFocus = fieldFocus,
                onQueryChange = { query = it },
                onSelect = { selectedPackage = it },
                onLaunch = onLaunch,
                onDismiss = onDismiss,
                onVoice = voiceUnavailable,
                onShowKeyboard = showKeyboard,
                onToggleKeyboard = toggleKeyboard
            )
        }
    }
}

private val SearchPreviewApps = listOf(
    AppEntry("sample.youtube", "YouTube"),
    AppEntry("sample.netflix", "Netflix"),
    AppEntry("sample.music", "Música"),
    AppEntry("sample.prime", "Prime Video"),
    AppEntry("sample.photos", "Fotos"),
    AppEntry("sample.moonlight", "Moonlight"),
    AppEntry("sample.browser", "Navegador"),
    AppEntry("sample.disney", "Disney+"),
    AppEntry("sample.spotify", "Spotify"),
    AppEntry("sample.files", "Archivos"),
    AppEntry("sample.max", "Max"),
    AppEntry("sample.games", "Juegos"),
    AppEntry("sample.plex", "Plex"),
    AppEntry("sample.drive", "Drive"),
    AppEntry("sample.crunchyroll", "Crunchyroll"),
    AppEntry("sample.gallery", "Galería"),
    AppEntry("sample.steam", "Steam Link"),
    AppEntry("sample.radio", "Radio")
)

@Preview(name = "Buscar · webOS", widthDp = 960, heightDp = 540)
@Composable
private fun SearchWebOsPreview() = LauncherTheme(ThemeId.WEBOS) {
    AppSearchScreen(SearchPreviewApps, {}, {})
}

@Preview(name = "Buscar · webOS teclado", widthDp = 960, heightDp = 540)
@Composable
private fun SearchWebOsKeyboardPreview() = LauncherTheme(ThemeId.WEBOS) {
    AppSearchScreen(SearchPreviewApps, {}, {}, initialKeyboardVisible = true)
}

@Preview(name = "Buscar · Google TV", widthDp = 960, heightDp = 540)
@Composable
private fun SearchGooglePreview() = LauncherTheme(ThemeId.GOOGLE_TV) {
    AppSearchScreen(SearchPreviewApps, {}, {})
}

@Preview(name = "Buscar · Roku foco", widthDp = 960, heightDp = 540)
@Composable
private fun SearchRokuPreview() = LauncherTheme(ThemeId.ROKU) {
    AppSearchScreen(SearchPreviewApps, {}, {})
}

@Preview(name = "Buscar · Apple TV foco teclado", widthDp = 960, heightDp = 540)
@Composable
private fun SearchAppleKeyboardPreview() = LauncherTheme(ThemeId.APPLE_TV) {
    AppSearchScreen(SearchPreviewApps, {}, {}, initialKeyboardVisible = true)
}

@Preview(name = "Buscar · sin resultados", widthDp = 960, heightDp = 540)
@Composable
private fun SearchNoResultsPreview() = LauncherTheme(ThemeId.GOOGLE_TV) {
    AppSearchScreen(SearchPreviewApps, {}, {}, initialQuery = "xyz")
}

@Preview(name = "Buscar · sin apps", widthDp = 960, heightDp = 540)
@Composable
private fun SearchEmptyPreview() = LauncherTheme(ThemeId.WEBOS) {
    AppSearchScreen(emptyList(), {}, {})
}

@Preview(name = "Buscar · nombre largo", widthDp = 960, heightDp = 540, fontScale = 1.15f)
@Composable
private fun SearchLongLabelPreview() = LauncherTheme(ThemeId.WEBOS) {
    AppSearchScreen(
        listOf(AppEntry("sample.library", "Una biblioteca multimedia con nombre muy largo")),
        {},
        {}
    )
}
