package com.tv.launcher.custom.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.device.AdvancedDeviceOperationPolicy
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

private const val SHIZUKU_REQUEST_CODE = 7314
private const val ANIMATION_SNAPSHOT_KEY = "animation_scales_snapshot"
private const val REMOVED_SYSTEM_PACKAGES_KEY = "removed_system_packages"
private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"

private data class PendingDeviceOperation(
    val message: String,
    val action: () -> Unit
)

@Composable
fun DeviceToolsDialog(
    apps: List<AppEntry>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gateway = remember { ShizukuDeviceGateway() }
    val protectedDevicePackages = remember(context) { getProtectedDevicePackages(context) }
    val preferences = remember(context) {
        context.getSharedPreferences("advanced_device_tools", Context.MODE_PRIVATE)
    }
    var shizukuAvailable by remember { mutableStateOf(runCatching { Shizuku.pingBinder() }.getOrDefault(false)) }
    var shizukuAuthorized by remember {
        mutableStateOf(runCatching {
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false))
    }
    var hasSavedAnimationScales by remember { mutableStateOf(preferences.contains(ANIMATION_SNAPSHOT_KEY)) }
    var removedSystemPackages by remember {
        mutableStateOf(preferences.getStringSet(REMOVED_SYSTEM_PACKAGES_KEY, emptySet()).orEmpty())
    }
    var selectedApp by remember { mutableStateOf<AppEntry?>(null) }
    var pendingDeviceOperation by remember { mutableStateOf<PendingDeviceOperation?>(null) }
    var isWorking by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refreshShizukuState() {
        shizukuAvailable = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        shizukuAuthorized = shizukuAvailable && runCatching {
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
    }

    DisposableEffect(gateway) {
        val binderReceivedListener = Shizuku.OnBinderReceivedListener { refreshShizukuState() }
        val binderDeadListener = Shizuku.OnBinderDeadListener { refreshShizukuState() }
        val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_REQUEST_CODE) {
                shizukuAuthorized = grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED
                refreshShizukuState()
            }
        }
        runCatching { Shizuku.addBinderReceivedListenerSticky(binderReceivedListener) }
        runCatching { Shizuku.addBinderDeadListener(binderDeadListener) }
        runCatching { Shizuku.addRequestPermissionResultListener(permissionResultListener) }
        onDispose {
            runCatching { Shizuku.removeBinderReceivedListener(binderReceivedListener) }
            runCatching { Shizuku.removeBinderDeadListener(binderDeadListener) }
            runCatching { Shizuku.removeRequestPermissionResultListener(permissionResultListener) }
            gateway.close()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        DeviceToolsPanel(
            apps = apps,
            shizukuAvailable = shizukuAvailable,
            shizukuAuthorized = shizukuAuthorized,
            isWorking = isWorking,
            statusMessage = statusMessage,
            hasSavedAnimationScales = hasSavedAnimationScales,
            removedSystemPackages = removedSystemPackages,
            onRequestShizuku = {
                when {
                    !shizukuAvailable -> openShizuku(context)
                    shizukuAuthorized -> Unit
                    else -> runCatching { Shizuku.requestPermission(SHIZUKU_REQUEST_CODE) }
                        .onFailure { statusMessage = it.message ?: context.getString(R.string.device_tools_operation_failed) }
                }
            },
            onApplyAnimations = {
                pendingDeviceOperation = PendingDeviceOperation(
                    context.getString(R.string.device_tools_apply_animation_warning)
                ) {
                    if (!shizukuAvailable || !shizukuAuthorized) {
                        statusMessage = context.getString(R.string.device_tools_shizuku_unavailable)
                    } else {
                        scope.launch {
                            isWorking = true
                            statusMessage = null
                            runCatching {
                                if (!preferences.contains(ANIMATION_SNAPSHOT_KEY)) {
                                    val snapshot = gateway.captureAnimationScales()
                                    check(isValidAnimationSnapshot(snapshot)) { "Device returned invalid animation settings" }
                                    check(preferences.edit().putString(ANIMATION_SNAPSHOT_KEY, snapshot).commit()) {
                                        "Could not save original animation settings"
                                    }
                                }
                                val snapshot = preferences.getString(ANIMATION_SNAPSHOT_KEY, null)
                                    ?: error("Original animation settings were not saved")
                                gateway.applySnappyAnimationPreset(snapshot)
                            }.onSuccess {
                                hasSavedAnimationScales = true
                                statusMessage = it.ifBlank { context.getString(R.string.device_tools_apply_animation) }
                            }.onFailure {
                                statusMessage = getDeviceOperationFailureMessage(context, it)
                            }
                            isWorking = false
                        }
                    }
                }
            },
            onRestoreAnimations = {
                val snapshot = preferences.getString(ANIMATION_SNAPSHOT_KEY, null)
                if (snapshot == null) {
                    hasSavedAnimationScales = false
                } else {
                    pendingDeviceOperation = PendingDeviceOperation(
                        context.getString(R.string.device_tools_restore_animation_warning)
                    ) {
                        if (!shizukuAvailable || !shizukuAuthorized) {
                            statusMessage = context.getString(R.string.device_tools_shizuku_unavailable)
                        } else {
                            scope.launch {
                                isWorking = true
                                statusMessage = null
                                runCatching { gateway.restoreAnimationScales(snapshot) }
                                    .onSuccess {
                                        preferences.edit().remove(ANIMATION_SNAPSHOT_KEY).commit()
                                        hasSavedAnimationScales = false
                                        statusMessage = context.getString(R.string.device_tools_restore_animation)
                                    }
                                    .onFailure { statusMessage = it.message ?: context.getString(R.string.device_tools_operation_failed) }
                                isWorking = false
                            }
                        }
                    }
                }
            },
            onSelectAppForRemoval = { selectedApp = it },
            onRestoreSystemApp = { packageName ->
                pendingDeviceOperation = PendingDeviceOperation(
                    context.getString(R.string.device_tools_restore_app_warning, packageName)
                ) {
                    if (!shizukuAvailable || !shizukuAuthorized) {
                        statusMessage = context.getString(R.string.device_tools_shizuku_unavailable)
                    } else {
                        scope.launch {
                            isWorking = true
                            runCatching { gateway.restorePreinstalledApp(packageName) }
                                .onSuccess {
                                    removedSystemPackages = removedSystemPackages - packageName
                                    preferences.edit().putStringSet(REMOVED_SYSTEM_PACKAGES_KEY, removedSystemPackages).commit()
                                    statusMessage = it.ifBlank { context.getString(R.string.device_tools_app_restore) }
                                }
                                .onFailure { statusMessage = it.message ?: context.getString(R.string.device_tools_operation_failed) }
                            isWorking = false
                        }
                    }
                }
            },
            onCopyAdbInstructions = { packageName ->
                copyAdbText(context, buildAdbInstructions(packageName, protectedDevicePackages, context.packageName))
            },
            onCopyAdbRestoreInstruction = { packageName ->
                copyAdbText(context, buildAdbRestoreInstruction(packageName, protectedDevicePackages, context.packageName))
            },
            onDismiss = onDismiss
        )
    }

    pendingDeviceOperation?.let { pending ->
        DeviceOperationConfirmationDialog(
            message = pending.message,
            onCancel = { pendingDeviceOperation = null },
            onConfirm = {
                pendingDeviceOperation = null
                pending.action()
            }
        )
    }

    selectedApp?.let { app ->
        DeviceToolsConfirmationDialog(
            app = app,
            onCancel = { selectedApp = null },
            onConfirm = {
                selectedApp = null
                if (app.isSystemApp) {
                    if (!AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser(
                            app.packageName, context.packageName, protectedDevicePackages
                        )
                    ) {
                        statusMessage = context.getString(R.string.device_tools_protected_package)
                    } else if (!shizukuAvailable || !shizukuAuthorized) {
                        statusMessage = context.getString(R.string.device_tools_shizuku_unavailable)
                    } else {
                        scope.launch {
                            isWorking = true
                            runCatching { gateway.uninstallPreinstalledApp(app.packageName) }
                                .onSuccess {
                                    removedSystemPackages = removedSystemPackages + app.packageName
                                    preferences.edit().putStringSet(REMOVED_SYSTEM_PACKAGES_KEY, removedSystemPackages).commit()
                                    statusMessage = it.ifBlank { context.getString(R.string.device_tools_app_remove_system) }
                                }
                                .onFailure { statusMessage = it.message ?: context.getString(R.string.device_tools_operation_failed) }
                            isWorking = false
                        }
                    }
                } else {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}")))
                    }.onFailure { statusMessage = it.message ?: context.getString(R.string.device_tools_operation_failed) }
                }
            },
            onCopyAdb = if (app.isSystemApp) {
                {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            context.getString(R.string.device_tools_title),
                            buildAdbInstructions(app.packageName, protectedDevicePackages, context.packageName)
                        )
                    )
                    Toast.makeText(context, R.string.device_tools_adb_copied, Toast.LENGTH_SHORT).show()
                }
            } else null
        )
    }
}

