# Shizuku instancia el servicio de usuario por nombre de clase en otro proceso,
# por lo que R8 no puede detectar la referencia y debe conservarla.
-keep class com.tv.launcher.custom.device.DeviceShellUserService { *; }
-keep interface com.tv.launcher.custom.device.IDeviceShellService { *; }
-keep class com.tv.launcher.custom.device.IDeviceShellService$Stub { *; }
-keep class com.tv.launcher.custom.device.IDeviceShellService$Stub$Proxy { *; }

# Shizuku usa este Parcelable desde su propio proceso.
-keepnames class moe.shizuku.api.BinderContainer
-keepclassmembers class moe.shizuku.api.BinderContainer {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Los temas declarativos se validan con kotlinx.serialization JSON DOM;
# no hay modelos @Serializable, pero se conservan los nombres de los
# componentes de renderizado por claridad en los reportes de fallos.
-keepnames class com.tv.launcher.custom.core.model.theme.** { *; }
