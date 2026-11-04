# VIDAA

Tema declarativo inspirado en el launcher de VIDAA: barra superior de iconos circulares,
hero con ondas magenta, cuadrícula de dos filas con tarjetas blancas y una fila de
recomendados.

## Paquete

- Preset: `ThemePresets` → `"VIDAA"`.
- ZIP: `app/src/main/assets/demo-themes/vidaa.zip` (seleccionable en Personalización →
  importar tema incluido → VIDAA).
- Fondo: `core/ui/src/main/res/drawable-nodpi/vidaa_waves.png`; el mismo PNG viaja como
  `assets/background.png` dentro del ZIP.

## Composición

- `topBar`: estilo `ICONS` (Buscar, Apps, Notificaciones, Perfil, Ajustes) con reloj de
  24 h; sin fecha.
- Bloques: `HERO` transparente con el nombre y el lema, `APPS_GRID` de 7 columnas × 2
  filas con tarjetas `LIGHT`, título “Top Picks” y `APPS_ROW` con tarjetas `GLASS`.
- `bgStyle = WAVES`: fondo generativo propio, no reutiliza el paisaje de macOS ni las
  colinas de Horizonte. Solicita formato v3.

## Datos reales

Los iconos y nombres salen de las aplicaciones instaladas; “Top Picks” también usa apps
reales, no carteles de demostración. Inicio, Apps, Buscar y Ajustes conservan sus acciones.
No se inventan contenidos multimedia ni estados de red.

## Regeneración

```sh
swift docs/design/propuestas/generate-vidaa-waves.swift core/ui/src/main/res/drawable-nodpi/vidaa_waves.png
UPDATE_VIDAA_ARCHIVE=1 ./gradlew :app:testDebugUnitTest --tests '*VidaaThemeArchiveTest*'
./gradlew :app:testDebugUnitTest :core:data:testDebugUnitTest :core:ui:testDebugUnitTest :feature:editor:testDebugUnitTest :app:assembleDebug
```

El test normal solo verifica el ZIP incluido; la variable de entorno lo reescribe.

## Alcance visual

Es una adaptación al motor TV, no una copia pixel a pixel: tipografía Android, iconos
reales, foco D-pad y superficies translúcidas del motor difieren de la captura. El hero
ocupa el ancho del contenido y las ondas se desvanecen a negro para mantener el contraste
de las tarjetas.
