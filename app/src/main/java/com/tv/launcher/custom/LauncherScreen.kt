package com.tv.launcher.custom

import com.tv.launcher.custom.core.ui.components.sectionSurfaceBackground

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.Manifest
import androidx.core.content.ContextCompat
import com.tv.launcher.custom.files.BrowserRoot
import com.tv.launcher.custom.files.FileBrowserDialog
import com.tv.launcher.custom.files.FileBrowserMode
import java.io.File
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
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
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
import com.tv.launcher.custom.core.ui.theme.toStyle
import com.tv.launcher.custom.core.ui.theme.LocalBackgroundImageVisible
import com.tv.launcher.custom.core.ui.theme.LocalBackgroundImage
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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
private const val MAX_BACKGROUND_IMAGE_SIDE = 1920

private data class PendingThemeImport(
    val manifest: ThemeManifest,
    val archiveBytes: ByteArray,
    val catalogVersion: String? = null,
    val previewBackgroundUri: String? = null
)

/** Materializes the bundled background asset so import previews show the real wallpaper. */
private fun themePreviewBackgroundUri(
    context: Context,
    archive: ThemeArchive,
    manifest: ThemeManifest
): String? = (manifest.background as? ThemeManifestBackground.Asset)?.let { background ->
    archive.assets[background.path]?.let { bytes ->
        runCatching {
            val directory = File(context.cacheDir, "theme-previews").apply { mkdirs() }
            val file = File(directory, "${manifest.id}.png")
            file.writeBytes(bytes)
            file.toURI().toString()
        }.getOrNull()
    }
}

private suspend fun fetchRemoteCatalog(url: String): List<RemoteTheme> = withContext(Dispatchers.IO) {
    val bytes = ThemeDownloadClient().download(url, 1024 * 1024)
    ThemeCatalogParser.parse(bytes)
}

private suspend fun fetchRemoteTheme(
    context: Context,
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
    PendingThemeImport(
        manifest = manifest,
        archiveBytes = archiveBytes,
        catalogVersion = catalogTheme?.version,
        previewBackgroundUri = themePreviewBackgroundUri(context, archive, manifest)
    )
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

private fun hasUsableDocumentsPicker(context: Context, mimeType: String = "application/zip"): Boolean {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = mimeType
        addCategory(Intent.CATEGORY_OPENABLE)
    }
    return context.packageManager
        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        .any { !it.activityInfo.name.endsWith("DocumentsStub") }
}

private fun hasStorageAccess(context: Context): Boolean = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Environment.isExternalStorageManager() ||
        (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED)
    else -> ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
        PackageManager.PERMISSION_GRANTED
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }.onFailure { openSystemSettings(context) }
}

