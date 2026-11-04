# Editor de composición para temas de usuario

Estado: IMPLEMENTED

## Objetivo

Permitir que cada usuario adapte la composición visual del launcher con un editor para
mando Android TV y mediante edición/importación de JSON, sin ejecutar código ni permitir
layouts arbitrarios dentro de paquetes ZIP.

## Contexto actual

- Hay siete perfiles con composiciones Compose propias.
- El manifiesto de tema v1 contiene un `layout` integrado, acento, fondo y forma de tarjeta.
- `LauncherAppearance` y el renderizado del home no representan bloques/filas configurables.
- La spec `advanced-theme-customization-spec.md` inicialmente excluía layouts arbitrarios,
  pero ahora el usuario pide temas con composición distinta. Se mantiene la decisión de
  seguridad: composición solo con componentes nativos permitidos y datos inertes.

## Alcance propuesto

- Los siete perfiles existentes se conservan sin cambios y funcionan como plantillas iniciales.
- El selector de perfiles visuales se retira de Ajustes; la creación comienza con diseños
  base sencillos y personalizables, sin eliminar compatibilidad con temas previamente creados.
- Añadir composición v2 como lista ordenada de 1..12 bloques: `title`, `apps_row`,
  `apps_grid`, `favorites_row`, `spacer`. Tipos y propiedades son enum/valores numéricos
  cerrados; los bloques no llevan contenido de texto/URLs/acciones.
- `apps_row` y `favorites_row`: `maxItems` 1..12; `apps_grid`: `columns` 2..8,
  `maxItems` 1..24; `spacer`: `weight` 1..4; título usa el nombre del tema del manifiesto.
- Un bloque puede aparecer una vez, excepto `spacer` (hasta 3); bloquear duplicados para los
  demás tipos. No hay campos desconocidos, rutas, expresiones, scripts, intents ni URLs.
- El editor visual del TV permite seleccionar plantilla, añadir/quitar/reordenar bloques y
  ajustar sus opciones con D-pad; vista previa y guardar como tema nuevo.
- Los temas de la biblioteca local ofrecen “Editar”; guardar reemplaza de forma atómica el
  mismo UUID, conservando los assets/fondo y metadatos de versión. Cancelar no modifica el ZIP.
- La edición JSON ocurre fuera de la app: exportar un archivo declarativo de ejemplo/tema,
  editarlo e importarlo como ZIP por los mecanismos existentes. No se requiere editor de
  texto en pantalla en esta fase.
- ZIP v1 continúa siendo válido y se interpreta como tema con plantilla integrada. ZIP v2
  añade `composition` en el manifiesto, validada con listas permitidas y límites estrictos.
- Fallback seguro a la plantilla integrada si una composición no es soportada; no perder el
  tema activo ni la biblioteca ante un paquete inválido.

## Fuera de alcance inicial

- Código Compose, plugins, HTML/JS, bytecode, layouts XML externos o widgets ejecutables.
- Componentes arbitrarios definidos por usuarios, acciones/intent personalizados, contenido
  dinámico remoto, reproductores y feeds externos.
- Editor JSON de texto completo dentro del televisor.
- Rediseñar Settings, navegación global u otras pantallas fuera del home.

## Reglas

- RB1: todas las composiciones se renderizan con un catálogo cerrado de componentes que
  pertenecen a la app.
- RB2: el orden y las propiedades se validan antes de guardar/aplicar y se limitan en tamaño,
  número de componentes y valores numéricos.
- RB3: v1 conserva exactamente su comportamiento actual; v2 no puede degradar ni invalidar
  paquetes v1 instalados.
- RB4: toda edición visual puede cancelarse; guardar una creación instala un tema nuevo y
  guardar una edición reemplaza el existente tras mostrar la vista previa en vivo.
- RB6: editar un tema instalado mantiene su UUID y no crea duplicados; fallos al validar o
  reemplazar conservan la versión instalada anterior.
- RB5: JSON y ZIP importados nunca definen permisos, navegación ejecutable ni referencias
  remotas.

## Decisiones/TBD

- D1: edición JSON es exportar/editar externamente/importar; editor visual es in-app. (Acordado
  con la respuesta “ambas”.)
- D2: un tema v2 conserva un perfil integrado como base de paleta/fondo, y su composición
  sustituye el layout del inicio mientras ese tema está activo.
- D3: editar un tema instalado crea un tema nuevo con UUID; el original no se modifica.

## Criterios de aceptación propuestos

- AC1: las siete composiciones actuales siguen renderizando igual para apariencias y temas v1.
- AC2: el editor visual puede crear una composición con componentes permitidos, reordenarla,
  previsualizarla, cancelar sin cambios y guardarla como tema nuevo.
- AC3: el renderizador procesa cada tipo permitido y respeta orden/visibilidad/configuración.
- AC4: el validador v2 rechaza componentes desconocidos, propiedades desconocidas, valores
  fuera de rango, listas vacías/inmensas y referencias a código/URL sin modificar estado.
- AC5: un tema ZIP v1 se importa y exporta sin perder compatibilidad; v2 se valida,
  previsualiza, aplica, exporta y vuelve a importar preservando la composición.
- AC6: la creación/edición del TV se puede completar con D-pad y foco visible.
- AC7: un tema instalado puede abrirse en edición; guardar actualiza el tema y su composición
  preservando identidad y apariencia, y cancelar conserva el archivo anterior.

## Trazabilidad

| Requisito | Criterio | Nivel | Estado |
|---|---|---|---|
| Compatibilidad de perfiles | AC1, AC5 | unit + manual | implemented; pendiente smoke test D-pad |
| Esquema y validador v2 | AC4, AC5 | unit | implemented |
| Render declarativo | AC3 | Compose | implemented; bloques nativos |
| Editor y persistencia | AC2, AC6 | Compose + unit | implemented; edición TV y guardado como tema nuevo |
| Import/export v2 | AC5 | unit + manual | implemented; manifiesto de ejemplo en `docs/theme-manifest-v2-example.json` |
| Editar tema instalado | AC7 | unit + manual | implemented; falta smoke test D-pad |

## Primer corte TDD

Probar el modelo/validador puro v2 antes de tocar el renderizador: un documento con
una fila de aplicaciones permitida se acepta; un componente desconocido se rechaza. Esto
expone temprano el contrato del ZIP y conserva la ruta v1.
