package com.tv.launcher.custom.device

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku

class ShizukuDeviceGateway {
    private val bindMutex = Mutex()
    private var deviceService: IDeviceShellService? = null
    private var pendingConnection: CompletableDeferred<IDeviceShellService>? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val connectedService = binder?.let(IDeviceShellService.Stub::asInterface)
            if (connectedService == null) {
                pendingConnection?.completeExceptionally(IllegalStateException("Shizuku service binder unavailable"))
            } else {
                deviceService = connectedService
                pendingConnection?.complete(connectedService)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            deviceService = null
            pendingConnection?.completeExceptionally(IllegalStateException("Shizuku service stopped"))
            pendingConnection = null
        }
    }

    suspend fun captureAnimationScales(): String = call(IDeviceShellService::captureAnimationScales)

    suspend fun applySnappyAnimationPreset(snapshot: String): String =
        call { service -> service.applySnappyAnimationPreset(snapshot) }

    suspend fun restoreAnimationScales(snapshot: String): String =
        call { service -> service.restoreAnimationScales(snapshot) }

    suspend fun uninstallPreinstalledApp(packageName: String): String =
        call { service -> service.uninstallPreinstalledAppForCurrentUser(packageName) }

    suspend fun restorePreinstalledApp(packageName: String): String =
        call { service -> service.restorePreinstalledAppForCurrentUser(packageName) }

    fun close() {
        if (!isBound) return
        runCatching {
            Shizuku.unbindUserService(userServiceArgs(), serviceConnection, true)
        }
        isBound = false
        deviceService = null
        pendingConnection = null
    }

    private suspend fun call(
        operation: (IDeviceShellService) -> String
    ): String = withContext(Dispatchers.IO) {
        try {
            operation(connect())
        } catch (exception: RemoteException) {
            throw IllegalStateException(exception.message ?: "Shizuku operation failed", exception)
        }
    }

    private suspend fun connect(): IDeviceShellService {
        deviceService?.takeIf { it.asBinder().isBinderAlive }?.let { return it }
        return bindMutex.withLock {
            deviceService?.takeIf { it.asBinder().isBinderAlive }?.let { return@withLock it }
            check(Shizuku.pingBinder()) { "Start Shizuku and authorize this launcher first" }
            check(Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                "Shizuku permission is not granted"
            }
            val connection = CompletableDeferred<IDeviceShellService>()
            pendingConnection = connection
            Shizuku.bindUserService(userServiceArgs(), serviceConnection)
            isBound = true
            withTimeout(BIND_TIMEOUT_MS) { connection.await() }
        }
    }

    private fun userServiceArgs() = Shizuku.UserServiceArgs(
        ComponentName(LAUNCHER_PACKAGE, DeviceShellUserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("device-tools")
        .tag("launcher-device-tools")
        .version(USER_SERVICE_VERSION)

    private companion object {
        const val BIND_TIMEOUT_MS = 15_000L
        const val USER_SERVICE_VERSION = 1
        const val LAUNCHER_PACKAGE = "com.tv.launcher.custom"
    }
}
