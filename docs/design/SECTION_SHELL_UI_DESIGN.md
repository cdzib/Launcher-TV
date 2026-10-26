# Section shell: Inicio, Buscar y Apps en una sola vista

Estado: EN REVISIÓN. Implementación Android no iniciada.

## Corrección acordada: navegación propia por tema

Esta decisión sustituye las referencias a una espina lateral universal en el documento original:

- No añadir un aside global a los ocho perfiles.
- Roku conserva su columna original de navegación; Apple TV conserva su panel original. No duplicarlos con un segundo rail.
- webOS y Tizen conservan sus controles laterales propios; Android TV y Google TV usan su cabecera y Fire TV su fila de acciones.
- Free My TV conserva su cabecera; los accesos de sección añadidos se presentan allí, no en una barra lateral.
- Al activar Inicio, Buscar o Apps se reemplaza el contenido dentro del área correspondiente del tema; la navegación original permanece disponible.
- El HTML es una propuesta adaptada, no una reproducción pixel-perfect del renderer Compose; los iconos son muestras y los controles de sección son demostrativos.

Prototipo actualizado: `docs/design/section-shell.html`. Las secciones siguientes documentan la propuesta inicial y deben interpretarse subordinadas a esta corrección; no usar la barra universal como requisito de implementación.

## Objetivo

Diseñar una navegación de secciones que mantenga una estructura persistente y cambie solo el contenido, en lugar de abrir pantallas o diálogos completos. El usuario debe percibir que sigue en el mismo launcher al pasar de Inicio a Buscar o Apps.

## Problema actual

Hoy las tres secciones viven en estados distintos del host:

- `showApps` y `showSearch` son booleanos independientes (`app/.../LauncherScreen.kt:165-166`).
- Apps se dibuja como una pantalla completa con su propio `Surface`, botón "‹" y `BackHandler` (`LauncherScreen.kt:195`, `:398-407`, `AppDrawerScreen` en `:775-812`).
- Buscar se abre en un `Dialog` a pantalla completa (`LauncherScreen.kt:751-760`).
- Buscar ya tiene una barra lateral propia de 72 dp y su propio teclado dentro de `SearchBaseLayout`; Inicio tiene las acciones de sección dentro de cada layout de tema.

El resultado es una navegación con tres "capas" distintas, foco que se pierde al volver y transiciones que se sienten como abrir otra app.

## Concepto: espina + escenario

**La espina** es una barra lateral de 104 px que nunca se mueve. Contiene la marca, Inicio, Buscar, Apps, Ajustes y el avatar. El indicador activo se desliza verticalmente entre iconos.

**El escenario** es el mismo rectángulo para las tres secciones. Cambia su contenido con un fundido y un desplazamiento corto; no cambia su posición, su tamaño ni el fondo general.

La metáfora es la de un televisor: la barra es el mando de entradas y el escenario es la pantalla. Cambiar de sección cambia la señal, no el televisor.

## Principios

1. La navegación no se mueve; el contenido sí.
2. Ninguna sección principal se abre en un `Dialog` ni en una `Activity`.
3. El foco se conserva por sección: al volver a Apps, el usuario regresa a la app donde estaba.
4. El teclado de búsqueda vive dentro del escenario y se comporta como un panel, no como una ventana.
5. La barra lateral se adapta a los tokens del perfil activo; no impone una paleta nueva.
6. El movimiento explica el cambio, no decora: 220–260 ms y desplazamientos de 16–24 px.
7. Inicio se construye con apps instaladas: el hero, las tarjetas y los accesos muestran apps reales, no películas ni contenido editorial inventado.

## Sistema visual

### Paleta (perfil webOS de referencia)

| Token | Valor | Uso |
|---|---|---|
| `ink` | `#07080C` | Fondo del escenario |
| `surface` | `#11141B` | Paneles elevados |
| `line` | `rgba(255,255,255,.075)` | Bordes y separadores |
| `text` | `#F2F5F7` | Títulos y contenido principal |
| `muted` | `#98A2B2` | Texto secundario |
| `dim` | `#7F8897` | Ayudas y metadatos de bajo énfasis |
| `accent` | `#37D9C5` | Foco, indicador activo y acción primaria |
| `accent-ink` | `#03211D` | Texto sobre accent |
| `warm` | `#F0C98D` | Clima y acentos editoriales |

