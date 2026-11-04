#!/usr/bin/env bash
# Instala el launcher en /system/priv-app del Wanbo T6 Max por ADB.
# Requiere: build userdebug, verity desactivada y APK de release firmado.
set -euo pipefail

APK="${1:-app/build/outputs/apk/release/app-release.apk}"
PACKAGE="com.tv.launcher.custom"
STOCK="com.google.android.tvlauncher"
TARGET_DIR="/system/priv-app/WanboOSLauncher"
BACKUP_DIR="respaldo-wanbo"

info() { printf '\n\033[1m%s\033[0m\n' "$*"; }
fail() { printf '\033[31m%s\033[0m\n' "$*" >&2; exit 1; }

[ -f "$APK" ] || fail "No existe el APK: $APK (ejecuta ./gradlew :app:assembleRelease)"
command -v adb >/dev/null || fail "adb no está en PATH"

info "Comprobando dispositivo"
adb get-state >/dev/null 2>&1 || fail "No hay dispositivo ADB conectado"
DEVICE=$(adb shell getprop ro.product.device | tr -d '\r')
TYPE=$(adb shell getprop ro.build.type | tr -d '\r')
[ "$DEVICE" = "marconi" ] || fail "Dispositivo inesperado: $DEVICE (se esperaba marconi)"
[ "$TYPE" = "userdebug" ] || fail "Se requiere build userdebug para escribir /system"

info "Activando root y sistema escribible"
adb root
adb wait-for-device
adb disable-verity || true
adb reboot
adb wait-for-device
sleep 20
adb root
adb wait-for-device
adb remount
adb shell mount | grep -q " /system .*rw" || fail "No se pudo montar /system en rw"

info "Respaldando launcher actual"
mkdir -p "$BACKUP_DIR"
adb pull /system/priv-app/TVLauncher/TVLauncher.apk "$BACKUP_DIR/" || true
adb shell pm path "$STOCK" > "$BACKUP_DIR/ruta.txt" || true

info "Instalando $APK"
adb shell mkdir -p "$TARGET_DIR"
adb push "$APK" "$TARGET_DIR/WanboOSLauncher.apk"
adb shell chown root:root "$TARGET_DIR/WanboOSLauncher.apk"
adb shell chmod 644 "$TARGET_DIR/WanboOSLauncher.apk"
adb shell restorecon -v "$TARGET_DIR/WanboOSLauncher.apk"

info "Verificando paquete"
adb shell pm list packages | grep -q "$PACKAGE" || fail "El paquete $PACKAGE no se reconoce"
adb shell cmd package resolve-activity -c android.intent.category.HOME || true

read -r -p "¿Deshabilitar $STOCK y reiniciar ahora? [s/N] " REPLY
if [[ "${REPLY:-n}" =~ ^[sS]$ ]]; then
    adb shell pm disable-user --user 0 "$STOCK" || true
    adb reboot
    info "Reiniciando. Verifica el nuevo Home en pantalla."
else
    info "Sin cambios de Home. Reversión: firmware-lab/runbooks/install-launcher-adb.md"
fi
