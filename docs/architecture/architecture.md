# Arquitectura modular de Launcher-TV

## Módulos y responsabilidad

| Módulo | Responsabilidad |
|---|---|
| `:app` | Activity, host de navegación, Activity Results, assets incluidos y composición Hilt |
| `:core:model` | Kotlin/JVM: modelos, contratos de repositorios y casos de uso; sin Android, Compose ni Hilt |
| `:core:apps` | Implementación Android de consulta, observación y apertura de paquetes |
| `:core:data` | DataStore, biblioteca de temas, codecs ZIP/JSON y cliente de descarga HTTPS |
| `:core:ui` | Tema Compose, recursos compartidos, artwork, contratos del canvas y controles TV reutilizables |
| `:core:theme` | Renderizado de composiciones, layouts nativos y plantillas del editor |
| `:feature:home` | Estado, intents, effects, ViewModel y contenido Home |
| `:feature:search` | Búsqueda, resultados y teclado propio |
| `:feature:settings` | Personalización y selección de temas; emite `AppearanceAction` |
| `:feature:editor` | Borrador, inspectores y previews del editor; emite callbacks de guardado |
| `:feature:remote` | UI del catálogo remoto de temas; no es un servidor de control remoto por celular |
| `build-logic` | Build incluido con plugins de convención Kotlin DSL; no forma parte del APK |

## Dirección de dependencias

```text
app ──> features ──> core:model / core:ui / core:theme
 │                                      │
 └──> core:apps / core:data ──> core:model

core:theme ──> core:ui ──> core:model
```

- Ninguna biblioteca depende de `:app`.
- Una feature no depende de otra feature.
- Ningún core depende de features.
- La presentación no depende de implementaciones de `:core:data` o `:core:apps`.
- Las pruebas pueden conectar infraestructura real: el editor tiene una dependencia `testImplementation` al codec de temas.
- Las dependencias son `implementation`; no se reexportan árboles completos mediante `api`.
- Los tipos públicos usados en una firma deben declararse como dependencia directa por quien los consume.

`verifyModuleBoundaries` comprueba estas reglas en el grafo Gradle y se ejecuta con `preBuild` y `check`.
El compilador mantiene además la visibilidad `internal` por módulo.

## Entradas públicas e integración

- `HomeContent`: recibe estado y callbacks; el host observa `HomeViewModel` y consume sus effects.
- `AppSearchDialog`: recibe apps, apertura y cierre.
- `LauncherSettingsDialog`: recibe apariencia, biblioteca y callbacks de integración. No conoce `HomeIntent`.
- `ThemeCompositionEditorDialog`: recibe borrador, datos de preview, estado de guardado y callbacks.
- `RemoteThemeCatalogDialog`: recibe catálogo, loading/error y callbacks de descarga.

`LauncherScreen.kt` en `:app` compone estos destinos y traduce `AppearanceAction` a intents de Home.
Las features no construyen pantallas de otras features ni conocen `MainActivity`.
Los Activity Results de importación, exportación y fondos pertenecen al host.

Los modelos `InstalledTheme`, `RemoteTheme`, `ThemeComposition` y `LauncherAppearance` están en `:core:model`.
La UI no necesita importar parsers ni modelos internos de almacenamiento para mostrarlos.
`CanvasData` es el contrato de renderizado: suministra datos y callbacks sin acoplar el canvas a PackageManager.

## Crear una feature

1. Crear `feature/<nombre>/build.gradle.kts`:

```kotlin
plugins {
    id("launcher.android.compose")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    testImplementation(libs.junit)
}
```

2. Registrar `include(":feature:<nombre>")` en `settings.gradle.kts`.
3. Añadir `implementation(project(":feature:<nombre>"))` únicamente en `:app`.
4. Crear un Composable de entrada público con estado y callbacks. Mantener contenido e implementación `internal`.
5. Conectar la entrada desde el host de aplicación. La salida y acciones entre features son callbacks tipados.
6. Si necesita un ViewModel Hilt, agregar plugins `libs.plugins.ksp` y `libs.plugins.hilt`,
   `implementation(libs.hilt.android)` y `ksp(libs.hilt.compiler)`, como `:feature:home`.
7. Añadir únicamente las bibliotecas utilizadas. Crear recursos propios en la feature; compartirlos en `core:ui` cuando tengan consumidores reales.
8. Compilar la feature, ejecutar sus pruebas y comprobar `:app:assembleDebug`.

`launcher.android.library` configura Android, minSdk 26, compileSdk 35, Java/Kotlin 17 y namespace según la ruta del módulo.
`launcher.android.compose` añade Compose, Compose TV y tooling de Debug usando el mismo catálogo de versiones.
Las versiones de AGP, Kotlin, Hilt y Compose se modifican en `gradle/libs.versions.toml`.
Los módulos Kotlin puros siguen el ejemplo de `:core:model`.

## Añadir componentes y temas

- Componentes exclusivos: dentro de su feature, sin hacerlos públicos por defecto.
- Controles y artwork reutilizados: `:core:ui`, con `Modifier`, estado recibido y callbacks.
- Nuevas composiciones basadas en bloques existentes: `:core:theme/ThemePreset.kt` o paquetes ZIP del editor.
- Un bloque nuevo requiere actualizar el contrato en `:core:model`, el codec en `:core:data`,
  el renderer en `:core:theme` y el inspector en `:feature:editor`, junto con pruebas de ida y vuelta.
- No incluir datos del sistema, repositorios ni navegación raíz dentro de un componente de renderizado.
- No cargar todos los bitmaps de aplicaciones en un modelo de dominio; el artwork se resuelve en la frontera UI/plataforma.

## Compatibilidad y alcance de la extracción

Se conservan applicationId, claves y nombre de DataStore (`launcher_prefs`), UUID de temas y formatos ZIP/JSON existentes.
Los nombres persistidos de `ThemeId` no se renombran sin una migración.
Se adapta el motor existente; el prototipo externo `core-theme` no sustituye la biblioteca ni el editor.
Los cambios de comportamiento pendientes (Apps independiente, contenedor único de búsqueda, voz, modo ligero)
son trabajos separados de esta extracción estructural.

## Verificación

Desde una instalación de Gradle 8.9 con JDK 17 y SDK Android configurado:

```text
gradle verifyModuleBoundaries
gradle testDebugUnitTest :core:model:test
gradle :feature:search:compileDebugKotlin
gradle :app:assembleDebug :app:assembleRelease
gradle lintDebug
```

El selector raíz `testDebugUnitTest` ejecuta las pruebas de todos los módulos Android.
`:app:testDebugUnitTest` por sí solo no ejecuta las suites trasladadas a las bibliotecas.
El artefacto release requiere firma externa para su distribución.
