# Seguridad del dispositivo e imágenes de fondo

Estado: IMPLEMENTED — validación automatizada local PASS; validación manual en dispositivo pendiente.

## Objetivo

Reducir el riesgo de dejar el launcher o el dispositivo en un estado no recuperable y evitar que imágenes grandes agoten la memoria del proceso. Recuperar validaciones reproducibles del repositorio sin afirmar que un gate pasó si no se ejecutó.

## Contexto y comportamiento actual

- `AdvancedDeviceOperationPolicy` aplica una lista estática de paquetes protegidos. El servicio retira apps preinstaladas para el usuario actual por Shizuku.
- Las escalas de animación y las apps tienen confirmación incompleta: las acciones de animación se ejecutan desde el panel y la confirmación de app no describe claramente el uso de Shizuku.
- El host decodifica directamente a bitmap la imagen de fondo seleccionada; otras vistas ya usan muestreo.
- Este checkout tiene Gradle 8.9 configurado, pero no contiene `gradlew` ni `gradle-wrapper.jar`; no se encontró CI. Gradle no está disponible como comando en el entorno al iniciar el cambio.

## Alcance

1. Protección de paquetes críticos/launcher, consentimiento informado por acción privilegiada y reporte honesto de recuperación parcial.
2. Decodificación muestreada de imágenes seleccionadas para fondos y pasos de importación/exportación relacionados.
3. Gradle Wrapper versionado y CI para compilación, tests, lint y límites modulares, si se puede generar/verificar desde las herramientas disponibles.
4. Pruebas unitarias de las reglas puras y verificaciones de compilación pertinentes.

## Fuera de alcance

- Ejecutar operaciones Shizuku/ADB sobre dispositivos durante pruebas automatizadas.
- Garantizar compatibilidad con cada firmware de TV o implementar inventario específico de fabricante sin evidencia de sus paquetes/roles.
- Actualizar masivamente Android Gradle Plugin, Kotlin, Compose o target SDK.
- Cambiar formatos persistidos de apariencia o archivos de temas.

## Reglas

- RB1: no se puede retirar el paquete de la app actual, el paquete que tenga el rol HOME ni paquetes de Settings/componentes críticos conocidos.
- RB2: una operación privilegiada de dispositivo solicita confirmación específica antes de ejecutarse y comunica objetivo, alcance y reversión conocida.
- RB3: un error de aplicación y un error de rollback se reportan por separado; no se presenta una recuperación como exitosa si falló o no se verificó.
- RB4: bitmaps de fondos seleccionados por el usuario se decodifican con dimensiones limitadas; los bytes de origen pueden conservarse para persistencia/exportación, pero la vista no los decodifica a resolución completa.
- RB5: el wrapper usa la distribución Gradle ya fijada en `gradle-wrapper.properties`; ningún gate se marca PASS sin ejecución real.

## Criterios de aceptación

- AC1: pruebas prueban que launcher actual y paquetes de Settings/componentes incluidos en la política no son retirables, y que una app ordinaria sí puede serlo.
- AC2: las acciones Shizuku de cambios de animación, retirada y restauración requieren confirmación separada que identifique la acción concreta.
- AC3: si una mutación de escalas falla y el rollback también falla, el resultado identifica ambos fallos y advierte que el estado puede ser parcial.
- AC4: imágenes de dimensiones superiores al límite visual se decodifican con muestreo, sin cargar el bitmap completo; imagen inválida/URI fallido no bloquea el launcher.
- AC5: wrapper y workflow CI versionados ejecutan `verifyModuleBoundaries`, tests, lint y assemble Debug/Release; resultados locales se reportan con precisión.

## Decisiones / TBD

- HOME y Settings predeterminados se resuelven mediante `PackageManager.resolveActivity`; si HOME no se puede resolver se bloquean las retiradas/ADB para paquetes en vez de permitirlas.
- Límite de bitmap: lado máximo de 1920 px, acorde con resolución de TV y con el muestreo existente para assets de temas.
- El wrapper mantiene Gradle 8.9 y verifica el SHA-256 de la distribución.
- Fire TV y paquetes de fabricante requieren validación manual; la lista estática cubre los identificadores conocidos y la resolución dinámica reduce la dependencia de esa lista.

## Plan de testing

- Unit: política de paquetes protegidos, argumentos y reporte de rollback con ejecutor falso si se extrae el seam mínimo.
- Android/Compose: prueba unitaria pura de cálculo de `inSampleSize`; prueba manual de carga de URI e imagen grande en TV.
- Build: `:core:model:test`, `:app:testDebugUnitTest`, `:app:lintDebug`, `verifyModuleBoundaries`, `:app:assembleDebug`, `:app:assembleRelease`.
- Manual: Shizuku concedido/revocado/detenido, cancelación de confirmaciones, aplicación/restauración parcial y dispositivos Android TV/Fire TV.

## Trazabilidad

| Criterio | Componente | Validación | Estado |
|---|---|---|---|
| AC1 | `AdvancedDeviceOperationPolicy` | `:core:model:test` | PASS |
| AC2 | `DeviceToolsDialog` | `:app:compileDebugKotlin`; confirmar/cancelar en TV pendiente | Código PASS; manual pendiente |
| AC3 | `DeviceShellUserService` | `:app:compileDebugKotlin`; rollback dual en dispositivo pendiente | Código PASS; manual pendiente |
| AC4 | Decodificación de fondo en `LauncherScreen` | `ImageSamplingTest`; build app; imagen grande en TV pendiente | Unit/build PASS; manual pendiente |
| AC5 | Gradle Wrapper/workflow | `verifyModuleBoundaries`, `test`, `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `assembleRelease` | PASS local; CI remoto pendiente |

## Validación local

Con Gradle Wrapper 8.9: `./gradlew verifyModuleBoundaries test testDebugUnitTest lintDebug :app:assembleDebug :app:assembleRelease --no-daemon` — BUILD SUCCESSFUL. AGP 8.5.2 emite advertencia de compatibilidad probada hasta compileSdk 34 (el proyecto usa 35); no bloquea el gate.
