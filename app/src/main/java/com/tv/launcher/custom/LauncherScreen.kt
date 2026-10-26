package com.tv.launcher.custom

import android.widget.Toast
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.CompositionLocalProvider
import com.tv.launcher.custom.feature.search.AppSearchScreen
import com.tv.launcher.custom.feature.remote.RemoteThemeCatalogDialog
import com.tv.launcher.custom.feature.editor.ThemeCompositionEditorDialog
import com.tv.launcher.custom.feature.home.presentation.HomeContent
import com.tv.launcher.custom.core.ui.components.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.ui.theme.LocalBackgroundImageVisible
import com.tv.launcher.custom.feature.settings.LauncherSettingsDialog
import com.tv.launcher.custom.feature.home.presentation.state.HomeEffect
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.home.presentation.state.HomeUiState
import com.tv.launcher.custom.feature.home.presentation.viewmodel.HomeViewModel
import com.tv.launcher.custom.device.DeviceToolsDialog
import com.tv.launcher.custom.weather.WeatherSettingsDialog
import com.tv.launcher.custom.weather.WeatherSummaryCard
import com.tv.launcher.custom.core.data.theme.*
import com.tv.launcher.custom.core.model.theme.*
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlinx.collections.immutable.persistentListOf

private const val MAX_BACKGROUND_IMAGE_SIDE = 1920

private data class PendingThemeImport(
    val manifest: ThemeManifest,
    val archiveBytes: ByteArray,
    val catalogVersion: String? = null
)

private suspend fun fetchRemoteCatalog(url: String): List<RemoteTheme> = withContext(Dispatchers.IO) {
    val bytes = ThemeDownloadClient().download(url, 1024 * 1024)
    ThemeCatalogParser.parse(bytes)
}

private suspend fun fetchRemoteTheme(
    url: String,
    catalogTheme: RemoteTheme? = null
): PendingThemeImport = withContext(Dispatchers.IO) {
    val archiveBytes = ThemeDownloadClient().download(url, 20 * 1024 * 1024)
    if (catalogTheme != null && !verifySha256(archiveBytes, catalogTheme.sha256)) {
        throw IllegalArgumentException("Theme checksum does not match catalog")
    }
    val archive = ThemeArchiveReader.read(archiveBytes)
    val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
    if (catalogTheme != null && manifest.id != catalogTheme.id) {
        throw IllegalArgumentException("Theme id does not match catalog")
    }
    PendingThemeImport(manifest, archiveBytes, catalogVersion = catalogTheme?.version)
}

private fun InputStream.readBounded(maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val count = read(buffer)
        if (count < 0) return output.toByteArray()
        if (output.size() + count > maxBytes) error("Input exceeds allowed size")
        output.write(buffer, 0, count)
    }
}

private fun hasUsableDocumentsPicker(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "application/zip"
        addCategory(Intent.CATEGORY_OPENABLE)
    }
    return context.packageManager
        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        .any { !it.activityInfo.name.endsWith("DocumentsStub") }
}