En implementación estos valores deben salir de `LocalLauncherStyle.current.palette` y de los tokens existentes. La propuesta usa webOS solo como referencia; Fire TV, Google TV y el resto de perfiles deben conservar su acento.

### Tipografía

- Display: 46–48 px, peso 600, tracking −0.04 em.
- Título de fila: 18 px, peso 600, tracking −0.02 em.
- Cuerpo: 14 px, peso 400.
- Metadatos: 12–12.5 px.
- Etiquetas de la espina: 10.5 px, peso 600.

La implementación debe reutilizar la escala tipográfica existente de `:core:ui`; no crear una escala paralela solo para el shell. Avenir Next se usó solo en el prototipo visual; en Compose se mantiene la fuente del tema.

### Medidas

- Lienzo: 1920 × 1080.
- Espina: 104 px; iconos de 23 px; objetivo de 72 × 72 px.
- Escenario: 1696 px de ancho útil; padding 56 px lateral, 30 px superior, 24 px inferior.
- Radios: 24 px hero, 16–18 px tarjetas, 999 px chips. El radio de los iconos sale de `TileShapeChoice` y del perfil (8–24 px); el shell no impone uno.
- Rejilla de apps: 9 columnas × 4 filas visibles; icono 84 px; separación 16/18 px.
- Teclado: panel de 324 px de alto; teclas de 52 px; espacio inferior de 34 px.
- Foco: contorno blanco de 2 px con offset 3 px; selección con borde accent; escala 1.04–1.06.
- Objetivos mínimos: 48 × 48 px; en la práctica todas las acciones miden 52–72 px.

## Integración con los ocho perfiles

El shell es una capa del launcher, no un tema nuevo. La espina y el escenario consumen los tokens del perfil activo; el contenido de Inicio conserva la composición que ya existe para cada `ThemeId`.

| Perfil | Inicio actual | Inicio dentro del shell | Piel del shell |
|---|---|---|---|
| webOS | Hero + shortcuts + app row + cards | Hero de app + 5 shortcuts + carrusel + tarjetas | Teal `#37D9C5`; radios amplios; fondo casi negro |
| Fire TV | Hero + fila de acciones + dos filas de cards | Hero + apps + dos filas de cards | Azul `#53B8FF`; radio 14; fondo grafito |
| Roku | Columna lateral + grilla de 3 columnas | Título + grilla 3 × 3 de apps | Blanco como acento; radio 8; fondo morado |
| Tizen | Rail izquierdo + hero + tabs centrados + cards | Hero + tabs Inicio/Apps centrados + cards + app row | Índigo `#6B7BFF`; radio 18 |
| Android TV | Header + hero + apps + cards | Header + hero + apps + cards | Azul hielo `#8AB4F8`; radio 16 |
| Google TV | Header + hero + cards + apps | Header + hero + cards + apps | Azul suave `#A8C7FA`; radio 24 |
| Apple TV | Hero grande + panel izquierdo + cards | Panel de contexto + hero + cards | Ámbar `#FFC078`; radio 20; fondo cálido |
| Free My TV | Favoritos + destacado | Header + destacado + panel de 8 favoritos | Azul `#168CFF`; radio 16; fondo azul noche |

Reglas de integración:

- La espina sustituye **solo** las acciones de sección que hoy viven dentro de cada layout (Buscar, Apps, Ajustes). No reemplaza las filas, tabs de contenido, dock inferior ni tarjetas propias del tema.
- En Roku, la columna izquierda del layout pierde sus acciones de sección y conserva la grilla de apps; la espina ocupa ese rol.
- En Tizen, el rail izquierdo se elimina y los tabs centrados Inicio/Apps se mantienen como navegación de contenido.
- Free My TV conserva su panel de 8 favoritos y su destacado; no se convierte en una grilla de todas las apps.
- En Apple TV, el panel izquierdo pierde Buscar/Apps/Ajustes; si se conserva, queda como panel de contexto (hora, "Ahora", accesos), no como navegación principal.
- En webOS, la columna de acciones se sustituye por la espina; el dock inferior de apps se mantiene.
- El conmutador de perfil propuesto es la píldora "Perfil" de la línea de sistema: abre una bandeja dentro del mismo escenario, sin salir de la sección. En el prototipo también se cambia con `T`.
- Cada perfil define acento, fondo, texto, radio de iconos y superficies. El shell no debe introducir un acento fijo ni un radio global.
- Roku usa blanco como acento; por eso los botones rellenos invierten a texto oscuro y el foco de botón usa el tono oscuro del perfil.

