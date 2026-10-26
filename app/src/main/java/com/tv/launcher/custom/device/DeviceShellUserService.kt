package com.tv.launcher.custom.device

import android.os.RemoteException
import com.tv.launcher.custom.core.model.device.AdvancedDeviceOperationPolicy
import com.tv.launcher.custom.core.model.device.AnimationScaleSetting
import java.io.ByteArrayOutputStream
import java.io.InputStream

/** Shizuku user service. Only fixed, app-owned operations are exposed over AIDL. */
class DeviceShellUserService : IDeviceShellService.Stub() {
    override fun destroy() {
        System.exit(0)
    }

    override fun captureAnimationScales(): String {
        return AnimationScaleSetting.entries.joinToString("\n") { setting ->
            val result = execute(AdvancedDeviceOperationPolicy.readAnimationScaleArguments(setting))
            if (result.exitCode != 0) throw RemoteException("Could not read ${setting.key}")
            "${setting.key}=${result.output.trim()}"
        }
    }

    override fun applySnappyAnimationPreset(capturedScales: String): String {
        val originalValues = parseCapturedScales(capturedScales)
        try {
            val results = AnimationScaleSetting.entries.map { setting ->
                val arguments = AdvancedDeviceOperationPolicy.setAnimationScaleArguments(setting, SNAPPY_SCALE)
                    ?: throw RemoteException("Unsupported animation setting")
                setting.key to execute(arguments)
            }
            val failed = results.filter { (_, result) -> result.exitCode != 0 }
            if (failed.isNotEmpty()) throw RemoteException("Could not update: ${failed.joinToString { it.first }}")
            verifyAnimationScales(AnimationScaleSetting.entries.associate { it.key to SNAPPY_SCALE })
            return results.joinToString("\n") { (key, result) -> "$key=${result.output.trim()}" }
        } catch (exception: Exception) {
            runCatching { restoreAnimationScales(capturedScales) }
            throw exception
        }
    }

    override fun restoreAnimationScales(capturedScales: String): String {
        val values = parseCapturedScales(capturedScales)
        val results = AnimationScaleSetting.entries.map { setting ->
            val value = values.getValue(setting.key)
            val arguments = AdvancedDeviceOperationPolicy.setAnimationScaleArguments(setting, value)
                ?: throw RemoteException("Invalid captured animation scale")
            setting.key to execute(arguments)
        }
        val failed = results.filter { (_, result) -> result.exitCode != 0 }
        if (failed.isNotEmpty()) throw RemoteException("Could not restore: ${failed.joinToString { it.first }}")
        verifyAnimationScales(values)
        return results.joinToString("\n") { (key, result) -> "$key=${result.output.trim()}" }
    }

    override fun uninstallPreinstalledAppForCurrentUser(packageName: String): String {
        val arguments = AdvancedDeviceOperationPolicy.uninstallForCurrentUserArguments(packageName, LAUNCHER_PACKAGE)
            ?: throw RemoteException("Package is not allowed")
        if (!isPreinstalledPackageForCurrentUser(packageName)) {
            throw RemoteException("Only a preinstalled app installed for this user can be removed here")
        }
        val result = execute(arguments)
        if (result.exitCode != 0) throw RemoteException(result.output.ifBlank { "Package removal failed" })
        if (isPackageInstalledForCurrentUser(packageName)) {
            throw RemoteException("Device did not confirm package removal")
        }
        return result.output
    }

    override fun restorePreinstalledAppForCurrentUser(packageName: String): String {
        val arguments = AdvancedDeviceOperationPolicy.restorePreinstalledAppArguments(packageName, LAUNCHER_PACKAGE)
            ?: throw RemoteException("Package is not allowed")
        if (!isPreinstalledPackage(packageName)) {
            throw RemoteException("Package is not part of the device image")
        }
        val result = execute(arguments)
        if (result.exitCode != 0) throw RemoteException(result.output.ifBlank { "Package restore failed" })
        if (!isPackageInstalledForCurrentUser(packageName)) {
            throw RemoteException("Device did not confirm package restoration")
        }
        return result.output
    }

    private fun verifyAnimationScales(expected: Map<String, String>) {
        AnimationScaleSetting.entries.forEach { setting ->
            val result = execute(AdvancedDeviceOperationPolicy.readAnimationScaleArguments(setting))
            val actualValue = result.output.trim().toFloatOrNull()
            val expectedValue = expected[setting.key]?.toFloatOrNull()
            if (result.exitCode != 0 || actualValue == null || expectedValue == null ||
                kotlin.math.abs(actualValue - expectedValue) > SCALE_TOLERANCE
            ) throw RemoteException("Device did not confirm ${setting.key}")
        }
    }

    private fun isPackageInstalledForCurrentUser(packageName: String): Boolean {
        val result = execute(listOf("pm", "list", "packages", "--user", "current", packageName))
        return result.exitCode == 0 && result.output.lineSequence().any { it.trim() == "package:$packageName" }
    }

    private fun isPreinstalledPackageForCurrentUser(packageName: String): Boolean =
        packageQuery(packageName, "pm", "list", "packages", "-s", "--user", "current", packageName)

    private fun isPreinstalledPackage(packageName: String): Boolean =
        packageQuery(packageName, "pm", "list", "packages", "-s", "-u", packageName)

    private fun packageQuery(packageName: String, vararg arguments: String): Boolean {
        val result = execute(arguments.toList())
        return result.exitCode == 0 && result.output.lineSequence().any { line ->
            line.trim() == "package:$packageName"
        }
    }

    private fun parseCapturedScales(raw: String): Map<String, String> {
        val values = raw.lineSequence().mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) null else line.substring(0, separator) to line.substring(separator + 1)
        }.toMap()
        if (values.keys != AdvancedDeviceOperationPolicy.animationSettings ||
            values.any { (key, value) ->
                val setting = AnimationScaleSetting.entries.firstOrNull { it.key == key } ?: return@any true
                AdvancedDeviceOperationPolicy.setAnimationScaleArguments(setting, value) == null
            }
        ) throw RemoteException("Invalid saved animation values")
        return values
    }

    private fun execute(arguments: List<String>): CommandResult {
        val process = try {
            ProcessBuilder(arguments).redirectErrorStream(true).start()
        } catch (exception: Exception) {
            return CommandResult(-1, exception.javaClass.simpleName)
        }
        val output = process.inputStream.use { input -> input.readLimited() }
        val exitCode = try {
            process.waitFor()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            process.destroy()
            return CommandResult(-1, "Operation interrupted")
        }
        return CommandResult(exitCode, output)
    }

    private fun InputStream.readLimited(): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (output.size() < MAX_OUTPUT_BYTES) {
            val count = read(buffer, 0, minOf(buffer.size, MAX_OUTPUT_BYTES - output.size()))
            if (count < 0) break
            output.write(buffer, 0, count)
        }
        return output.toString(Charsets.UTF_8.name())
    }

    private data class CommandResult(val exitCode: Int, val output: String)

    private companion object {
        const val LAUNCHER_PACKAGE = "com.tv.launcher.custom"
        const val SNAPPY_SCALE = "0.5"
        const val SCALE_TOLERANCE = 0.001f
        const val MAX_OUTPUT_BYTES = 4 * 1024
    }
}
