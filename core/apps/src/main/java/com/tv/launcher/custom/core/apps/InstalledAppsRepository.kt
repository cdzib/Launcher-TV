package com.tv.launcher.custom.core.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import androidx.core.content.ContextCompat
import com.tv.launcher.custom.core.model.DomainError
import com.tv.launcher.custom.core.model.DomainResult
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.apps.AppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AppsRepository {

    private val pm: PackageManager = context.packageManager

    override fun observeApps(): Flow<List<AppEntry>> = packageChanges()
        .map { queryApps() }
        .flowOn(Dispatchers.IO)

    override suspend fun launch(packageName: String): DomainResult<Unit> {
        val intent = pm.getLeanbackLaunchIntentForPackage(packageName)
            ?: pm.getLaunchIntentForPackage(packageName)
            ?: return DomainResult.Failure(DomainError.AppNotFound)
        return runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .fold(
                onSuccess = { DomainResult.Success(Unit) },
                onFailure = { DomainResult.Failure(DomainError.Unknown(it.message)) }
            )
    }

    private fun packageChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        trySend(Unit)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun queryApps(): List<AppEntry> {
        val leanback = query(Intent.CATEGORY_LEANBACK_LAUNCHER)
        return leanback.ifEmpty { query(Intent.CATEGORY_LAUNCHER) }
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val applicationInfo = resolveInfo.activityInfo.applicationInfo
                AppEntry(
                    packageName = applicationInfo.packageName,
                    label = resolveInfo.loadLabel(pm).toString(),
                    isSystemApp = applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun query(category: String) =
        pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)
}