@Composable
private fun DeviceToolsPanel(
    apps: List<AppEntry>,
    shizukuAvailable: Boolean,
    shizukuAuthorized: Boolean,
    isWorking: Boolean,
    statusMessage: String?,
    hasSavedAnimationScales: Boolean,
    removedSystemPackages: Set<String>,
    onRequestShizuku: () -> Unit,
    onApplyAnimations: () -> Unit,
    onRestoreAnimations: () -> Unit,
    onSelectAppForRemoval: (AppEntry) -> Unit,
    onRestoreSystemApp: (String) -> Unit,
    onCopyAdbInstructions: (String?) -> Unit,
    onCopyAdbRestoreInstruction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(Modifier.fillMaxSize(), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
        Column(
            Modifier.fillMaxSize().background(Color(0xFF101318)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.device_tools_title), fontSize = 26.sp)
                    Text(stringResource(R.string.device_tools_description), fontSize = 14.sp)
                }
                Button(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (shizukuAvailable && shizukuAuthorized) stringResource(R.string.device_tools_shizuku_ready)
                    else stringResource(R.string.device_tools_shizuku_unavailable)
                )
                Button(onClick = onRequestShizuku, enabled = !isWorking) {
                    Text(stringResource(if (shizukuAvailable) R.string.device_tools_grant_shizuku else R.string.device_tools_open_shizuku))
                }

                Text(stringResource(R.string.device_tools_animation_heading), fontSize = 20.sp)
                Text(stringResource(R.string.device_tools_animation_description))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onApplyAnimations, enabled = !isWorking && shizukuAvailable && shizukuAuthorized) {
                        Text(stringResource(if (isWorking) R.string.device_tools_working else R.string.device_tools_apply_animation))
                    }
                    Button(onClick = onRestoreAnimations, enabled = !isWorking && hasSavedAnimationScales && shizukuAvailable && shizukuAuthorized) {
                        Text(stringResource(R.string.device_tools_restore_animation))
                    }
                }
                Button(onClick = { onCopyAdbInstructions(null) }) {
                    Text(stringResource(R.string.device_tools_copy_adb))
                }
                Text(stringResource(R.string.device_tools_adb_heading), fontSize = 18.sp)
                Text(stringResource(R.string.device_tools_adb_description))

                Text(stringResource(R.string.device_tools_app_heading), fontSize = 20.sp)
                apps.forEach { app ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(app.label, maxLines = 1)
                            Text(app.packageName, fontSize = 12.sp)
                        }
                        Button(onClick = { onSelectAppForRemoval(app) }, enabled = !isWorking) {
                            Text(stringResource(if (app.isSystemApp) R.string.device_tools_app_remove_system else R.string.device_tools_app_remove))
                        }
                    }
                }
                removedSystemPackages.forEach { packageName ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(packageName, Modifier.weight(1f), fontSize = 14.sp)
                        Button(onClick = { onRestoreSystemApp(packageName) }, enabled = !isWorking && shizukuAvailable && shizukuAuthorized) {
                            Text(stringResource(R.string.device_tools_app_restore))
                        }
                        Button(onClick = { onCopyAdbRestoreInstruction(packageName) }, enabled = !isWorking) {
                            Text(stringResource(R.string.device_tools_copy_adb))
                        }
                    }
                }
                statusMessage?.let { Text(it) }
            }
        }
    }
}