@Composable
fun LauncherRoute(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler(enabled = true) { /* un launcher no sale con Back */ }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowMessage ->
                    Toast.makeText(context, effect.messageRes, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LauncherScreen(state = state, onIntent = viewModel::onIntent, weatherState = weatherState)
}

@Composable
fun LauncherScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    weatherState: com.tv.launcher.custom.feature.home.presentation.state.WeatherUiState =
        com.tv.launcher.custom.feature.home.presentation.state.WeatherUiState()
) {
    val successState = state as? HomeUiState.Success
    val appearance = successState?.appearance
    var showLauncherSettings by remember { mutableStateOf(false) }
    var showDeviceTools by remember { mutableStateOf(false) }
    var showWeatherSettings by remember { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf(LauncherSection.HOME) }
    val sectionStateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
    var pendingTheme by remember { mutableStateOf<PendingThemeImport?>(null) }
    var showBundledThemePicker by remember { mutableStateOf(false) }
    var showRemoteThemes by remember { mutableStateOf(false) }
    var catalogUrl by rememberSaveable { mutableStateOf("") }
    var directZipUrl by rememberSaveable { mutableStateOf("") }
    var remoteThemes by remember { mutableStateOf(emptyList<RemoteTheme>()) }
    var isRemoteLoading by remember { mutableStateOf(false) }
    var remoteErrorMessage by remember { mutableStateOf<String?>(null) }
    var showCompositionEditor by remember { mutableStateOf(false) }
    var isSavingComposition by remember { mutableStateOf(false) }
    var editingThemeId by remember { mutableStateOf<String?>(null) }
    var editingThemeAppearance by remember { mutableStateOf<LauncherAppearance?>(null) }
    var editingThemeVersion by remember { mutableStateOf<String?>(null) }
    var compositionName by rememberSaveable { mutableStateOf("") }
    var editedComposition by remember {
        mutableStateOf(ThemeComposition(listOf(
            ThemeBlock(ThemeBlockType.TITLE),
            ThemeBlock(ThemeBlockType.FAVORITES_ROW, maxItems = 5),
            ThemeBlock(ThemeBlockType.APPS_GRID, maxItems = 12, columns = 4)
        )))
    }
    var activeComposition by remember { mutableStateOf<ThemeComposition?>(null) }
    var activeCompositionName by remember { mutableStateOf("") }
    var activeThemeAssetUris by remember { mutableStateOf(emptyMap<String, String>()) }
    var editorAssetBytes by remember { mutableStateOf(emptyMap<String, ByteArray>()) }
    var editorAssetUris by remember { mutableStateOf(emptyMap<String, String>()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = section != LauncherSection.HOME) { section = section.onBack() }
    val editorAssetsPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) coroutineScope.launch {
            val importedAssets = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri ->
                    runCatching {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBounded(5 * 1024 * 1024) }
                            ?: error("Could not open image")
                        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) error("Invalid image")
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = ImageSampling.calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_BACKGROUND_IMAGE_SIDE)
                        }
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: error("Invalid image")
                        val output = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                        val filename = "image_${java.util.UUID.randomUUID()}.png"
                        "assets/$filename" to output.toByteArray()
                    }.getOrNull()
                }.toMap()
            }
            editorAssetBytes = editorAssetBytes + importedAssets
            editorAssetUris = editorAssetUris + importedAssets.mapValues { (_, bytes) ->
                val file = java.io.File(context.cacheDir, "theme-preview-${bytes.contentHashCode()}.png")
                file.writeBytes(bytes)
                file.toURI().toString()
            }
        }
    }
    var installedThemes by remember { mutableStateOf(emptyList<InstalledTheme>()) }
    var themeLibraryRefresh by remember { mutableStateOf(0) }
    val installedThemeStore = remember(context) {
        InstalledThemeStore(java.io.File(context.filesDir, "installed_themes"))
    }
    val compositionPreviewData: CanvasData = remember(state, onIntent, context, activeThemeAssetUris) {
        val success = state as? HomeUiState.Success
        val apps = success?.apps.orEmpty()
        val favoritePackages = success?.appearance?.shortcuts?.values?.toSet().orEmpty()
        InstalledAppsCanvasData(
            apps = apps,
            favorites = apps.filter { it.packageName in favoritePackages },
            onLaunch = { packageName -> onIntent(HomeIntent.Launch(packageName)) },
            onOpenApps = { section = LauncherSection.APPS },
            onOpenSettings = { openSystemSettings(context) },
            onOpenPersonalization = { showLauncherSettings = true },
            onOpenSearch = { section = LauncherSection.SEARCH },
            onUnavailableNavigation = {},
            assetUris = activeThemeAssetUris
        )
    }
    LaunchedEffect(appearance?.installedThemeId, themeLibraryRefresh) {
        val themeId = appearance?.installedThemeId
        val active = if (themeId == null) null else withContext(Dispatchers.IO) { installedThemeStore.load(themeId) }
        activeComposition = active?.composition
        activeCompositionName = active?.name.orEmpty()
        activeThemeAssetUris = active?.assetUris.orEmpty()
    }
    LaunchedEffect(showLauncherSettings, themeLibraryRefresh) {
        if (showLauncherSettings) {
            installedThemes = withContext(Dispatchers.IO) {
                val defaultThemePackage = context.assets.open("demo-themes/webos-demo.zip").use {
                    it.readBounded(20 * 1024 * 1024)
                }
                installedThemeStore.initializeDefaultTheme(defaultThemePackage)
                installedThemeStore.list()
            }
        }
    }
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onIntent(HomeIntent.SelectBackgroundImage(uri.toString()))
        }
    }
    val importThemePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) coroutineScope.launch {
            val imported = runCatching {
                val (bytes, manifest) = withContext(Dispatchers.IO) {
                    val archiveBytes = context.contentResolver.openInputStream(uri)?.use { it.readBounded(20 * 1024 * 1024) }
                        ?: error("Could not open theme package")
                    val archive = ThemeArchiveReader.read(archiveBytes)
                    archiveBytes to ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
                }
                PendingThemeImport(manifest, bytes)
            }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }.getOrNull()
            if (imported == null) {
                Toast.makeText(context, R.string.theme_import_failed, Toast.LENGTH_LONG).show()
            } else {
                pendingTheme = imported
            }
        }
    }
    val exportThemePicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        val activeAppearance = appearance
        if (uri != null && activeAppearance != null) coroutineScope.launch {
            val succeeded = runCatching {
                val imageBytes = activeAppearance.backgroundImageUri?.let { imageUri ->
                    withContext(Dispatchers.IO) {
                        val bitmap = decodeSampledBitmap(context.contentResolver, Uri.parse(imageUri), MAX_BACKGROUND_IMAGE_SIDE)
                            ?: error("Invalid background image")
                        ByteArrayOutputStream().use { output ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                            output.toByteArray()
                        }
                    }
                }
                val installedArchive = activeAppearance.installedThemeId?.let { id ->
                    withContext(Dispatchers.IO) { installedThemeStore.archiveBytes(id) }
                }
                val archive = installedArchive ?: withContext(Dispatchers.Default) {
                    ThemeArchiveWriter.write(activeAppearance, imageBytes)
                }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "w")?.use { it.write(archive) }
                        ?: error("Could not write theme package")
                }
            }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }.isSuccess
            if (!succeeded) Toast.makeText(context, R.string.theme_export_failed, Toast.LENGTH_LONG).show()
        }
    }
    fun loadRemoteCatalog() {
        if (isRemoteLoading) return
        isRemoteLoading = true
        remoteErrorMessage = null
        coroutineScope.launch {
            try {
                remoteThemes = fetchRemoteCatalog(catalogUrl)
            } catch (exception: Exception) {
                if (exception is kotlinx.coroutines.CancellationException) throw exception
                remoteErrorMessage = exception.message ?: context.getString(R.string.remote_theme_error)
            } finally {
                isRemoteLoading = false
            }
        }
    }
    fun downloadRemoteTheme(url: String, catalogTheme: RemoteTheme? = null) {
        if (isRemoteLoading) return
        isRemoteLoading = true
        remoteErrorMessage = null
        coroutineScope.launch {
            try {
                pendingTheme = fetchRemoteTheme(url, catalogTheme)
                showRemoteThemes = false
            } catch (exception: Exception) {
                if (exception is kotlinx.coroutines.CancellationException) throw exception
                remoteErrorMessage = exception.message ?: context.getString(R.string.remote_theme_error)
            } finally {
                isRemoteLoading = false
            }
        }
    }
    val appearanceImage = appearance?.backgroundImageUri
    val backgroundBitmap by androidx.compose.runtime.produceState<ImageBitmap?>(null, appearanceImage) {
        value = if (appearanceImage == null) null else withContext(Dispatchers.IO) {
            runCatching {
                decodeSampledBitmap(context.contentResolver, Uri.parse(appearanceImage), MAX_BACKGROUND_IMAGE_SIDE)
                    ?.asImageBitmap()
            }.getOrNull()
        }
    }
    LauncherTheme(appearance ?: LauncherAppearance(state.themeId)) {
        val palette = LocalLauncherStyle.current.palette
        CompositionLocalProvider(LocalBackgroundImageVisible provides (backgroundBitmap != null)) {
        Box(Modifier.fillMaxSize().background(palette.background)) {
          if (backgroundBitmap != null) Image(backgroundBitmap!!, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
          Surface(
            modifier = Modifier.fillMaxSize(), shape = RectangleShape,
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = if (backgroundBitmap != null) androidx.compose.ui.graphics.Color.Transparent else palette.background,
                contentColor = palette.onBackground
            )
          ) {
            when (state) {
                is HomeUiState.Loading -> CenteredMessage(stringResource(R.string.loading))
                is HomeUiState.Empty -> CenteredMessage(stringResource(R.string.empty_apps))
                is HomeUiState.Error -> CenteredMessage(
                    message = stringResource(R.string.error_load),
                    actionLabel = stringResource(R.string.retry),
                    onAction = { onIntent(HomeIntent.Retry) }
                )
                  is HomeUiState.Success -> CompositionLocalProvider(
                      LocalOpenHome provides { section = LauncherSection.HOME },
                      LocalOpenApps provides { section = LauncherSection.APPS },
                      LocalOpenSearch provides { section = LauncherSection.SEARCH },
                      LocalLauncherSection provides section,
                      LocalLauncherSectionContent provides if (section == LauncherSection.HOME) null else {
                          @Composable {
                              sectionStateHolder.SaveableStateProvider(section.name) {
                                  when (section) {
                                      LauncherSection.APPS -> AppDrawerScreen(
                                          apps = state.apps,
                                          onLaunch = { onIntent(HomeIntent.Launch(it)) }
                                      )
                                      LauncherSection.SEARCH -> AppSearchScreen(
                                          apps = state.apps,
                                          onLaunch = { onIntent(HomeIntent.Launch(it)) },
                                          onDismiss = { section = LauncherSection.HOME },
                                          onOpenSettings = { openSystemSettings(context) },
                                          embedded = true
                                      )
                                      LauncherSection.HOME -> Unit
                                  }
                              }
                          }
                      },
                     LocalShortcutPackages provides state.appearance.shortcuts,
                     LocalSetShortcut provides { slot, packageName -> onIntent(HomeIntent.SelectShortcut(slot, packageName)) }
                  ) {
                    Box(Modifier.fillMaxSize()) {
                     HomeContent(
                     state = state,
                     onIntent = onIntent,
                      onOpenSettings = { openSystemSettings(context) },
                      onUnavailableNavigation = { label ->
                          Toast.makeText(context, context.getString(R.string.custom_section_unavailable, label), Toast.LENGTH_SHORT).show()
                      },
                      customComposition = activeComposition,
                      customThemeName = activeCompositionName,
                      customThemeAssets = activeThemeAssetUris,
                      onOpenPersonalization = { showLauncherSettings = true },
                      onOpenSearch = { section = LauncherSection.SEARCH }
                      )
                    }
                   }
            }
           }
           if (section == LauncherSection.HOME && state !is HomeUiState.Loading && state !is HomeUiState.Error) {
               val palette = LocalLauncherStyle.current.palette
               WeatherSummaryCard(
                   state = weatherState,
                   backgroundColor = palette.surface,
                   contentColor = palette.onBackground,
                   mutedColor = palette.onMuted,
                   onIntent = onIntent,
                   onOpenSettings = { showWeatherSettings = true },
                   modifier = Modifier.align(Alignment.TopStart).padding(24.dp)
               )
           }
           if (state !is HomeUiState.Success) {
              Button(
                  onClick = { showLauncherSettings = true },
                  modifier = Modifier.align(Alignment.TopEnd).padding(24.dp)
              ) {
                  Text(stringResource(R.string.launcher_settings))
              }
          }
        }
        if (showLauncherSettings) {
            LauncherSettingsDialog(
                appearance = appearance ?: LauncherAppearance(state.themeId),
                onIntent = { action ->
                    onIntent(when (action) {
                        AppearanceAction.ClearCustomBackground -> HomeIntent.ClearCustomBackground
                        is AppearanceAction.SelectBackground -> HomeIntent.SelectBackground(action.value)
                        is AppearanceAction.SelectBackgroundColor -> HomeIntent.SelectBackgroundColor(action.argb)
                        is AppearanceAction.SelectAccent -> HomeIntent.SelectAccent(action.value)
                        is AppearanceAction.SelectTileShape -> HomeIntent.SelectTileShape(action.value)
                    })
                },
                onDismiss = { showLauncherSettings = false },
                onChooseBackgroundImage = { backgroundPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp")) },
                onImportTheme = {
                    if (!hasUsableDocumentsPicker(context)) {
                        showBundledThemePicker = true
                    } else {
                        runCatching {
                            importThemePicker.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
                        }
                            .onFailure { showBundledThemePicker = true }
                    }
                },
                onExportTheme = { exportThemePicker.launch("${(appearance ?: LauncherAppearance(state.themeId)).themeId.name.lowercase()}-theme.zip") },
                onOpenOnlineThemes = { showRemoteThemes = true },
                onCreateCustomTheme = {
                    editingThemeId = null
                    editingThemeAppearance = null
                    editingThemeVersion = null
                    editorAssetBytes = emptyMap()
                    editorAssetUris = emptyMap()
                    compositionName = "Mi inicio"
                    editedComposition = ThemeComposition(listOf(
                        ThemeBlock(ThemeBlockType.TITLE),
                        ThemeBlock(ThemeBlockType.FAVORITES_ROW, maxItems = 5),
                        ThemeBlock(ThemeBlockType.APPS_GRID, maxItems = 12, columns = 4)
                    ))
                    showCompositionEditor = true
                },
                installedThemes = installedThemes,
                apps = successState?.apps ?: persistentListOf(),
                previewData = compositionPreviewData,
                onOpenDeviceTools = { showLauncherSettings = false; showDeviceTools = true },
                onOpenWeatherSettings = { showLauncherSettings = false; showWeatherSettings = true },
                onEditInstalledTheme = { themeId ->
                    coroutineScope.launch {
                        val installed = withContext(Dispatchers.IO) { installedThemeStore.load(themeId) }
                        val archiveBytes = withContext(Dispatchers.IO) { installedThemeStore.archiveBytes(themeId) }
                        if (installed != null && archiveBytes != null) {
                            editingThemeId = themeId
                            editingThemeAppearance = installed.appearance
                            editingThemeVersion = installed.version
                            compositionName = installed.name
                             editedComposition = installed.composition ?: ThemeComposition(listOf(
                                ThemeBlock(ThemeBlockType.TITLE),
                                ThemeBlock(ThemeBlockType.APPS_ROW, maxItems = 8)
                             ))
                             editorAssetBytes = ThemeArchiveReader.read(archiveBytes).assets
                             editorAssetUris = installed.assetUris
                            showCompositionEditor = true
                        } else {
                            Toast.makeText(context, R.string.theme_import_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onApplyInstalledTheme = { themeId ->
                    coroutineScope.launch {
                        val theme = withContext(Dispatchers.IO) { installedThemeStore.load(themeId) }
                        if (theme != null) onIntent(HomeIntent.ApplyImportedTheme(theme.appearance))
                    }
                },
                onDeleteInstalledTheme = { themeId ->
                    coroutineScope.launch {
                        val removedTheme = withContext(Dispatchers.IO) { installedThemeStore.load(themeId) }
                        val removed = withContext(Dispatchers.IO) { installedThemeStore.remove(themeId) }
                        val currentAppearance = appearance ?: LauncherAppearance(state.themeId)
                        if (removed && currentAppearance.installedThemeId == themeId) {
                            onIntent(HomeIntent.ApplyImportedTheme(
                                LauncherAppearance(themeId = currentAppearance.themeId, shortcuts = currentAppearance.shortcuts)
                            ))
                        } else if (
                            removed && removedTheme?.appearance?.backgroundImageUri != null &&
                            currentAppearance.backgroundImageUri == removedTheme.appearance.backgroundImageUri
                        ) {
                            onIntent(HomeIntent.ApplyImportedTheme(
                                currentAppearance.copy(backgroundImageUri = null, installedThemeId = null)
                            ))
                        }
                        themeLibraryRefresh++
                    }
                }
            )
        }
        if (showWeatherSettings) {
            WeatherSettingsDialog(
                state = weatherState,
                onIntent = onIntent,
                onDismiss = { showWeatherSettings = false }
            )
        }
        if (showDeviceTools) {
            DeviceToolsDialog(
                apps = successState?.apps?.toList().orEmpty(),
                onDismiss = { showDeviceTools = false }
            )
        }
        if (showRemoteThemes) {
            RemoteThemeCatalogDialog(
                catalogUrl = catalogUrl,
                onCatalogUrlChange = { catalogUrl = it },
                directZipUrl = directZipUrl,
                onDirectZipUrlChange = { directZipUrl = it },
                themes = remoteThemes,
                installedThemeVersions = installedThemes.associate { it.id to it.version },
                isLoading = isRemoteLoading,
                errorMessage = remoteErrorMessage,
                onLoadCatalog = ::loadRemoteCatalog,
                onDownloadDirectZip = { downloadRemoteTheme(directZipUrl) },
                onDownloadCatalogTheme = { theme -> downloadRemoteTheme(theme.downloadUrl, theme) },
                onDismiss = { showRemoteThemes = false }
            )
        }
        if (showCompositionEditor && state is HomeUiState.Success) {
            ThemeCompositionEditorDialog(
                name = compositionName,
                onNameChange = { if (!isSavingComposition) compositionName = it },
                composition = editedComposition,
                previewData = compositionPreviewData.withThemeAssets(editorAssetUris),
                availableAssetPaths = editorAssetBytes.keys.sorted(),
                onImportAssets = { editorAssetsPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp")) },
                onCompositionChange = { if (!isSavingComposition) editedComposition = it },
                isEditing = editingThemeId != null,
                isSaving = isSavingComposition,
                onSave = saveTheme@{
                    if (isSavingComposition || compositionName.isBlank()) return@saveTheme
                    isSavingComposition = true
                    coroutineScope.launch {
                        try {
                            val savedTheme = runCatching {
                                val existingThemeId = editingThemeId
                                val themeAppearance = editingThemeAppearance ?: state.appearance
                                val existingArchive = existingThemeId?.let { id ->
                                    withContext(Dispatchers.IO) { installedThemeStore.archiveBytes(id) }
                                }
                                val parsedExistingArchive = existingArchive?.let {
                                    withContext(Dispatchers.Default) { ThemeArchiveReader.read(it) }
                                }
                                val existingManifest = parsedExistingArchive?.let { archive ->
                                    withContext(Dispatchers.Default) {
                                        ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
                                    }
                                }
                                val backgroundAsset = if (parsedExistingArchive != null && existingManifest != null) {
                                    (existingManifest.background as? ThemeManifestBackground.Asset)?.let { background ->
                                        parsedExistingArchive.assets[background.path]
                                    }
                                } else themeAppearance.backgroundImageUri?.let { imageUri ->
                                    withContext(Dispatchers.IO) {
                                        val bitmap = decodeSampledBitmap(context.contentResolver, Uri.parse(imageUri), MAX_BACKGROUND_IMAGE_SIDE)
                                            ?: error("Invalid background image")
                                        ByteArrayOutputStream().use { output ->
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                                            output.toByteArray()
                                        }
                                    }
                                }
                                val archive = withContext(Dispatchers.Default) {
                                    ThemeArchiveWriter.write(
                                        themeAppearance.copy(installedThemeId = null),
                                        backgroundAsset,
                                        composition = editedComposition,
                                        name = compositionName,
                                        id = existingThemeId ?: java.util.UUID.randomUUID().toString(),
                                        additionalAssets = editorAssetBytes
                                    )
                                }
                                withContext(Dispatchers.IO) {
                                    installedThemeStore.install(
                                        archiveBytes = archive,
                                        version = editingThemeVersion,
                                        replaceExisting = existingThemeId != null
                                    )
                                }
                            }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }.getOrNull()
                            if (savedTheme == null) {
                                Toast.makeText(context, R.string.theme_import_failed, Toast.LENGTH_LONG).show()
                            } else {
                                showCompositionEditor = false
                                val updatedThemeId = editingThemeId
                                if (updatedThemeId == null || state.appearance.installedThemeId == updatedThemeId) {
                                    val updated = withContext(Dispatchers.IO) {
                                        updatedThemeId?.let(installedThemeStore::load)
                                    }
                                    onIntent(HomeIntent.ApplyImportedTheme(updated?.appearance ?: savedTheme.appearance))
                                }
                                installedThemes = withContext(Dispatchers.IO) { installedThemeStore.list() }
                                themeLibraryRefresh++
                                editingThemeId = null
                                editingThemeAppearance = null
                                editingThemeVersion = null
                            }
                        } finally {
                            isSavingComposition = false
                        }
                    }
                },
                onCancel = {
                    showCompositionEditor = false
                    editingThemeId = null
                    editingThemeAppearance = null
                    editingThemeVersion = null
                }
            )
        }
        if (showBundledThemePicker) {
            Dialog(
                onDismissRequest = { showBundledThemePicker = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(0.72f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(stringResource(R.string.bundled_demo_themes), fontSize = 28.sp)
                        Text(stringResource(R.string.no_document_picker_hint))
                        BUNDLED_DEMO_THEMES.forEach { (fileName, label) ->
                            Button(onClick = {
                                coroutineScope.launch {
                                    val imported = runCatching {
                                        val bytes = withContext(Dispatchers.IO) {
                                            context.assets.open("demo-themes/$fileName").use {
                                                it.readBounded(20 * 1024 * 1024)
                                            }
                                        }
                                        val manifest = withContext(Dispatchers.Default) {
                                            val archive = ThemeArchiveReader.read(bytes)
                                            ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
                                        }
                                        PendingThemeImport(manifest, bytes)
                                    }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }.getOrNull()
                                    if (imported == null) {
                                        Toast.makeText(context, R.string.theme_import_failed, Toast.LENGTH_LONG).show()
                                    } else {
                                        pendingTheme = imported
                                        showBundledThemePicker = false
                                    }
                                }
                            }) { Text(label) }
                        }
                        Button(onClick = { showBundledThemePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
        pendingTheme?.let { pending ->
            Dialog(
                onDismissRequest = { pendingTheme = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(0.72f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        Modifier.padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.theme_import_preview), fontSize = 30.sp)
                        Text(stringResource(R.string.theme_preview_description, pending.manifest.name, pending.manifest.layout.name))
                        if (installedThemes.any { it.id == pending.manifest.id }) {
                            Text(stringResource(R.string.theme_update_confirmation))
                        }
                        val backgroundColor = (pending.manifest.background as? ThemeManifestBackground.Solid)?.argb
                        Surface(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = androidx.tv.material3.SurfaceDefaults.colors(
                                containerColor = backgroundColor?.let { androidx.compose.ui.graphics.Color(it.toInt()) }
                                    ?: LocalLauncherStyle.current.palette.surface
                            )
                        ) { }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = { pendingTheme = null }) { Text(stringResource(R.string.cancel)) }
                            Button(onClick = {
                                coroutineScope.launch {
                                    val importedAppearance = runCatching {
                                        val existingTheme = withContext(Dispatchers.IO) {
                                            installedThemeStore.load(pending.manifest.id) != null
                                        }
                                        withContext(Dispatchers.IO) {
                                            installedThemeStore.install(
                                                archiveBytes = pending.archiveBytes,
                                                version = pending.catalogVersion,
                                                replaceExisting = existingTheme
                                            )
                                        }.appearance
                                    }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }.getOrNull()
                                    if (importedAppearance == null) {
                                        Toast.makeText(context, R.string.theme_import_failed, Toast.LENGTH_LONG).show()
                                    } else {
                                        onIntent(HomeIntent.ApplyImportedTheme(importedAppearance))
                                        installedThemes = withContext(Dispatchers.IO) { installedThemeStore.list() }
                                        themeLibraryRefresh++
                                        pendingTheme = null
                                    }
                                }
                            }) { Text(stringResource(R.string.apply_theme)) }
                        }
                    }
                }
            }
        }
        }
    }
}

private fun openSystemSettings(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
    if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }.onFailure {
        Toast.makeText(context, R.string.system_settings_unavailable, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun AppDrawerScreen(
    apps: List<com.tv.launcher.custom.core.model.apps.AppEntry>,
    onLaunch: (String) -> Unit
) {
    var focusedPackage by rememberSaveable { mutableStateOf<String?>(null) }
    val focusTarget = apps.firstOrNull { it.packageName == focusedPackage } ?: apps.firstOrNull()
    val firstAppFocus = remember { FocusRequester() }
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val style = com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle.current
    val cardShape = androidx.compose.foundation.shape.RoundedCornerShape(style.tileRadius)
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.launcher_all_apps), fontSize = 28.sp, color = style.palette.onBackground)
                Text(androidx.compose.ui.res.pluralStringResource(R.plurals.launcher_app_count, apps.size, apps.size), fontSize = 16.sp, color = style.palette.onMuted)
            }
            if (apps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.launcher_no_apps), color = style.palette.onMuted)
                }
            } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(144.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    androidx.tv.material3.Surface(
                        onClick = { onLaunch(app.packageName) },
                        shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(cardShape),
                        colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
                            containerColor = style.palette.surface,
                            contentColor = style.palette.onBackground,
                            focusedContainerColor = style.palette.surface,
                            focusedContentColor = style.palette.onBackground
                        ),
                        scale = androidx.tv.material3.ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
                        border = androidx.tv.material3.ClickableSurfaceDefaults.border(
                            focusedBorder = androidx.tv.material3.Border(
                                androidx.compose.foundation.BorderStroke(3.dp, style.palette.accent), shape = cardShape
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                            .then(if (app == focusTarget) Modifier.focusRequester(firstAppFocus) else Modifier)
                            .onFocusChanged { if (it.isFocused) focusedPackage = app.packageName }
                    ) {
                        Column(Modifier.fillMaxWidth().height(152.dp).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                            if (isPreview) Text(app.label.take(2))
                            else AppIcon(app.packageName, Modifier.size(56.dp))
                            Text(app.label, maxLines = 2, minLines = 2, fontSize = 16.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            }
        }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Apps · Contenido integrado", widthDp = 640, heightDp = 460)
@Composable
private fun AppDrawerScreenPreview() = LauncherTheme(ThemeId.ROKU) {
    AppDrawerScreen(
        apps = listOf(
            com.tv.launcher.custom.core.model.apps.AppEntry("sample.video", "Video"),
            com.tv.launcher.custom.core.model.apps.AppEntry("sample.music", "Música")
        ),
        onLaunch = {}
    )
}

private val BUNDLED_DEMO_THEMES = listOf(
    "webos-demo.zip" to "webOS",
    "fire-tv-demo.zip" to "Fire TV",
    "roku-demo.zip" to "Roku",
    "tizen-demo.zip" to "Tizen",
    "android-tv-demo.zip" to "Android TV",
    "google-tv-demo.zip" to "Google TV",
    "apple-tv-demo.zip" to "Apple TV",
    "free-my-tv-demo.zip" to "Free My TV",
    "launch-dock-demo.zip" to "Launch Dock",
    "side-stream-demo.zip" to "Side Stream"
)

@Composable
private fun CenteredMessage(
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(message, fontSize = 32.sp)
            if (actionLabel != null) {
                Button(onClick = onAction, shape = androidx.tv.material3.ButtonDefaults.shape(RoundedCornerShape(50))) {
                    Text(actionLabel)
                }
            }
        }
    }
}
