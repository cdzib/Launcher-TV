# webOS declarativo — candidato de migración

## Acceso

- Crear tema → Diseño → webOS.
- Paquete importable incluido: `app/src/main/assets/demo-themes/webos-editable.zip`.
- El paquete `webos-demo.zip` y `WebOsLayout` siguen siendo la referencia nativa; no se reemplazan.

## Contenido

La composición tiene cinco bloques de contenido dinámico:

1. Portada: primera app del primer acceso, o primera app instalada como fallback.
2. Cinco accesos configurables: pulsación larga para cambiar; los vacíos permiten elegir app.
3. Fila de iconos de aplicaciones instaladas y acceso a Apps.
4. Encabezado con edición de los cinco accesos.
5. Tarjetas con arte real de aplicaciones instaladas.

Los accesos utilizan el almacenamiento existente mediante `LocalShortcutPackages` y
`LocalSetShortcut`; no se incrustan nombres de paquetes ni credenciales en el ZIP.
El preview usa el mismo motor, pero no ejecuta acciones ni modifica accesos.

## Extensiones declarativas

- `appContent`: `FEATURED_APP`, `SHORTCUTS`, `APP_ICONS`, `SHORTCUT_EDITOR`, `APP_CARDS`.
  Cada valor se valida contra el tipo de bloque correspondiente.
- `startInset` / `endInset`: márgenes por bloque, en unidades de referencia.
- `topPadding`: margen superior de la composición.
- `rail.overlay`: permite navegación superpuesta sin desplazar todas las filas.

El motor interpreta estos campos independientemente del nombre o `layout` del tema.
Los paquetes anteriores sin estos campos mantienen su ruta de renderizado.
Estos ZIP requieren la versión del launcher que reconoce las extensiones; un importador
anterior de validación estricta puede rechazarlos.

## Verificación y límites

- Tests del contrato: round trip, rechazo de roles desconocidos e incompatibles.
- Tests del ZIP: coincide con la composición del editor y mantiene separada la referencia nativa.
- La geometría usa 960 × 540 como referencia, a partir de las medidas del layout nativo.
- Sigue pendiente certificar paridad visual y navegación E2E en TV, tamaños distintos de
  16:9, ausencia de apps, cambios de accesos, reinicio y exportación/importación en dispositivo.
- Se reutilizan widgets de contenido instalado, no `WebOsLayout`. No se elimina aún el layout nativo.
