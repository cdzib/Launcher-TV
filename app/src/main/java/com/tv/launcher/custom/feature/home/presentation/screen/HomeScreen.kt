package com.tv.launcher.custom.feature.home.presentation.screen

import android.widget.Toast
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.tv.launcher.custom.feature.home.presentation.components.AppBrowserDialog
import com.tv.launcher.custom.feature.home.presentation.components.RemoteThemeCatalogDialog
import com.tv.launcher.custom.feature.home.presentation.components.ThemeCompositionEditorDialog
import com.tv.launcher.custom.feature.home.presentation.layout.CustomThemeLayout
import com.tv.launcher.custom.feature.home.presentation.layout.InstalledAppsCanvasData
import com.tv.launcher.custom.feature.home.presentation.components.LocalOpenApps
import com.tv.launcher.custom.feature.home.presentation.components.LocalShortcutPackages
import com.tv.launcher.custom.feature.home.presentation.components.LocalSetShortcut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
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
import com.tv.launcher.custom.R
import com.tv.launcher.custom.core.theme.LauncherTheme
import com.tv.launcher.custom.core.theme.LayoutKind
import com.tv.launcher.custom.core.theme.LocalLauncherStyle
import com.tv.launcher.custom.feature.home.presentation.layout.BottomBarLayout
import com.tv.launcher.custom.feature.home.presentation.layout.SideGridLayout
import com.tv.launcher.custom.feature.home.presentation.layout.TopNavLayout
import com.tv.launcher.custom.feature.home.presentation.layout.AndroidTvLayout
import com.tv.launcher.custom.feature.home.presentation.layout.GoogleTvLayout
import com.tv.launcher.custom.feature.home.presentation.layout.AppleTvLayout
import com.tv.launcher.custom.feature.home.presentation.layout.TizenLayout
import com.tv.launcher.custom.feature.home.presentation.components.LauncherSettingsDialog
import com.tv.launcher.custom.feature.home.presentation.state.HomeEffect
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.home.presentation.state.HomeUiState
import com.tv.launcher.custom.feature.home.presentation.viewmodel.HomeViewModel
import com.tv.launcher.custom.feature.theme.data.ThemeArchiveReader
import com.tv.launcher.custom.feature.theme.data.ThemeArchiveWriter
import com.tv.launcher.custom.feature.theme.data.InstalledTheme
import com.tv.launcher.custom.feature.theme.data.InstalledThemeStore
import com.tv.launcher.custom.feature.theme.data.ThemeManifest
import com.tv.launcher.custom.feature.theme.data.ThemeManifestBackground
import com.tv.launcher.custom.feature.theme.data.ThemeManifestValidator
import com.tv.launcher.custom.feature.theme.data.ThemeCatalogParser
import com.tv.launcher.custom.feature.theme.data.ThemeDownloadClient
import com.tv.launcher.custom.feature.theme.data.RemoteTheme
import com.tv.launcher.custom.feature.theme.data.verifySha256
import com.tv.launcher.custom.feature.theme.data.ThemeBlock
import com.tv.launcher.custom.feature.theme.data.ThemeBlockType
import com.tv.launcher.custom.feature.theme.data.ThemeComposition
import com.tv.launcher.custom.feature.theme.data.CanvasData
import com.tv.launcher.custom.feature.theme.data.PlaceholderCanvasData
import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch
import java.io.InputStream

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
fun HomeRoute(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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

    HomeScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun HomeScreen(state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    val appearance = (state as? HomeUiState.Success)?.appearance
    var showLauncherSettings by remember { mutableStateOf(false) }
    var showApps by remember { mutableStateOf(false) }
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
    var installedThemes by remember { mutableStateOf(emptyList<InstalledTheme>()) }
    var themeLibraryRefresh by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val installedThemeStore = remember(context) {
        InstalledThemeStore(java.io.File(context.filesDir, "installed_themes"))
    }
    val compositionPreviewData: CanvasData = remember(state, onIntent, context) {
        val success = state as? HomeUiState.Success
        val apps = success?.apps.orEmpty()
        val favoritePackages = success?.appearance?.shortcuts?.values?.toSet().orEmpty()
        InstalledAppsCanvasData(
            apps = apps,
            favorites = apps.filter { it.packageName in favoritePackages },
            onLaunch = { packageName -> onIntent(HomeIntent.Launch(packageName)) },
            onOpenApps = { showApps = true },
            onOpenSettings = { showLauncherSettings = true },
            onUnavailableNavigation = {}
        )
    }
    LaunchedEffect(appearance?.installedThemeId, themeLibraryRefresh) {
        val themeId = appearance?.installedThemeId
        val active = if (themeId == null) null else withContext(Dispatchers.IO) { installedThemeStore.load(themeId) }
        activeComposition = active?.composition
        activeCompositionName = active?.name.orEmpty()
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
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBounded(20 * 1024 * 1024) }
                        ?: error("Could not open theme package")
                }
                val archive = ThemeArchiveReader.read(bytes)
                val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
                PendingThemeImport(manifest, bytes)
            }.getOrNull()
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
                        context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { input ->
                            val bitmap = BitmapFactory.decodeStream(input) ?: error("Invalid background image")
                            ByteArrayOutputStream().use { output ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                                output.toByteArray()
                            }
                        }
                    }
                }
                val installedArchive = activeAppearance.installedThemeId?.let { id ->
                    withContext(Dispatchers.IO) { installedThemeStore.archiveBytes(id) }
                }
                val archive = installedArchive ?: ThemeArchiveWriter.write(activeAppearance, imageBytes)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "w")?.use { it.write(archive) }
                        ?: error("Could not write theme package")
                }
            }.isSuccess
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
                context.contentResolver.openInputStream(Uri.parse(appearanceImage))?.use(BitmapFactory::decodeStream)?.asImageBitmap()
            }.getOrNull()
        }
    }
    LauncherTheme(appearance ?: com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance(state.themeId)) {
        val palette = LocalLauncherStyle.current.palette
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
                     LocalOpenApps provides { showApps = true },
                     LocalShortcutPackages provides state.appearance.shortcuts,
                     LocalSetShortcut provides { slot, packageName -> onIntent(HomeIntent.SelectShortcut(slot, packageName)) }
                 ) {
                    LauncherContent(
                     state = state,
                     onIntent = onIntent,
                     onOpenSettings = { showLauncherSettings = true },
                      onUnavailableNavigation = { label ->
                          Toast.makeText(context, context.getString(R.string.custom_section_unavailable, label), Toast.LENGTH_SHORT).show()
                      },
                      customComposition = activeComposition,
                     customThemeName = activeCompositionName
                   )
                 }
            }
          }
        }
        if (showLauncherSettings && state is HomeUiState.Success) {
            LauncherSettingsDialog(
                appearance = state.appearance,
                onIntent = onIntent,
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
                onExportTheme = { exportThemePicker.launch("${state.appearance.themeId.name.lowercase()}-theme.zip") },
                onOpenOnlineThemes = { showRemoteThemes = true },
                onCreateCustomTheme = {
                    editingThemeId = null
                    editingThemeAppearance = null
                    editingThemeVersion = null
                    compositionName = "Mi inicio"
                    editedComposition = ThemeComposition(listOf(
                        ThemeBlock(ThemeBlockType.TITLE),
                        ThemeBlock(ThemeBlockType.FAVORITES_ROW, maxItems = 5),
                        ThemeBlock(ThemeBlockType.APPS_GRID, maxItems = 12, columns = 4)
                    ))
                    showCompositionEditor = true
                },
                installedThemes = installedThemes,
                apps = state.apps,
                previewData = compositionPreviewData,
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
                        if (removed && state.appearance.installedThemeId == themeId) {
                            onIntent(HomeIntent.ApplyImportedTheme(
                                LauncherAppearance(themeId = state.appearance.themeId, shortcuts = state.appearance.shortcuts)
                            ))
                        } else if (
                            removed && removedTheme?.appearance?.backgroundImageUri != null &&
                            state.appearance.backgroundImageUri == removedTheme.appearance.backgroundImageUri
                        ) {
                            onIntent(HomeIntent.ApplyImportedTheme(
                                state.appearance.copy(backgroundImageUri = null, installedThemeId = null)
                            ))
                        }
                        themeLibraryRefresh++
                    }
                }
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
                previewData = compositionPreviewData,
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
                                val parsedExistingArchive = existingArchive?.let(ThemeArchiveReader::read)
                                val existingManifest = parsedExistingArchive?.let { archive ->
                                    ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
                                }
                                val backgroundAsset = if (parsedExistingArchive != null && existingManifest != null) {
                                    (existingManifest.background as? ThemeManifestBackground.Asset)?.let { background ->
                                        parsedExistingArchive.assets[background.path]
                                    }
                                } else themeAppearance.backgroundImageUri?.let { imageUri ->
                                    withContext(Dispatchers.IO) {
                                        context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { input ->
                                            val bitmap = BitmapFactory.decodeStream(input) ?: error("Invalid background image")
                                            ByteArrayOutputStream().use { output ->
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                                                output.toByteArray()
                                            }
                                        }
                                    }
                                }
                                val archive = ThemeArchiveWriter.write(
                                    themeAppearance.copy(installedThemeId = null),
                                    backgroundAsset,
                                    composition = editedComposition,
                                    name = compositionName,
                                    id = existingThemeId ?: java.util.UUID.randomUUID().toString()
                                )
                                withContext(Dispatchers.IO) {
                                    installedThemeStore.install(
                                        archiveBytes = archive,
                                        version = editingThemeVersion,
                                        replaceExisting = existingThemeId != null
                                    )
                                }
                            }.getOrNull()
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
                                        val archive = ThemeArchiveReader.read(bytes)
                                        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
                                        PendingThemeImport(manifest, bytes)
                                    }.getOrNull()
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
                                    }.getOrNull()
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
        if (showApps && state is HomeUiState.Success) {
            AppBrowserDialog(
                apps = state.apps,
                onLaunch = { packageName -> onIntent(HomeIntent.Launch(packageName)) },
                onDismiss = { showApps = false }
            )
        }
    }
}