private fun openAllFilesAccessSettings(context: Context) {
    val appSettings = Intent(
        android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    runCatching { context.startActivity(appSettings) }.onFailure {
        runCatching {
            context.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
    }
}

/** Directories offered by the in-app picker: public storage when permitted plus app-owned folders. */
private fun browserRoots(context: Context): List<BrowserRoot> {
    val roots = mutableListOf<BrowserRoot>()
    if (hasStorageAccess(context)) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ?.takeIf { it.canRead() }?.let { roots += BrowserRoot(context.getString(R.string.file_browser_root_downloads), it) }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            ?.takeIf { it.canRead() }?.let { roots += BrowserRoot(context.getString(R.string.file_browser_root_pictures), it) }
        Environment.getExternalStorageDirectory()
            ?.takeIf { it.canRead() }?.let { roots += BrowserRoot(context.getString(R.string.file_browser_root_internal), it) }
        context.getExternalFilesDirs(null).filterNotNull().forEach { appDirectory ->
            val volume = generateSequence(appDirectory) { it.parentFile }.elementAtOrNull(4) ?: return@forEach
            if (volume != Environment.getExternalStorageDirectory() && volume.canRead()) {
                roots += BrowserRoot(context.getString(R.string.file_browser_root_usb, volume.name), volume)
            }
        }
    }
    context.getExternalFilesDir(null)?.let { roots += BrowserRoot(context.getString(R.string.file_browser_root_app), it) }
    return roots.distinctBy { it.directory.absolutePath }
}

/** Themes that do not declare their own recents row get one once the user has opened two apps. */
private fun ThemeComposition.withRecentAppsRow(hasRecentApps: Boolean): ThemeComposition {
    if (!hasRecentApps || blocks.any { it.type == ThemeBlockType.RECENTS_ROW }) return this
    return copy(blocks = blocks + ThemeBlock(
        ThemeBlockType.RECENTS_ROW,
        text = "Recientes",
        maxItems = 8,
        itemHeight = 76,
        aspect = CardAspect.WIDE,
        cardStyle = CardStyle.FILLED,
        cornerRadius = 12,
        labelPos = LabelPos.BELOW,
        spacing = 16,
        textSize = 11
    ))
}

private fun persistBackgroundImage(context: Context, source: File): Uri? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(source.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) error("Invalid image")
    val options = BitmapFactory.Options().apply {
        inSampleSize = ImageSampling.calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_BACKGROUND_IMAGE_SIDE)
    }
    val bitmap = BitmapFactory.decodeFile(source.absolutePath, options) ?: error("Invalid image")
    val directory = File(context.filesDir, "backgrounds").apply { mkdirs() }
    directory.listFiles()?.forEach { it.delete() }
    val target = File(directory, "background-${java.util.UUID.randomUUID()}.png")
    target.outputStream().use { output -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, output) }
    Uri.fromFile(target)
}.getOrNull()

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
    // Section is transient navigation state: restoring SEARCH from a saved composition can
    // make a freshly launched launcher appear to start in the wrong destination.
    var section by remember { mutableStateOf(LauncherSection.HOME) }
    var appsFilter by rememberSaveable { mutableStateOf(AppsFilter.ALL) }
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
    var editorBaseBackgroundAsset by remember { mutableStateOf<ByteArray?>(null) }
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
    var fileBrowserMode by rememberSaveable { mutableStateOf<FileBrowserMode?>(null) }
    var pendingFileBrowserMode by remember { mutableStateOf<FileBrowserMode?>(null) }
    fun importEditorAssets(uris: List<Uri>) {
        if (uris.isEmpty()) return
        coroutineScope.launch {
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
    fun importThemeArchive(bytes: ByteArray) {
        coroutineScope.launch {
            val imported = runCatching {
                val (archive, manifest) = withContext(Dispatchers.Default) {
                    val parsed = ThemeArchiveReader.read(bytes)
                    parsed to ThemeManifestValidator.parse(parsed.manifest, parsed.assets.keys)
                }
                val previewBackground = withContext(Dispatchers.IO) {
                    themePreviewBackgroundUri(context, archive, manifest)
                }
                PendingThemeImport(manifest, bytes, previewBackgroundUri = previewBackground)
            }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }.getOrNull()
            if (imported == null) {
                Toast.makeText(context, R.string.theme_import_failed, Toast.LENGTH_LONG).show()
            } else {
                pendingTheme = imported
            }
        }
    }
    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val mode = pendingFileBrowserMode
        pendingFileBrowserMode = null
        if (granted && mode != null) {
            fileBrowserMode = mode
        } else {
            Toast.makeText(context, R.string.file_browser_permission_denied, Toast.LENGTH_LONG).show()
        }
    }
    fun openFileBrowser(mode: FileBrowserMode) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && !hasStorageAccess(context)) {
            pendingFileBrowserMode = mode
            storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            fileBrowserMode = mode
        }
    }
    fun requestStorageAccess() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> openAllFilesAccessSettings(context)
            else -> {
                pendingFileBrowserMode = fileBrowserMode
                storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var storageRefresh by remember { mutableStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) storageRefresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(fileBrowserMode) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // USB removed/reconnected while browsing: refresh roots without closing the dialog.
                storageRefresh++
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_EJECT)
            addAction(Intent.ACTION_MEDIA_REMOVED)
        }
        runCatching { context.registerReceiver(receiver, filter) }
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    val editorAssetsPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        importEditorAssets(uris)
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
        val recents = success?.appearance?.recentPackages.orEmpty().mapNotNull { packageName ->
            apps.firstOrNull { it.packageName == packageName }
        }
        InstalledAppsCanvasData(
            apps = apps,
            favorites = apps.filter { it.packageName in favoritePackages },
            onLaunch = { packageName -> onIntent(HomeIntent.Launch(packageName)) },
            onOpenApps = { appsFilter = AppsFilter.ALL; section = LauncherSection.APPS },
            onOpenAppsFiltered = { filter -> appsFilter = filter; section = LauncherSection.APPS },
            onOpenSettings = { openSystemSettings(context) },
            onOpenPersonalization = { showLauncherSettings = true },
            onOpenSearch = { section = LauncherSection.SEARCH },
            onUnavailableNavigation = {},
            assetUris = activeThemeAssetUris,
            recents = recents,
            onOpenProfile = { showLauncherSettings = true },
            onOpenNotifications = { openNotificationSettings(context) }
        )
    }
    LaunchedEffect(appearance?.installedThemeId, appearance?.themeId, themeLibraryRefresh) {
        val themeId = appearance?.installedThemeId
        val active = if (themeId == null) null else withContext(Dispatchers.IO) { installedThemeStore.load(themeId) }
        activeComposition = active?.composition
            ?: appearance?.themeId?.let { com.tv.launcher.custom.core.theme.defaultCompositionFor(it) }
        activeCompositionName = active?.name ?: appearance?.themeId?.let { context.getString(it.toStyle().nameRes) }.orEmpty()
        activeThemeAssetUris = active?.assetUris.orEmpty()
    }
    val homeComposition = remember(activeComposition, appearance?.recentPackages) {
        activeComposition?.withRecentAppsRow((appearance?.recentPackages?.size ?: 0) >= 2)
    }
    // Primera instalación: deja un tema editable aplicado para que Inicio nunca quede vacío.
    LaunchedEffect(Unit) {
        val hadThemes = withContext(Dispatchers.IO) { installedThemeStore.list().isNotEmpty() }
        if (hadThemes) return@LaunchedEffect
        val installed = withContext(Dispatchers.IO) {
            val bytes = context.assets.open("demo-themes/webos-editable.zip").use { it.readBounded(20 * 1024 * 1024) }
            installedThemeStore.initializeDefaultTheme(bytes)
            installedThemeStore.list().firstOrNull()
        }
        if (installed != null && appearance?.installedThemeId == null) {
            onIntent(HomeIntent.ApplyImportedTheme(installed.appearance))
        }
        themeLibraryRefresh++
    }
    LaunchedEffect(appearance?.themeId, appearance?.installedThemeId) {
        section = LauncherSection.HOME
    }
    LaunchedEffect(showLauncherSettings, themeLibraryRefresh) {
        if (showLauncherSettings && !showCompositionEditor) {
            installedThemes = withContext(Dispatchers.IO) {
                val defaultThemePackage = context.assets.open("demo-themes/webos-editable.zip").use {
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
            val bytes = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBounded(20 * 1024 * 1024) }
                }
            }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }.getOrNull()
            if (bytes == null) {
                Toast.makeText(context, R.string.theme_import_failed, Toast.LENGTH_LONG).show()
            } else {
                importThemeArchive(bytes)
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
                pendingTheme = fetchRemoteTheme(context, url, catalogTheme)
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
    val wallpaperMotion = rememberInfiniteTransition(label = "wallpaper")
    val wallpaperScale by wallpaperMotion.animateFloat(
        initialValue = 1.04f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 32000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wallpaperScale"
    )
    val wallpaperDriftX by wallpaperMotion.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 24000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wallpaperDriftX"
    )
    val wallpaperDriftY by wallpaperMotion.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 19000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wallpaperDriftY"
    )
    val wallpaperParallax by animateFloatAsState(
        targetValue = if (section == LauncherSection.HOME) 0f else -18f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "wallpaperParallax"
    )
    LauncherTheme(appearance ?: LauncherAppearance(state.themeId)) {
        val palette = LocalLauncherStyle.current.palette
         CompositionLocalProvider(
             LocalBackgroundImageVisible provides (backgroundBitmap != null),
             LocalBackgroundImage provides backgroundBitmap,
             com.tv.launcher.custom.core.ui.components.LocalCanvasWeatherContent provides
                 (if (weatherState.settings.isEnabled && weatherState.settings.city != null) { widgetModifier ->
                     WeatherSummaryCard(
                         state = weatherState,
                         backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
                          contentColor = LocalLauncherStyle.current.palette.onBackground,
                          mutedColor = LocalLauncherStyle.current.palette.onBackground.copy(alpha = 0.8f),
                         onIntent = onIntent,
                         onOpenSettings = { showWeatherSettings = true },
                         modifier = widgetModifier,
                          stackActions = true,
                          compact = true
                     )
                 } else null)
         ) {
        Box(Modifier.fillMaxSize().clipToBounds().background(palette.background)) {
          if (backgroundBitmap != null) Image(
              backgroundBitmap!!, null,
              Modifier.fillMaxSize().graphicsLayer {
                  scaleX = wallpaperScale
                  scaleY = wallpaperScale
                  translationX = wallpaperDriftX + wallpaperParallax
                  translationY = wallpaperDriftY
              },
              contentScale = ContentScale.Crop
          )
          Surface(
            modifier = Modifier.fillMaxSize(), shape = RectangleShape,
            colors = androidx.tv.material3.SurfaceDefaults.colors(
                containerColor = if (backgroundBitmap != null) androidx.compose.ui.graphics.Color.Transparent else palette.background,
                contentColor = palette.onBackground
            )
          ) {
            when (state) {
                is HomeUiState.Loading -> LauncherLoadingSplash()
                is HomeUiState.Empty -> CenteredMessage(stringResource(R.string.empty_apps))
                is HomeUiState.Error -> CenteredMessage(
                    message = stringResource(R.string.error_load),
                    actionLabel = stringResource(R.string.retry),
                    onAction = { onIntent(HomeIntent.Retry) }
                )
                  is HomeUiState.Success -> CompositionLocalProvider(
                      LocalOpenHome provides { section = LauncherSection.HOME },
                      LocalOpenApps provides { appsFilter = AppsFilter.ALL; section = LauncherSection.APPS },
                      LocalOpenSearch provides { section = LauncherSection.SEARCH },
                      LocalLauncherSection provides section,
                      LocalLauncherSectionContent provides if (section == LauncherSection.HOME) null else {
                          @Composable {
                              var appeared by remember(section) { mutableStateOf(false) }
                              val progress by animateFloatAsState(
                                  targetValue = if (appeared) 1f else 0f,
                                  animationSpec = spring(dampingRatio = 0.9f, stiffness = 280f),
                                  label = "sectionEnter"
                              )
                              LaunchedEffect(section) { appeared = true }
                              Box(Modifier.fillMaxSize().graphicsLayer {
                                  alpha = progress
                                  translationX = (1f - progress) * 42.dp.toPx()
                                  val scale = 0.99f + 0.01f * progress
                                  scaleX = scale
                                  scaleY = scale
                              }) {
                                  sectionStateHolder.SaveableStateProvider(section.name) {
                                      when (section) {
                                          LauncherSection.APPS -> AppDrawerScreen(
                                              apps = state.apps,
                                              onLaunch = { onIntent(HomeIntent.Launch(it)) },
                                              filter = appsFilter,
                                              favorites = state.appearance.shortcuts.values.toSet(),
                                              recents = state.appearance.recentPackages,
                                              onFilterChange = { appsFilter = it }
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
                      customComposition = homeComposition,
                      customThemeName = activeCompositionName,
                      customThemeAssets = activeThemeAssetUris,
                      onOpenPersonalization = { showLauncherSettings = true },
                      onOpenSearch = { section = LauncherSection.SEARCH },
                      onOpenAppsFiltered = { filter -> appsFilter = filter; section = LauncherSection.APPS },
                      onOpenProfile = { showLauncherSettings = true },
                      onOpenNotifications = { openNotificationSettings(context) }
                      )
                    }
                   }
            }
           }
            if (section == LauncherSection.HOME && state !is HomeUiState.Loading && state !is HomeUiState.Error &&
                activeComposition?.widgets?.none { it.type == com.tv.launcher.custom.core.model.theme.ThemeWidgetType.WEATHER } != false) {
               val palette = LocalLauncherStyle.current.palette
               WeatherSummaryCard(
                   state = weatherState,
                   backgroundColor = if (backgroundBitmap != null) androidx.compose.ui.graphics.Color.Transparent else palette.surface,
                   contentColor = palette.onBackground,
                   mutedColor = palette.onMuted,
                   onIntent = onIntent,
                   onOpenSettings = { showWeatherSettings = true },
                   modifier = Modifier.align(Alignment.TopStart).padding(24.dp)
               )
           }
        }
        if (showLauncherSettings && !showCompositionEditor) {
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
                onChooseBackgroundImage = { openFileBrowser(FileBrowserMode.IMAGES) },
                onImportTheme = { openFileBrowser(FileBrowserMode.THEMES) },
                onExportTheme = { exportThemePicker.launch("${(appearance ?: LauncherAppearance(state.themeId)).themeId.name.lowercase()}-theme.zip") },
                onOpenOnlineThemes = { showRemoteThemes = true },
                onCreateCustomTheme = {
                    editingThemeId = null
                    editingThemeAppearance = LauncherAppearance(state.themeId)
                    editingThemeVersion = null
                    editorAssetBytes = emptyMap()
                    editorBaseBackgroundAsset = null
                    editorAssetUris = emptyMap()
                    compositionName = context.getString(R.string.editor_new_theme)
                    editedComposition = ThemeComposition(emptyList())
                    showCompositionEditor = true
                },
                installedThemes = installedThemes,
                apps = successState?.apps ?: persistentListOf(),
                previewData = compositionPreviewData,
                onOpenDeviceTools = { showLauncherSettings = false; showDeviceTools = true },
                onOpenWeatherSettings = { showLauncherSettings = false; showWeatherSettings = true },
                weatherContent = {
                    com.tv.launcher.custom.weather.WeatherSettingsContent(
                        state = weatherState, onIntent = onIntent, onDismiss = {}, embedded = true
                    )
                },
                deviceToolsContent = {
                    DeviceToolsDialog(apps = successState?.apps?.toList().orEmpty(), onDismiss = {}, embedded = true)
                },
                onEditInstalledTheme = { themeId ->
                    coroutineScope.launch {
                        val installed = withContext(Dispatchers.IO) { installedThemeStore.load(themeId) }
                        val archiveBytes = withContext(Dispatchers.IO) { installedThemeStore.archiveBytes(themeId) }
                         if (installed != null && archiveBytes != null && installed.composition != null) {
                             editingThemeId = themeId
                             editorBaseBackgroundAsset = null
                            editingThemeAppearance = installed.appearance
                            editingThemeVersion = installed.version
                            compositionName = installed.name
                              editedComposition = requireNotNull(installed.composition)
                             editorAssetBytes = ThemeArchiveReader.read(archiveBytes).assets
                             editorAssetUris = installed.assetUris
                            showCompositionEditor = true
                        } else {
                             Toast.makeText(context, if (installed != null && installed.composition == null)
                                 R.string.editor_native_theme_base else R.string.theme_import_failed, Toast.LENGTH_LONG).show()
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
                onImportAssets = { openFileBrowser(FileBrowserMode.ASSETS) },
                onCompositionChange = { if (!isSavingComposition) editedComposition = it },
                isEditing = editingThemeId != null,
                isSaving = isSavingComposition,
                useDialog = false,
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
                                 } else editorBaseBackgroundAsset ?: themeAppearance.backgroundImageUri?.let { imageUri ->
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
        fileBrowserMode?.let { mode ->
            FileBrowserDialog(
                mode = mode,
                roots = remember(mode, storageRefresh) { browserRoots(context) },
                storageAccess = remember(fileBrowserMode, storageRefresh) { hasStorageAccess(context) },
                onRequestStorage = { requestStorageAccess() },
                onDismiss = { fileBrowserMode = null },
                onOpenBundledThemes = if (mode == FileBrowserMode.THEMES) {
                    { fileBrowserMode = null; showBundledThemePicker = true }
                } else null,
                onOpenSystemPicker = if (hasUsableDocumentsPicker(
                        context,
                        if (mode == FileBrowserMode.THEMES) "application/zip" else "image/*"
                    )
                ) {
                    {
                        fileBrowserMode = null
                        runCatching {
                            when (mode) {
                                FileBrowserMode.THEMES -> importThemePicker.launch(
                                    arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")
                                )
                                FileBrowserMode.IMAGES -> backgroundPicker.launch(
                                    arrayOf("image/png", "image/jpeg", "image/webp")
                                )
                                FileBrowserMode.ASSETS -> editorAssetsPicker.launch(
                                    arrayOf("image/png", "image/jpeg", "image/webp")
                                )
                            }
                        }.onFailure {
                            if (mode == FileBrowserMode.THEMES) showBundledThemePicker = true
                        }
                    }
                } else null,
                onFileSelected = { file ->
                    fileBrowserMode = null
                    when (mode) {
                        FileBrowserMode.THEMES -> coroutineScope.launch {
                            val bytes = withContext(Dispatchers.IO) {
                                runCatching { file.readBytes() }.getOrNull()
                            }
                            if (bytes == null || bytes.size > 20 * 1024 * 1024) {
                                Toast.makeText(context, R.string.theme_import_failed, Toast.LENGTH_LONG).show()
                            } else {
                                importThemeArchive(bytes)
                            }
                        }
                        FileBrowserMode.IMAGES -> coroutineScope.launch {
                            val uri = withContext(Dispatchers.IO) { persistBackgroundImage(context, file) }
                            if (uri == null) {
                                Toast.makeText(context, R.string.file_browser_image_failed, Toast.LENGTH_LONG).show()
                            } else {
                                onIntent(HomeIntent.SelectBackgroundImage(uri.toString()))
                            }
                        }
                        FileBrowserMode.ASSETS -> importEditorAssets(listOf(Uri.fromFile(file)))
                    }
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
                                        val (archive, manifest) = withContext(Dispatchers.Default) {
                                            val parsed = ThemeArchiveReader.read(bytes)
                                            parsed to ThemeManifestValidator.parse(parsed.manifest, parsed.assets.keys)
                                        }
                                        val previewBackground = withContext(Dispatchers.IO) {
                                            themePreviewBackgroundUri(context, archive, manifest)
                                        }
                                        PendingThemeImport(manifest, bytes, previewBackgroundUri = previewBackground)
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
                        androidx.compose.foundation.layout.Box(
                            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp))
                        ) {
                            com.tv.launcher.custom.feature.settings.SettingsThemePreview(
                                appearance = LauncherAppearance(
                                    themeId = pending.manifest.layout,
                                    accentColorArgb = pending.manifest.accentArgb,
                                    backgroundColorArgb = backgroundColor,
                                    backgroundImageUri = pending.previewBackgroundUri
                                ),
                                composition = pending.manifest.composition,
                                name = pending.manifest.name,
                                apps = successState?.apps ?: persistentListOf(),
                                data = compositionPreviewData,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
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
    onLaunch: (String) -> Unit,
    filter: AppsFilter = AppsFilter.ALL,
    favorites: Set<String> = emptySet(),
    recents: List<String> = emptyList(),
    onFilterChange: (AppsFilter) -> Unit = {}
) {
    val context = LocalContext.current
    var categories by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    LaunchedEffect(apps) {
        categories = withContext(Dispatchers.IO) {
            apps.associate { app ->
                app.packageName to runCatching {
                    context.packageManager.getApplicationInfo(app.packageName, 0).category
                }.getOrDefault(android.content.pm.ApplicationInfo.CATEGORY_UNDEFINED)
            }
        }
    }
    val filteredApps = remember(apps, filter, categories, favorites, recents) {
        AppsFiltering.apply(apps, filter, categories, favorites, recents)
    }
    var focusedPackage by rememberSaveable { mutableStateOf<String?>(null) }
    val focusTarget = filteredApps.firstOrNull { it.packageName == focusedPackage } ?: filteredApps.firstOrNull()
    val firstAppFocus = remember { FocusRequester() }
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val style = com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle.current
    val hasCustomBackground = LocalBackgroundImageVisible.current || LocalBackgroundImage.current != null
    val cardShape = androidx.compose.foundation.shape.RoundedCornerShape(style.tileRadius)
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(if (filter == AppsFilter.ALL) R.string.launcher_all_apps else filter.labelRes()),
                    fontSize = 28.sp, color = style.palette.onBackground
                )
                Text(androidx.compose.ui.res.pluralStringResource(R.plurals.launcher_app_count, filteredApps.size, filteredApps.size), fontSize = 16.sp, color = style.palette.onMuted)
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppsFilter.entries.forEach { option ->
                    SettingsButton(
                        label = stringResource(option.labelRes()),
                        selected = option == filter,
                        onClick = { onFilterChange(option) }
                    )
                }
            }
            if (filteredApps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            stringResource(if (apps.isEmpty()) R.string.launcher_no_apps else R.string.apps_filter_empty),
                            color = style.palette.onMuted
                        )
                        if (filter != AppsFilter.ALL) {
                            Button(onClick = { onFilterChange(AppsFilter.ALL) }) {
                                Text(stringResource(R.string.apps_filter_show_all))
                            }
                        }
                    }
                }
            } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(144.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    androidx.tv.material3.Surface(
                        onClick = { onLaunch(app.packageName) },
                        shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(cardShape),
                        colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
                            containerColor = if (style.glassSurfaces || hasCustomBackground) androidx.compose.ui.graphics.Color.Transparent else style.palette.surface,
                            contentColor = style.palette.onBackground,
                            focusedContainerColor = if (style.glassSurfaces || hasCustomBackground) androidx.compose.ui.graphics.Color.Transparent else style.palette.surface,
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
                        Column(Modifier.fillMaxWidth().height(152.dp).sectionSurfaceBackground(cardShape).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
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

private fun AppsFilter.labelRes(): Int = when (this) {
    AppsFilter.ALL -> R.string.apps_filter_all
    AppsFilter.FOR_YOU -> R.string.apps_filter_for_you
    AppsFilter.VIDEO -> R.string.apps_filter_video
    AppsFilter.AUDIO -> R.string.apps_filter_audio
    AppsFilter.GAMES -> R.string.apps_filter_games
    AppsFilter.FAVORITES -> R.string.apps_filter_favorites
    AppsFilter.RECENTS -> R.string.apps_filter_recents
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
    "orbita.zip" to "Órbita",
    "bruma.zip" to "Bruma",
    "horizonte.zip" to "Horizonte",
    "estudio.zip" to "Estudio",
    "vidaa.zip" to "VIDAA",
    "webos-editable.zip" to "webOS · Editable",
    "android-tv-editable.zip" to "Android TV · Editable",
    "google-tv-editable.zip" to "Google TV · Editable",
    "fire-tv-editable.zip" to "Fire TV · Editable",
    "roku-editable.zip" to "Roku · Editable",
    "tizen-editable.zip" to "Tizen · Editable",
    "apple-tv-editable.zip" to "Apple TV · Editable",
    "free-my-tv-editable.zip" to "Free My TV · Editable",
    "launch-dock-demo.zip" to "Launch Dock",
    "side-stream-demo.zip" to "Side Stream",
    "nova-minimal-premium.zip" to "NOVA · Minimal Premium",
    "macos-liquid-glass.zip" to "macOS Liquid Glass"
)

@Composable
private fun LauncherLoadingSplash() {
    val palette = LocalLauncherStyle.current.palette

    val transition = rememberInfiniteTransition(
        label = "launcher-splash"
    )

    val progressPosition by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress-position"
    )

    val glowAlpha by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background-glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.Center
    ) {
        // Halo ambiental detrás del contenido.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.65f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.accent.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 48.dp, vertical = 32.dp)
                .widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                palette.accent.copy(alpha = 0.20f),
                                palette.accent.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = palette.accent.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Símbolo de TV sin dependencia de Material Icons.
                Canvas(modifier = Modifier.size(52.dp)) {
                    val stroke = 2.5.dp.toPx()
                    val screenHeight = size.height * 0.65f
                    val screenTop = size.height * 0.10f
                    val screenBottom = screenTop + screenHeight

                    drawRoundRect(
                        color = palette.accent,
                        topLeft = Offset(0f, screenTop),
                        size = Size(size.width, screenHeight),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                        style = Stroke(width = stroke)
                    )

                    drawLine(
                        color = palette.accent,
                        start = Offset(size.width / 2f, screenBottom),
                        end = Offset(size.width / 2f, size.height * 0.90f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = palette.accent,
                        start = Offset(size.width * 0.30f, size.height * 0.90f),
                        end = Offset(size.width * 0.70f, size.height * 0.90f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.app_name),
                color = palette.onBackground,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.launcher_splash_message),
                color = palette.onMuted,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Canvas(
                modifier = Modifier
                    .width(260.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .semantics {
                        progressBarRangeInfo =
                            ProgressBarRangeInfo.Indeterminate
                    }
            ) {
                drawRect(
                    color = palette.onBackground.copy(alpha = 0.10f)
                )

                val segmentWidth = size.width * 0.35f
                val startX = progressPosition * size.width

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            palette.accent.copy(alpha = 0f),
                            palette.accent,
                            palette.accent.copy(alpha = 0f)
                        ),
                        startX = startX,
                        endX = startX + segmentWidth
                    ),
                    topLeft = Offset(startX, 0f),
                    size = Size(segmentWidth, size.height),
                    cornerRadius = CornerRadius(size.height / 2f)
                )
            }
        }
    }
}

@Preview(name = "Launcher · Splash", widthDp = 960, heightDp = 540)
@Composable
private fun LauncherLoadingSplashPreview() = LauncherTheme(ThemeId.GOOGLE_TV) {
    LauncherLoadingSplash()
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
