package com.tv.launcher.custom.core.model.device

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedDeviceOperationTest {
    @Test
    fun `allows only syntactically valid non-critical app packages`() {
        assertTrue(AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser("org.example.streamer", "com.tv.launcher.custom"))
        assertFalse(AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser("com.tv.launcher.custom", "com.tv.launcher.custom"))
        assertFalse(AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser("com.android.settings", "com.tv.launcher.custom"))
        assertFalse(AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser("com.android.systemui", "com.tv.launcher.custom"))
        assertFalse(AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser("org.example;reboot", "com.tv.launcher.custom"))
        assertFalse(AdvancedDeviceOperationPolicy.canRemovePackageForCurrentUser("", "com.tv.launcher.custom"))
    }

    @Test
    fun `builds fixed commands and rejects unsafe animation values`() {
        assertEquals(
            listOf("pm", "uninstall", "--user", "current", "org.example.streamer"),
            AdvancedDeviceOperationPolicy.uninstallForCurrentUserArguments("org.example.streamer", "com.tv.launcher.custom")
        )
        assertEquals(
            listOf("settings", "put", "global", "window_animation_scale", "0.5"),
            AdvancedDeviceOperationPolicy.setAnimationScaleArguments(AnimationScaleSetting.WINDOW, "0.5")
        )
        assertNull(AdvancedDeviceOperationPolicy.setAnimationScaleArguments(AnimationScaleSetting.WINDOW, "0.5; reboot"))
        assertNull(AdvancedDeviceOperationPolicy.setAnimationScaleArguments(AnimationScaleSetting.WINDOW, "-1"))
    }
}
