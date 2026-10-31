# Instalar WanboOS Launcher por ADB (sin modificar la OTA)

Vía recomendada para el primer despliegue en el Wanbo T6 Max: no toca AVB, no
reescribe particiones y se puede revertir.

## Requisitos

- Depuración USB/ADB activa en el proyector.
- Cable USB de datos y `adb` en el equipo.
- Firmware `userdebug/test-keys` (confirmado en el OTA `20240913`).
- APK de release: `app/build/outputs/apk/release/app-release.apk`.

## 1. Comprobar el equipo

```bash
adb devices
adb shell getprop ro.product.device          # marconi
adb shell getprop ro.build.type              # userdebug
adb shell getprop ro.build.version.sdk       # 28
```

## 2. Root y sistema escribible

```bash
adb root
adb disable-verity      # si responde que ya está desactivada, continuar
adb reboot
# esperar a que vuelva
adb wait-for-device
adb root
adb remount
adb shell mount | grep " /system "
```

Si `remount` falla, repetir `disable-verity` + `reboot` antes de continuar. No
seguir con el sistema en solo lectura.

## 3. Respaldar el launcher actual

```bash
mkdir -p respaldo-wanbo
adb pull /system/priv-app/TVLauncher/TVLauncher.apk respaldo-wanbo/
adb shell pm path com.google.android.tvlauncher | tee respaldo-wanbo/ruta.txt
```

## 4. Instalar el launcher

```bash
APK=app/build/outputs/apk/release/app-release.apk

adb shell mkdir -p /system/priv-app/WanboOSLauncher
adb push "$APK" /system/priv-app/WanboOSLauncher/WanboOSLauncher.apk
adb shell chown root:root /system/priv-app/WanboOSLauncher/WanboOSLauncher.apk
adb shell chmod 644 /system/priv-app/WanboOSLauncher/WanboOSLauncher.apk
adb shell restorecon -v /system/priv-app/WanboOSLauncher/WanboOSLauncher.apk
adb shell ls -lZ /system/priv-app/WanboOSLauncher/
```

El contexto SELinux debe quedar como `u:object_r:system_file:s0`.

## 5. Dejar WanboOS como Home

Primero verificar que el paquete está instalado y declara HOME:

```bash
adb shell pm list packages | grep com.tv.launcher.custom
adb shell cmd package resolve-activity -c android.intent.category.HOME
```

Opción A (si el comando existe en Android 9 / tu build):

```bash
adb shell cmd package set-home-activity com.tv.launcher.custom/.MainActivity
```

Opción B (retirar el Home anterior):

```bash
adb shell pm disable-user --user 0 com.google.android.tvlauncher
adb reboot
```

Al quedar un solo paquete con `CATEGORY_HOME`, Android lo usa al arrancar.

## 6. Verificar

```bash
adb shell cmd package resolve-activity -c android.intent.category.HOME
adb shell dumpsys activity activities | grep -m1 mResumedActivity
```

En pantalla: Inicio con el tema webOS editable, recorrido con D-pad, Ajustes y
Personalización accesibles.

## Reversión

```bash
adb root && adb remount
adb shell pm enable com.google.android.tvlauncher
adb push respaldo-wanbo/TVLauncher.apk /system/priv-app/TVLauncher/TVLauncher.apk
adb shell chmod 644 /system/priv-app/TVLauncher/TVLauncher.apk
adb shell restorecon -v /system/priv-app/TVLauncher/TVLauncher.apk
adb shell rm -rf /system/priv-app/WanboOSLauncher
adb reboot
```

Si el equipo no arranca la interfaz, el sistema sigue siendo accesible por ADB:
repetir la reversión sin depender del launcher.

## Riesgos y límites

- `adb root` y `remount` dependen de que el build sea `userdebug` y la verity esté
  desactivada.
- Deshabilitar `com.google.android.tvlauncher` elimina el Home de Google; no se
  pierden apps ni datos.
- Esta vía no modifica `boot`, `recovery`, `vbmeta` ni `bootloader`.
- La OTA modificada (`update-wanboos-signed.zip`) queda como alternativa futura;
  requiere resolver AVB y una ruta de rescate.
