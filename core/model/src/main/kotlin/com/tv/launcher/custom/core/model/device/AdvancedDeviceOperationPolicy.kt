package com.tv.launcher.custom.core.model.device

private val packageNamePattern = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+")

private val protectedPackages = setOf(
    "android",
    "com.android.settings",
    "com.android.systemui",
    "com.android.shell",
    "com.android.packageinstaller",
    "com.google.android.packageinstaller",
    "com.google.android.permissioncontroller",
    "com.android.providers.settings",
    "com.android.providers.media",
    "com.android.providers.downloads",
    "com.android.documentsui",
    "com.android.inputdevices",
    "com.google.android.gms",
    "com.google.android.gsf",
    "com.google.android.tvlauncher",
    "com.google.android.apps.tv.launcherx"
)

enum class AnimationScaleSetting(val key: String) {
    WINDOW("window_animation_scale"),
    TRANSITION("transition_animation_scale"),
    ANIMATOR("animator_duration_scale")
}

object AdvancedDeviceOperationPolicy {
    val animationSettings: Set<String> = AnimationScaleSetting.entries.mapTo(linkedSetOf()) { it.key }

    fun canRemovePackageForCurrentUser(packageName: String, launcherPackage: String): Boolean =
        packageNamePattern.matches(packageName) &&
            packageName != launcherPackage &&
            packageName !in protectedPackages

    fun setAnimationScaleArguments(setting: AnimationScaleSetting, value: String): List<String>? {
        if (setting.key !in animationSettings || !value.toFloatOrNull().isFiniteAnimationScale()) return null
        return listOf("settings", "put", "global", setting.key, value)
    }

    fun readAnimationScaleArguments(setting: AnimationScaleSetting): List<String> =
        listOf("settings", "get", "global", setting.key)

    fun uninstallForCurrentUserArguments(packageName: String, launcherPackage: String): List<String>? {
        if (!canRemovePackageForCurrentUser(packageName, launcherPackage)) return null
        return listOf("pm", "uninstall", "--user", "current", packageName)
    }

    fun restorePreinstalledAppArguments(packageName: String, launcherPackage: String): List<String>? {
        if (!canRemovePackageForCurrentUser(packageName, launcherPackage)) return null
        return listOf("cmd", "package", "install-existing", "--user", "current", packageName)
    }

    private fun Float?.isFiniteAnimationScale(): Boolean = this?.let { it.isFinite() && it in 0f..10f } == true
}