@Composable
private fun DeviceOperationConfirmationDialog(
    message: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(Modifier.width(560.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.device_tools_confirm_operation), fontSize = 22.sp)
                Text(message)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onConfirm) { Text(stringResource(R.string.device_tools_confirm_remove)) }
                    Button(onClick = onCancel) { Text(stringResource(R.string.device_tools_cancel)) }
                }
            }
        }
    }
}

@Composable
private fun DeviceToolsConfirmationDialog(
    app: AppEntry,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onCopyAdb: (() -> Unit)?
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(Modifier.width(520.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(app.label, fontSize = 22.sp)
                Text(stringResource(if (app.isSystemApp) R.string.device_tools_system_app_warning else R.string.device_tools_user_app_warning))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onConfirm) { Text(stringResource(R.string.device_tools_confirm_remove)) }
                    onCopyAdb?.let { Button(onClick = it) { Text(stringResource(R.string.device_tools_copy_adb)) } }
                    Button(onClick = onCancel) { Text(stringResource(R.string.device_tools_cancel)) }
                }
            }
        }
    }
}

private fun openShizuku(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/"))
    runCatching { context.startActivity(intent) }
}

private fun isValidAnimationSnapshot(snapshot: String): Boolean {
    val values = snapshot.lineSequence().mapNotNull { line ->
        val separator = line.indexOf('=')
        if (separator <= 0) null else line.substring(0, separator) to line.substring(separator + 1)
    }.toMap()
    return values.keys == AdvancedDeviceOperationPolicy.animationSettings && values.all { (key, value) ->
        val setting = com.tv.launcher.custom.core.model.device.AnimationScaleSetting.entries.firstOrNull { it.key == key }
        setting != null && AdvancedDeviceOperationPolicy.setAnimationScaleArguments(setting, value) != null
    }
}