private val BUNDLED_DEMO_THEMES = listOf(
    "webos-demo.zip" to "webOS",
    "fire-tv-demo.zip" to "Fire TV",
    "roku-demo.zip" to "Roku",
    "tizen-demo.zip" to "Tizen",
    "android-tv-demo.zip" to "Android TV",
    "google-tv-demo.zip" to "Google TV",
    "apple-tv-demo.zip" to "Apple TV"
)

@Composable
private fun LauncherContent(
    state: HomeUiState.Success,
    onIntent: (HomeIntent) -> Unit,
    onOpenSettings: () -> Unit,
    onUnavailableNavigation: (String) -> Unit = {},
    customComposition: ThemeComposition? = null,
    customThemeName: String = ""
) {
    val layout = LocalLauncherStyle.current.layout
    val firstItemFocus = remember { FocusRequester() }
    val onLaunch = { packageName: String -> onIntent(HomeIntent.Launch(packageName)) }
    val onOpenApps = LocalOpenApps.current
    val installedCanvasData = remember(state.apps, state.appearance.shortcuts, onIntent, onOpenSettings, onOpenApps, onUnavailableNavigation) {
        val favoritePackages = state.appearance.shortcuts.values.toSet()
        InstalledAppsCanvasData(
            apps = state.apps,
            favorites = state.apps.filter { it.packageName in favoritePackages },
            onLaunch = onLaunch,
            onOpenApps = onOpenApps,
            onOpenSettings = onOpenSettings,
            onUnavailableNavigation = onUnavailableNavigation
        )
    }
    LaunchedEffect(layout, customComposition, state.apps) {
        if (customComposition != null) {
            val firstBlockWithApps = customComposition.blocks.indexOfFirst { block ->
                block.type in setOf(
                    ThemeBlockType.APPS_ROW,
                    ThemeBlockType.APPS_GRID,
                    ThemeBlockType.FAVORITES_ROW,
                    ThemeBlockType.RECENTS_ROW
                ) && when (block.type) {
                    ThemeBlockType.FAVORITES_ROW -> state.apps.any { app -> app.packageName in state.appearance.shortcuts.values }
                    ThemeBlockType.RECENTS_ROW -> false
                    else -> state.apps.isNotEmpty()
                }
            }
            if (firstBlockWithApps >= 0) runCatching { firstItemFocus.requestFocus() }
        } else {
            runCatching { firstItemFocus.requestFocus() }
        }
    }

    if (customComposition != null) {
        CustomThemeLayout(
            customThemeName,
            customComposition,
            installedCanvasData,
            firstItemFocus,
            onOpenSettings
        )
        return
    }

    when (layout) {
        LayoutKind.BOTTOM_FLOATING -> com.tv.launcher.custom.feature.home.presentation.layout.WebOsLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.BOTTOM_EXPANDING -> BottomBarLayout(state.apps, true, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.TOP_NAV -> TopNavLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.SIDE_GRID -> SideGridLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.ANDROID_GRID -> AndroidTvLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.GOOGLE_HOME -> GoogleTvLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.APPLE_HOME -> AppleTvLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.TIZEN_HOME -> TizenLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
    }
}

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