## Secciones

### Inicio

- Línea de sistema persistente: perfil activo, clima y reloj.
- Saludo contextual ("Buenas noches") y fecha.
- Hero de app instalada con acción "Abrir" y "Ver apps"; usa el icono y el nombre de la app, no arte promocional inventado.
- webOS conserva sus cinco shortcuts de color, el carrusel de apps y las tarjetas de apps.
- Fire TV conserva hero, app row y dos filas de cards; no tiene tabs de contenido.
- Tizen conserva hero, tabs centrados Inicio/Apps, tarjetas y app row.
- Android TV y Google TV conservan su header y el orden apps/cards o cards/apps según el layout.
- Apple TV conserva el panel de contexto (TV, hora, "Ahora", tres apps) y el hero grande.
- Roku usa título + grilla 3 × 3 de apps; Free My TV usa destacado + panel de 8 favoritos.
- El contenido de Inicio sigue siendo responsabilidad de `HomeContent` y de los layouts de tema. La composición del prototipo es una referencia de jerarquía para el escenario, no una orden de reemplazar los ocho perfiles.

### Buscar

- Título y subtítulo.
- Campo de búsqueda de 980 px, botón de voz y filtros "Todo / Apps / Sistema".
- Resultados en tarjetas horizontales; el primero recibe foco al entrar.
- Chips de sugerencia.
- Teclado QWERTY dentro del escenario, con panel lateral de búsquedas recientes.
- Sin `Dialog`: el fondo del escenario y la espina permanecen visibles.
- Estados: consulta vacía (recientes + apps), con resultados, sin resultados y error de carga. El estado vacío debe explicar qué probar y ofrecer una acción.

### Apps

- Título y conteo ("128 instaladas · 12 recientes").
- Filtros: Todas, Favoritas, Sistema, Recientes.
- Orden A–Z y paginación.
- Rejilla 9 × 4 con claves estables por `packageName`; foco recordado.
- Acción mantenida (OK sostenido) para opciones de app: se resolverá en una fase posterior, dentro del mismo escenario.
- "Ajustes TV" es la app del sistema; "Ajustes" de la espina es el panel del launcher. Mantener esa diferencia de nombre.

### Ajustes

Fase 2. La propuesta deja el engranaje en la espina, pero conserva el `LauncherSettingsDialog` actual durante la primera fase para no mezclar la migración de navegación con el rediseño de Ajustes. Después puede convertirse en una cuarta sección del escenario.

## Navegación y foco

Modelo propuesto:

```kotlin
sealed interface LauncherSection {
    data object Home : LauncherSection
    data object Search : LauncherSection
    data object Apps : LauncherSection
}
```

- `LauncherSection` se guarda con `rememberSaveable`.
- Arranque en frío: Inicio. Si se restaura el proceso, se puede recuperar la última sección; decisión pendiente.
- Espina enfocada: arriba/abajo mueve entre secciones; OK activa; derecha o abajo entra al escenario.
- Escenario: al llegar al borde izquierdo de la primera columna, izquierda devuelve el foco a la espina.
- Inicio → Buscar/Apps: la espina mantiene el foco visual y el escenario hace la transición.
- Buscar: Back oculta primero el teclado; si ya está oculto, vuelve a Inicio.
- Apps: Back vuelve a Inicio.
- Inicio: Back no cierra el launcher.
- Cada sección conserva scroll, filtro, consulta y último foco con `SaveableStateHolder` y `FocusRequester`.
- No se recrea el `HomeViewModel` al cambiar de sección.
- Cambiar de perfil no cambia de sección: el escenario sigue en Inicio, Buscar o Apps y solo se actualizan tokens, arte y composición de Inicio.
- La bandeja de perfiles se abre dentro del escenario y se cierra con Back o al elegir un perfil; no es un `Dialog`.

## Movimiento