private fun buildAdbInstructions(
    packageName: String?,
    protectedDevicePackages: Set<String>,
    launcherPackage: String
): String = buildString {
    appendLine("# Read and save current values before changing them")
    listOf("window_animation_scale", "transition_animation_scale", "animator_duration_scale").forEach { key ->
        appendLine("adb shell settings get global $key")
    }
    appendLine("adb shell settings put global window_animation_scale 0.5")
    appendLine("adb shell settings put global transition_animation_scale 0.5")
    appendLine("adb shell settings put global animator_duration_scale 0.5")
    appendLine("# Restore each key separately with its value recorded above")
    if (packageName != null && AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser(
            packageName, launcherPackage, protectedDevicePackages
        )
    ) {
        appendLine("adb shell pm uninstall --user current $packageName")
        appendLine("# Restore is a separate action; do not paste it together with this command")
    }
}

private fun buildAdbRestoreInstruction(
    packageName: String,
    protectedDevicePackages: Set<String>,
    launcherPackage: String
): String =
    if (AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser(packageName, launcherPackage, protectedDevicePackages)) {
        "adb shell cmd package install-existing --user current $packageName"
    } else {
        "# Package is protected; no restore command generated"
    }

private fun getProtectedDevicePackages(context: Context): Set<String> = runCatching {
    val packageManager = context.packageManager
    val flags = android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
    val homePackage = packageManager.resolveActivity(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), flags
    )?.activityInfo?.packageName ?: return@runCatching setOf("*")
    val settingsPackage = packageManager.resolveActivity(Intent(android.provider.Settings.ACTION_SETTINGS), flags)
        ?.activityInfo?.packageName
    setOfNotNull(homePackage, settingsPackage)
}.getOrDefault(setOf("*"))

private fun getDeviceOperationFailureMessage(context: Context, exception: Throwable): String {
    val message = exception.message.orEmpty()
    val partialRollback = message.split('|', limit = 3)
    return if (partialRollback.size == 3 && partialRollback[0] == "PARTIAL_ROLLBACK") {
        context.getString(R.string.device_tools_partial_rollback, partialRollback[1], partialRollback[2])
    } else {
        message.ifBlank { context.getString(R.string.device_tools_operation_failed) }
    }
}

private fun copyAdbText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.device_tools_title), text))
    Toast.makeText(context, R.string.device_tools_adb_copied, Toast.LENGTH_SHORT).show()
}

@Preview(widthDp = 960, heightDp = 700)
@Composable
private fun DeviceToolsPanelPreview() = LauncherTheme(com.tv.launcher.custom.core.model.theme.ThemeId.WEBOS) {
    DeviceToolsPanel(
        apps = listOf(AppEntry("com.example.streamer", "Streaming app")),
        shizukuAvailable = false,
        shizukuAuthorized = false,
        isWorking = false,
        statusMessage = null,
        hasSavedAnimationScales = false,
        removedSystemPackages = emptySet(),
        onRequestShizuku = {},
        onApplyAnimations = {},
        onRestoreAnimations = {},
        onSelectAppForRemoval = {},
        onRestoreSystemApp = {},
        onCopyAdbInstructions = {},
        onCopyAdbRestoreInstruction = {},
        onDismiss = {}
    )
}
