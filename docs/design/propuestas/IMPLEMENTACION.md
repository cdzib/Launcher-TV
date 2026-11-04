# Temas minimalistas

Las cuatro propuestas se encuentran en `ThemePresets` y en el selector de importación
de temas incluidos. No agregan `ThemeId` ni renderers específicos por nombre.

Paquetes: `app/src/main/assets/demo-themes/{orbita,bruma,horizonte,estudio}.zip`.
Importar desde Personalización → importar tema incluido y seleccionar su nombre.
La instalación es explícita; no se migran ni reemplazan temas instalados.

## Datos reales

- Los iconos y nombres provienen de aplicaciones instaladas.
- Horizonte usa exclusivamente favoritos y muestra una indicación cuando no hay ninguno.
- Los relojes usan hora/fecha locales. El clima solo aparece con el proveedor configurado
  en Órbita y Bruma, nunca con datos de la maqueta.
- Inicio, Apps, Buscar y Ajustes conservan las acciones reales. Ajustes del sistema y
  Personalización son acciones diferentes.
- Apps y Buscar heredan superficies, contraste, radios y fondo declarativos.

## Motor y compatibilidad

El bloque `clock` usa los controles existentes de tamaño, alineación y color. `RIDGES`
es un fondo reutilizable de colinas, no un layout de Horizonte. `showDate` es opcional
en la barra superior. Estas capacidades solicitan formato v3; los valores por defecto
de las composiciones antiguas y los formatos v1/v2 permanecen válidos.

El PNG original está en `core/ui/src/main/res/drawable-nodpi/ridges_landscape.png`;
`generate-landscape.swift` reproduce los trazados SVG del HTML sin texto ni apps de
demostración. El mismo PNG se incluye en Horizonte como `assets/background.png`.
`ThemePreset.backgroundAssetName` identifica el original para exportar el paquete.

## Regeneración y pruebas

```sh
swift docs/design/propuestas/generate-landscape.swift core/ui/src/main/res/drawable-nodpi/ridges_landscape.png
GENERATE_MINIMALIST_THEMES=1 ./gradlew :app:testDebugUnitTest --tests '*MinimalistThemeArchiveTest' --rerun-tasks
./gradlew :app:testDebugUnitTest :core:data:testDebugUnitTest :core:ui:testDebugUnitTest :feature:editor:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

La regeneración usa `ThemeArchiveWriter`, UUID estables por nombre y verificación de
lectura/instalación/recarga; el test normal no modifica paquetes. Comprueba paridad de
composición y de los bytes del paisaje, incluidos los extraídos a almacenamiento privado.
Hay previews de los cuatro presets y del estado sin favoritos.

## Ajustes de fidelidad

- Los relojes de los cuatro temas usan formato de 24 horas y fecha completa, y el editor
  permite elegir sistema, 24 h o 12 h por barra o bloque.
- Se retiraron las bandas de fondo que Bruma y Horizonte generaban detrás de las filas; ahora
  el color vive en cada tarjeta.
- Bruma usa tarjetas claras sobre el fondo claro y Horizonte separa favoritos de una fila
  adicional de aplicaciones.
- Si el clima no está configurado se muestra un aviso discreto en el lugar del widget, sin
  inventar temperatura ni condición.
- La vista previa al importar un ZIP renderiza la composición real; antes era un rectángulo
  vacío.

## Alcance visual

Son adaptaciones al motor TV, no capturas del HTML: tipografía Android, iconos reales,
tarjetas y foco D-pad difieren de la maqueta. El paisaje mantiene los trazados originales
con aproximación de gradientes. El material translúcido utiliza el backdrop compartido,
no el filtro CSS de navegador. No se incluye el estado Wi-Fi ficticio ni descripciones
inventadas de medios. Pendiente comparación interactiva de los cuatro temas en TV.