- Indicador de la espina: 320 ms, curva `cubic-bezier(.2,.8,.2,1)`.
- Contenido saliente: fundido 160 ms + desplazamiento −22 px.
- Contenido entrante: fundido 200 ms + desplazamiento desde +22 px.
- Teclado: 240 ms desde el borde inferior.
- Foco: 160–180 ms; escala máxima 1.06.
- No se escala el escenario completo ni se anima el fondo: en TV eso produce sensación de mareo.
- Respetar `MotionDurationScale` y la escala de animación del sistema; con movimiento reducido, cambiar sin desplazamiento.

## Correspondencia con el código

| Propuesta | Implementación prevista |
|---|---|
| Espina persistente | Nuevo composable de shell en `:app`; no un módulo nuevo |
| Sección activa | Reemplazar `showApps`/`showSearch` por `LauncherSection` |
| Escenario | `Box`/`AnimatedContent` con límites fijos; `HomeContent` dentro |
| Apps sin pantalla nueva | Extraer el contenido de `AppDrawerScreen` a `AppsStage`; quitar `Surface` y botón "‹" |
| Buscar sin diálogo | Extraer `AppSearchDialog`/`SearchBaseLayout` a `SearchStage`; quitar `Dialog` |
| Acciones de sección | `LocalOpenApps`/`LocalOpenSearch` deben navegar a la sección, no abrir overlays |
| Foco por sección | `SaveableStateHolder` + `FocusRequester`; recordar último ítem |
| Ajustes | Mantener `LauncherSettingsDialog` en fase 1; integrarlo en fase 2 |

Restricciones:

- No crear dependencias entre `:feature:home`, `:feature:search` y `:feature:apps` (no existe este último). La composición queda en `:app`.
- No mover la lógica de búsqueda ni el ViewModel a `:app`; solo se adapta la presentación.
- Los layouts de tema conservan su identidad. La espina sustituye únicamente las acciones duplicadas de sección (Buscar, Apps, Ajustes) cuando se aborde cada layout.

## Fases de implementación

1. **Shell y estado**
   - `LauncherSection`, espina, línea de sistema, escenario con límites fijos.
   - Animación de contenido y del indicador.
   - Back y foco básico.

2. **Apps y Buscar dentro del escenario**
   - Extraer `AppsStage` y `SearchStage`.
   - Eliminar `Dialog` y `Surface` de pantalla completa.
   - Conservar filtros, consulta, teclado y scroll.

3. **Foco y estado por sección**
   - `SaveableStateHolder`, último foco, restauración tras proceso.
   - Pruebas de D-pad y Back.
   - Ajustar los layouts de tema para no duplicar acciones.

4. **Ajustes y acciones de app**
   - Decidir si Ajustes es sección o panel.
   - Long-press de apps y hoja de acciones dentro del escenario.

## Criterios de aceptación

- AC1: la espina y la línea de sistema permanecen visibles y en la misma posición en Inicio, Buscar y Apps.
- AC2: cambiar de sección no crea una `Activity`, un `Dialog` ni una `Surface` a pantalla completa.
- AC3: solo un contenido de sección está activo a la vez; no hay superposición de vistas.
- AC4: al volver a una sección se conservan scroll, filtro, consulta, teclado y último foco.
- AC5: Back en Buscar oculta el teclado antes de volver a Inicio; Back en Apps vuelve a Inicio; Back en Inicio no cierra el launcher.
- AC6: el teclado de búsqueda se dibuja dentro del escenario y no bloquea la espina.
- AC7: la rejilla usa `packageName` como clave y no pierde el foco al cambiar de filtro.
- AC8: con movimiento reducido no hay desplazamientos ni animaciones de escala.
- AC9: los layouts de tema siguen renderizando su contenido sin regresiones visuales.
- AC10: una prueba de UI verifica que al pulsar Inicio/Buscar/Apps solo hay un contenido activo y que el foco vuelve donde corresponde.
- AC11: cambiar entre los ocho perfiles actualiza acento, fondo, radio y composición de Inicio sin mover la espina ni cambiar de sección.
- AC12: Roku conserva su grilla 3 × 3; Free My TV su panel de favoritos; el resto conserva su hero, filas y tabs de contenido según el layout real.
- AC13: ningún perfil queda con texto de bajo contraste por usar su acento como color de texto sobre fondos claros; el foco de Roku usa el tono oscuro del perfil.

