# Temas basados en las referencias locales

## Implementación

- webOS: `WebOsLayout.kt`, rail, banner, categorías, apps y muestras.
- Tizen: `TizenLayout.kt`, rail, banner, selector Para ti/Apps y apps.
- Apple TV, Android TV, Google TV, Fire TV y Roku: `ReferenceTvLayouts.kt`.
- Las imágenes de `desing ` determinan la composición, no el contenido mostrado.
  Ningún layout activo utiliza las capturas de películas o series. Banners y tarjetas
  muestran exclusivamente apps instaladas y las abren mediante el flujo de lanzamiento existente.
- Se utiliza el banner del paquete cuando existe y su icono como alternativa. Si no se puede
  cargar la imagen, el nombre permanece visible; no se inventa un destino de reproducción.
- Apps, búsqueda y ajustes tienen acciones reales. Las apps disponibles dependen del
  dispositivo; no se simulan Netflix, Disney+ u otros servicios que no estén instalados.
- Roku usa tres columnas; Apple usa rail translúcido; Android usa cabecera y filas;
  Google superpone navegación a portada; Fire usa portada, franja de apps y dos filas.
- webOS conserva cinco botones de colores, ahora con apps elegibles: clic abre la app,
  pulsación larga permite cambiarla y «Editar accesos» ofrece la misma opción con D-pad.
  Los cinco slots se persisten en DataStore junto a la apariencia, sin borrar preferencias
  anteriores. La primera asignación determina también el banner de webOS.
- Un slot vaciado o cuya app ya no existe muestra «Elegir app». Los slots nunca configurados
  usan inicialmente las apps instaladas disponibles. Se pueden repetir apps entre slots.

## Verificación

- APK debug y pruebas unitarias ejecutados.
- La revisión visual anterior de los siete temas precede a la sustitución de muestras.
- Pruebas de ViewModel: asignación, validación, vaciado y conservación al cambiar el tema.
- Pruebas de preferencias: escritura/lectura y eliminación de asignaciones antiguas.
- Pruebas de resolución: orden cambiante, desinstalación, defaults y slots vacíos.
- No hay pruebas instrumentadas automatizadas de todos los recorridos de foco.
- TMDB, reproducción y enlaces a contenido de terceros no forman parte de estos accesos.
