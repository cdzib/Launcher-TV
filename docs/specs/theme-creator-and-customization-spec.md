# Creador de temas — Personalización

Estado: IMPLEMENTED

## Objetivo
Integrar `design/theme-creator-proposal.html` en el editor Compose existente, con
el mismo lenguaje visual de Personalización. Mantener el contrato de composición,
las diez plantillas y todos los controles avanzados; no reducir a los tres ejemplos del HTML.

## Hechos y decisiones
- `HomeScreen` mantiene el borrador y guarda archivos ZIP mediante el almacén existente.
- Crear aplica el tema guardado; editar un tema inactivo no lo aplica. Se conserva esta regla.
- El formato permite 1–12 bloques, máximo tres del mismo tipo. Se conservan estos límites.
- La vista previa usa el Canvas real con acciones desactivadas, no una imagen de muestra.
- Se reutilizan colores y botones de Personalización; la paleta del tema solo afecta la vista previa.

## Criterios
- AC1: Diseño, Secciones y Apariencia se seleccionan con D-pad. Vista previa 16:9 siempre accesible y ampliable; indicadores de borrador y contexto por categoría.
- AC2: Diseño permite cambiar el nombre y elegir cualquiera de las plantillas reales. Cambiar plantilla actualiza el borrador, no el tema activo.
- AC3: Secciones conserva selección, añadir tipos, mover, duplicar, restablecer y eliminar; no permite borrar el último bloque ni exceder los límites. Barra superior y menú lateral conservan todos sus ajustes.
- AC4: Apariencia conserva fondo, colores, margen y espaciado global. Los controles avanzados de cada bloque siguen disponibles en Secciones.
- AC5: Restablecer recupera nombre y composición con que se abrió el editor, previa confirmación. Cerrar/Atrás con cambios solicita descartar; sin cambios sale directamente. Cancelar la confirmación conserva el borrador.
- AC6: Guardar se deshabilita con nombre en blanco o guardado en curso. Se muestra estado Guardando y se impiden guardados duplicados; error conserva borrador y permite reintentar. Persistencia y aplicación existentes no cambian.
- AC7: Foco visible, iconos vectoriales, controles legibles/desplazables y previews sin acceso a repositorios. No lanzar apps desde la vista previa.

## Validación
Baseline de unit tests antes de cambiar UI. Caracterización de operaciones de bloques y
límites, pruebas adicionales de límites inválidos si procede. Build + unit + lint.
Prueba en emulador: categorías, controles, añadir/reordenar, ampliación, confirmaciones,
guardar, biblioteca y reapertura. Infraestructura Compose instrumentada no existente;
registrar pruebas manuales y limitaciones sin afirmar cobertura automatizada de UI.

## Fuera de alcance
Cambios de esquema/ZIP, librerías nuevas, búsqueda online, sincronización y cambios en Home
ajenos al estado de guardado del editor.

## Trazabilidad y resultados
- AC1–AC4: `ThemeEditorContent` reemplaza la distribución anterior, reutiliza `SettingsColors` y `SettingsButton`, y conserva los inspectores. Las diez plantillas tienen miniaturas reales. El nombre aparece antes de la lista de plantillas. Selección y detalles de sección mantienen foco; navegación entra a plantillas sin abrir el IME.
- AC3: `ThemeEditorOperationsTest` caracteriza movimiento sin mutar el borrador, límites de adición/duplicación y serialización de los ocho bloques predeterminados y las diez plantillas. El movimiento fuera de límites tuvo RED real (`IndexOutOfBoundsException`), seguido de GREEN al ignorar índices inválidos.
- AC5: Cerrar/Atrás compara el borrador con los valores de entrada; la confirmación enfoca la opción no destructiva. Restablecer recupera ambos valores. Comprobados manualmente cancelar salida y restablecer tras reordenar.
- AC6: `HomeScreen` evita reentradas, bloquea cambios de borrador durante guardado y libera el estado en `finally`. Se conserva el almacén y la regla previa de aplicación del tema.
- AC7: Controles de cantidad y selección se adaptaron al inspector estrecho, con iconos vectoriales y descripciones accesibles. Tras cerrar el IME, arriba/abajo salen de campos de texto. Previews puros: Diseño, Secciones, Apariencia y Guardando.
- Canvas: escalado de previews pequeños sin el mínimo de tamaño interactivo; se evita superponer el nombre automático a un bloque de título ya existente.
- Gate ejecutado: Gradle 8.9, `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` correcto. 56 pruebas, cero fallos/errores; lint cero errores (125 incidencias de proyecto, ninguna localizada en los archivos del editor).
- Emulador TV `emulator-5554`, 3840×2160: navegación D-pad por categorías, edición y movimiento de sección, añadir banner, apariencia, confirmación de descarte y cancelación, restablecer, entrada/salida del nombre con IME y ampliación con cierre visible. La primera ampliación recortaba el cierre; corregida y comprobada de nuevo con bounds visibles y foco en Cerrar.
- Persistencia E2E: creación de `StudioQA`, guardado, aparición en la biblioteca y reapertura como edición con el nombre conservado. Se eliminó únicamente ese tema de prueba desde la UI; el conteo de biblioteca volvió a siete.

## Riesgos residuales
Pendientes TV física, matriz de resolución/font scale, prueba instrumentada de cada inspector,
errores forzados de almacenamiento y estrés de guardados repetidos. La serialización está
cubierta en unit tests; las interacciones Compose tienen evidencia manual, no automatizada.
No se declara VERIFIED de release con esas validaciones pendientes.
