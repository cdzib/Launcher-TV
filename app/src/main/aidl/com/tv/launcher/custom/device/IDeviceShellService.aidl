package com.tv.launcher.custom.device;

interface IDeviceShellService {
    void destroy() = 16777114;
    String captureAnimationScales() = 1;
    String applySnappyAnimationPreset(String capturedScales) = 2;
    String restoreAnimationScales(String capturedScales) = 3;
    String uninstallPreinstalledAppForCurrentUser(String packageName) = 4;
    String restorePreinstalledAppForCurrentUser(String packageName) = 5;
}
