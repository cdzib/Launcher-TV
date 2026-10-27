# Temas y bases editables

La capacidad de edición depende de la presencia de `composition` en el paquete validado,
no del nombre del tema ni del valor de `layout`. Este último también define el estilo
base de un paquete editable.

## Paquetes incluidos

Todos los paquetes de demostración son declarativos y editables.

| Paquete | Composición |
| --- | --- |
| NOVA · Minimal Premium | Sí |
| Launch Dock | Sí |
| Side Stream | Sí |
| Android TV · Editable | Sí |
| Google TV · Editable | Sí |
| Fire TV · Editable | Sí |
| Roku · Editable | Sí |
| Tizen · Editable | Sí |
| webOS · Editable | Sí |
| Apple TV · Editable | Sí |
| Free My TV · Editable | Sí |
| Custom Layout Preview | Sí; paquete de validación |

Los ocho `*-demo.zip` originales sin composición se eliminaron del proyecto. En una
instalación nueva se instala y aplica `webos-editable.zip` como tema inicial. Si un tema
instalado no trae composición, el launcher resuelve la composición declarativa de su layout
(`defaultCompositionFor`), de modo que Inicio nunca queda vacío.

## Comportamiento

- La biblioteca permite aplicar cualquier paquete válido.
- Crear abre directamente un borrador vacío, sin diálogo ni selector de base.
- Las opciones del tema incluyen Editar, habilitado solo si el paquete tiene composición editable.
- El editor ofrece los presets declarativos como plantillas editables, con aviso de que
  todavía no son equivalentes a los layouts nativos. Seleccionarlos no ejecuta ni copia
  el Composable nativo; carga su `ThemeComposition`.
- Copiar conserva nombre identificable de la base, composición e imágenes; genera otra identidad al guardar.
- Nuevos paquetes importados se clasifican por su composición validada, no mediante listas de nombres.

## Migración al motor ZIP

El objetivo es sustituir los layouts específicos por composiciones importables, no
eliminar Compose como tecnología de renderizado.

Primera etapa: recuperar las diez plantillas y verificar su exportación/importación
sin pérdida de nombre o composición. La prueba `ThemeTemplateArchiveTest` cubre este contrato,
no equivalencia visual ni navegación.

Antes de retirar cada layout nativo hay que contrastar: distribución, paleta, medidas,
datos reales, navegación con mando, foco inicial, acciones de sistema y personalización,
preview frente a ejecución, y persistencia después de reiniciar.

| Referencia nativa | Plantilla actual | Estado |
| --- | --- | --- |
| Android TV | Android TV | Aproximación; paridad pendiente |
| Google TV | Google TV | Aproximación; paridad pendiente |
| Fire TV | Fire TV | Aproximación; paridad pendiente |
| Roku | Roku | Aproximación; paridad pendiente |
| Tizen | Tizen | Aproximación; paridad pendiente |
| webOS | webOS | Contenido dinámico y ZIP integrados; paridad visual pendiente (ver `webos-declarative.md`) |
| Apple TV | Apple tvOS | Aproximación; paridad pendiente |
| Free My TV | No existe aún | Falta plantilla declarativa |

Minimalista, Minimal + reloj y Cuadrícula limpia son composiciones independientes,
no conversiones de los layouts anteriores. Launch Dock, Side Stream y NOVA ya llegan
en ZIP con composición; eso tampoco implica por sí solo paridad visual certificada.

Los layouts nativos se conservan hasta superar esas verificaciones.

## Eliminación de los composables

Los layouts nativos (`WebOsLayout`, `TizenLayout`, `FreeMyTvLayout`, `BottomBarLayout`,
`ReferenceTvLayouts` y `LauncherLayoutPreview`) se eliminaron. Todo el render pasa por
`CompositionCanvas`:

- `HomeContent` solo usa `CustomThemeLayout`.
- La biblioteca (`SettingsThemePreview`) usa `CompositionCanvas` con
  `composition ?: defaultCompositionFor(layout)`.
- `LauncherSectionContentTest` valida las composiciones declarativas, no los layouts.
- El tema inicial y los paquetes v1 sin composición se resuelven con
  `defaultCompositionFor(ThemeId)`.

## Estado tras retirar los ZIP nativos

Los `*-demo.zip` ya no forman parte de `assets/demo-themes`. Consecuencias:

- Una instalación limpia solo recibe temas con `composition`; el motor declarativo cubre
  todo el catálogo incluido.
- Un tema v1 instalado previamente (sin `composition`) sigue dependiendo de los layouts
  nativos como fallback. Por eso los composables todavía no se eliminan.
- El fallback `layout -> ThemeComposition` está implementado y los layouts ya se retiraron.

