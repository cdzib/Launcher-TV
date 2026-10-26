# Theme Generation Engine

Estado: READY

## Objetivo

Permitir que nuevos diseños de launcher se definan como datos de tema importables y sean
renderizados por el motor común, sin añadir un Composable, un `ThemeId` ni una rama de
navegación por cada tema.

## Contexto y comportamiento actual

- `ThemeComposition` ya describe bloques, orden, estilos, navegación y tamaños.
- `CompositionCanvas` renderiza composiciones y `CustomThemeLayout` la presenta de forma
  interactiva.
- Los paquetes v2 pueden incluir `composition`, guardarla e importarla.
- `HomeContent` aún conserva layouts específicos para los temas integrados; los temas
  instalados con composición usan el renderer común.
- Los datos de producto disponibles son apps instaladas y accesos favoritos. No hay fuente
  de catálogo multimedia o canales en vivo.

## Alcance de este cambio

- Consolidar el camino de temas importados/editados para que la composición controle la
  estructura del diseño y el renderer genérico lo presente.
- Poder representar diseños de launcher basados en rail, hero/banner, apps favoritas,
  grillas y filas con propiedades configurables ya soportadas, sin registrar un nuevo
  `ThemeId` por ZIP.
- Proveer dos configuraciones demo importables que muestren un dock inferior y un rail
  lateral mediante el mismo renderer.
- Mantener paquetes existentes y layouts sin composición con su comportamiento compatible.
- Añadir cobertura de prueba a la generación/serialización/importación de configuraciones y
  al dispatch común.
- No incluir assets de terceros ni inventar contenido; los bloques se alimentan de datos
  disponibles o quedan fuera hasta existir un contrato real.

## Fuera de alcance

- Generar o compilar código Kotlin desde un ZIP.
- Ejecutar scripts, plugins o código activo incluido en temas.
- Implementar proveedor de películas, series, canales en vivo o clima.
- Convertir automáticamente cualquier diseño arbitrario en componentes nativos nuevos:
  capacidades visuales nuevas del motor pueden requerir evolución del renderer, pero no
  deberían requerir una nueva pantalla para cada combinación de tema.

## Reglas

- RB1: Un tema con composición se dibuja mediante el renderer de composición, no mediante
  una selección de Composable por nombre/ID de tema.
- RB2: Un tema sin composición conserva el fallback actual del layout integrado.
- RB3: El manifiesto solo declara configuración y recursos validados; nunca código ejecutable.
- RB4: Los elementos interactivos mantienen apertura de apps, Ajustes y navegación disponible
  con D-pad y toque donde aplique.
- RB5: El contenido de filas y hero debe venir de apps instaladas/favoritos o de datos
  legítimamente provistos por el contrato de la app; no se simulan catálogo o recientes.
- RB6: Agregar una nueva composición basada en capacidades existentes no requiere modificar
  `ThemeId` ni añadir una pantalla Kotlin.

## Criterios de aceptación

- AC1: Una composición importada se puede seleccionar y renderizar sin agregar un `ThemeId`
  ni una rama de pantalla dedicada.
- AC2: Cambiar el orden/configuración de bloques cambia la vista generada y mantiene las
  acciones reales de las apps.
- AC3: Los manifiestos y temas ZIP existentes siguen siendo importables; los formatos
  antiguos sin composición conservan su fallback.
- AC4: Temas desconocidos, campos inválidos o recursos inseguros se rechazan sin ejecutar
  contenido del archivo.
- AC5: Los diseños basados en capacidades actuales pueden exportarse, reimportarse y
  seleccionarse conservando su composición.
- AC6: No se muestran datos multimedia, clima o recientes ficticios en la UI de producción.
- AC7: Las demos Launch Dock y Side Stream se instalan y renderizan desde su composición,
  sin agregar layouts ni IDs de tema dedicados.

## TBD no bloqueantes

- Qué capacidades de composición nuevas se requieren para igualar exactamente las imágenes
  de referencia. Propuesta inicial: reutilizar hero/asset global, rail y filas/grillas de
  apps; distinguir contenido multimedia requiere fuente de datos y modelo separados.
- Si se añade un bloque declarativo nuevo, qué datos legítimos lo alimentarán.

## Plan de pruebas

- Unit tests del validador/serializador y round-trip del ZIP.
- Tests del camino de selección/wiring cuando puedan aislarse con el seam existente.
- Compilar módulos afectados, ejecutar tests relevantes y lint.
- Validar manualmente foco D-pad y toque en Android TV/emulador cuando haya dispositivo.

## Conservación del buscador existente

- Los controles Buscar de los layouts integrados usan `LocalOpenSearch`, separado de
  `LocalOpenApps`, y abren el `AppSearchDialog` existente sin reemplazar su implementación.
- Las pestañas Buscar/Search explícitas se conservan aunque `showSearch` esté habilitado;
  solo se añade un icono implícito cuando no existe una pestaña equivalente. La barra
  MINIMAL conserva su configuración sin agregar navegación no solicitada.
- `ThemeSearchNavigationTest` reproduce el acceso desaparecido, cubre barras de texto e
  iconos y comprueba que Buscar y Apps disparen destinos distintos al conservar assets.
- Gates ejecutados correctamente: tests de `core:ui`, `feature:search`, `feature:home`,
  `:app:assembleDebug` y `:app:lintDebug`. El análisis lint de Search se repitió aislado
  tras un error interno de lectura de clases en la primera ejecución, y finalizó correcto.
- Pendiente comprobar apertura, teclado y retorno con D-pad en Android TV real/emulador.

## Acceso condicional a personalización

- Conservar la acción propia del tema si ya abre personalización; Free My TV reutiliza
  su control «Personalizar», sin añadir otro acceso.
- Perfil: si el rail declara tanto avatar decorativo como opción «Perfil»/«Profile»,
  conservar solo la opción del menú. Side Stream contiene esta combinación; se resuelve
  al renderizar sin alterar ZIPs instalados. `ThemeProfileTest` cubre la regresión para
  los tres estilos de rail, mayúsculas, espacios y preservación de avatares no duplicados.
- Solo cuando falta, integrar un icono de paleta junto a Ajustes en la barra o menú
  lateral. No superponer un botón de texto sobre Inicio.
- En composiciones, reconocer acciones de personalización en tabs visibles, rail y chips;
  un título decorativo o una opción oculta no reemplazan el acceso.
- Regresión: `ThemePersonalizationTest` cubre detección, ausencia de duplicados y destinos
  separados de personalización/sistema, incluidos nombres con acentos y copia de assets.
- Verificación ejecutada: `:core:ui:testDebugUnitTest`, `:feature:home:testDebugUnitTest`,
  `:app:assembleDebug` y `:app:lintDebug` correctos. Pendiente validación visual y D-pad
  en Android TV; los previews no sustituyen esa comprobación.

## Definition of Done

- El camino de composición seleccionada es genérico y no depende de un `when` de diseños
  personalizados.
- Los casos legacy sin composición siguen teniendo fallback.
- Validación, interacción, ausencia de datos y compatibilidad están cubiertas por pruebas
  ejecutadas; las pruebas manuales no disponibles quedan explícitas.