## Pruebas

- Compose UI: cambio de sección, ausencia de `Dialog`, Back, foco inicial y restauración.
- Compose UI: teclado visible/oculto, consulta sin resultados, filtros de Apps.
- Preview/screenshot: Inicio de los ocho perfiles, Buscar con resultados, Buscar vacío, Apps, Apps filtradas, foco y error.
- Preview/screenshot: cambio de perfil en Inicio, Buscar y Apps; la espina y el escenario no se mueven.
- Instrumentada en TV: D-pad completo, teclado, foco al borde izquierdo, proceso recreado.
- Manual: contraste de acentos claros (Roku, Google TV, Android TV) sobre fondos oscuros y de texto oscuro sobre botones claros.
- Rendimiento: cambio de sección sin recomponer todo el árbol; medir frames y evitar `Column` con listas largas.
- Manual: los ocho perfiles visuales dentro del escenario, 1280×720 y 960×540.

## Riesgos y decisiones abiertas

- **Layouts de tema:** algunos tienen barra inferior, rail propio o fondo a pantalla completa. La espina puede chocar visualmente. Hay que adaptar cada layout o aceptar una capa superior. La migración más delicada es Roku (grilla con columna propia) y Apple TV (panel lateral).
- **Acento claro de Roku:** usar blanco como acento exige inversión de texto en botones y un foco oscuro; no reutilizar el mismo tratamiento que los acentos de color.
- **Conmutador de perfil:** una píldora en la línea de sistema es descubrible, pero añade una acción nueva al shell. Si se prefiere, puede abrir el selector de temas existente en lugar de una bandeja propia.
- **Ocho variantes:** el prototipo demuestra la integración, pero la implementación debe montar los layouts reales dentro del escenario, no reescribir ocho Homes desde cero.
- **Teclado:** el diseño propone QWERTY; el código actual usa una rejilla A–Z de 13 columnas. QWERTY es más familiar, pero requiere probar el recorrido D-pad y el tamaño de teclas.
- **Ajustes:** mantenerlo como panel en fase 1 evita mezclar dos migraciones; a largo plazo debería ser una sección.
- **Persistencia de sección:** Inicio en arranque en frío; restaurar la última sección solo tras recreación de proceso es la opción recomendada.
- **Etiquetas de la espina:** siempre visibles mejora descubribilidad; si se ocultan, el usuario depende de iconos.
- **Ancho:** 104 px reduce el escenario; validar en 1280×720 antes de fijar la medida.

## Artefactos de diseño

Los archivos viven en `docs/design/` (carpeta ignorada por git en este repositorio):

- `docs/design/section-shell.html`: prototipo navegable. `1`, `2`, `3` cambian de sección; `T` cambia de perfil; las flechas también cambian de sección y `Escape` vuelve a Inicio.
- `docs/design/section-shell-home.png`: Inicio webOS a 1920 × 1080.
- `docs/design/section-shell-search.png`: Buscar webOS con teclado dentro del escenario.
- `docs/design/section-shell-apps.png`: Apps webOS.
- `docs/design/section-shell-themes-board.png`: tablero con los ocho perfiles y la integración del shell.
- `docs/design/section-shell-theme-tray.png`: bandeja de perfiles abierta dentro del escenario.
- `docs/design/theme-webos-home.png`
- `docs/design/theme-firetv-home.png`
- `docs/design/theme-roku-home.png`
- `docs/design/theme-tizen-home.png`
- `docs/design/theme-androidtv-home.png`
- `docs/design/theme-googletv-home.png`
- `docs/design/theme-appletv-home.png`
- `docs/design/theme-freemytv-home.png`
- `docs/design/theme-roku-search.png`: ejemplo de Buscar con el acento blanco de Roku.
- `docs/design/theme-appletv-apps.png`: ejemplo de Apps con el acento ámbar de Apple TV.

Los nombres e iconos de apps del prototipo son datos de muestra; en la app deben venir de `InstalledAppsRepository` y de los paquetes instalados.

Este documento está incluido mediante una excepción de `.gitignore`. Las capturas y el prototipo `section-shell.html` siguen siendo referencias locales ignoradas; versionarlos requiere ampliar esas excepciones.
