# Propuesta de diseño · Hub de Ajustes del launcher

> Estado: propuesta de diseño, sin implementación.
> Objetivo: dar acceso a los ajustes del sistema y del proyector desde el launcher,
> reutilizando las pantallas de `com.android.tv.settings` por `Intent` y aportando
> valor propio donde no se necesitan permisos privilegiados.
> Mockups: `docs/design/settings-hub/`

---

## 1. Idea

Un **hub de Ajustes** dentro del launcher, con la misma identidad visual de
Personalización, que:

1. Muestra el estado real del equipo (red, almacenamiento, Shizuku, proyector).
2. Agrupa accesos a lo que ya existe, sin duplicar pantallas ni pedir permisos.
3. Implementa de forma nativa lo que sí puede hacer una app normal
   (apariencia, clima, accesos, animaciones vía Shizuku, apps preinstaladas).
4. Abre el panel exacto del sistema con un `Intent`, no la portada de Ajustes.
5. Declara siempre **de dónde viene cada ajuste**: PROPIO, SISTEMA, SHIZUKU u OEM.

No sustituye a `com.android.tv.settings`: lo **orquesta**. Es la única ruta
viable sin firma de plataforma (ver análisis previo).

---

## 2. Principios de diseño

| Principio | Decisión |
|---|---|
| TV first (10-foot UI) | Todo legible a 3 m, foco visible, navegación solo con D-pad |
| Transparencia | Cada fila lleva un badge de origen; el usuario sabe qué abre una app externa |
| Fail-soft | Si un `Intent` no resuelve, la fila se oculta o se deshabilita con motivo; nunca crash |
| Sin privilegios ocultos | Nada que requiera `signature|privileged` se simula; se delega o se marca SHIZUKU |
| Una sola capa | El hub es un overlay del launcher, no una app aparte ni otra actividad en Recents |
| Consistencia | Reutiliza `LauncherPalette`, radios, tipografía y foco del launcher |

---

## 3. Mapa de navegación

```text
Launcher (Home)
└── Ajustes · overlay a pantalla completa
    ├── Inicio            ← resumen + accesos rápidos
    ├── Pantalla y sonido ← SISTEMA + PROPIO
    ├── Red               ← SISTEMA
    ├── Dispositivos      ← SISTEMA (Bluetooth / mandos)
    ├── Aplicaciones      ← SISTEMA + SHIZUKU
    ├── Sistema           ← SISTEMA (idioma, fecha, almacenamiento, reset)
    ├── Proyector         ← OEM (si existe) + SHIZUKU
    └── Avanzado          ← SHIZUKU + ADB + diagnóstico
```

Dos niveles, no tres:

- **Nivel 1:** rail de categorías (izquierda) + contenido (centro) + panel de detalle (derecha).
- **Nivel 2:** `Intent` a la pantalla del sistema o acción Shizuku, y se vuelve con Atrás.

El panel derecho no es una pantalla nueva: cambia con el foco de la fila.
Así se evita el patrón “entrar, entrar, atrás, atrás” que en TV se siente lento.

### Comportamiento de Atrás

| Contexto | Atrás hace |
|---|---|
| Foco en contenido | Vuelve al ítem del rail |
| Foco en rail, categoría ≠ Inicio | Vuelve a Inicio |
| Foco en rail, Inicio | Cierra el hub y vuelve al launcher |
| Tras abrir un panel del sistema | Android vuelve al hub (la tarea se conserva) |

El foco se recuerda por categoría al volver.

---

## 4. Lenguaje visual

### 4.1 Lienzo y rejilla (1920 × 1080)

| Elemento | Valor |
|---|---|
| Margen exterior | 64 px (4×16) |
| Cabecera | 120 px de alto |
| Rail | 300 px de ancho, radio 28 |
| Separación rail–contenido | 36 px |
| Contenido | 1456 px (2 columnas de 716 + 24 de gap) |
| Chips de estado | 48 px de alto, radio 24 |
| Tarjeta grande | 716 × 168, radio 24, gap 24 |
| Tarjeta pequeña | 468 × 160, radio 24, gap 24 |
| Fila de ajuste | 880 × 76, radio 18, gap 8 |
| Panel de detalle | 536 × 700, radio 28 |
| Botón primario | 56 px de alto, radio 28 |

Todo el espaciado es múltiplo de 4; los bloques principales usan múltiplos de 8.

### 4.2 Tipografía (sp)

| Rol | Tamaño | Peso | Uso |
|---|---|---|---|
| Display | 46 | Bold | “Ajustes” |
| Subtítulo | 19 | Regular | Ruta / descripción |
| Sección | 13 | SemiBold, tracking 1.6, mayúsculas | “DEL SISTEMA” |
| Tarjeta | 22 | SemiBold | “Pantalla y sonido” |
| Fila | 18 | SemiBold | “Modo de imagen” |
| Cuerpo | 15–16 | Regular | Descripciones |
| Caption | 13–14 | Regular | Estado, ayudas |
| Badge | 12 | SemiBold | PROPIO / SISTEMA / SHIZUKU / OEM |

### 4.3 Color

Se toma de `LauncherPalette` (`core/ui/theme/LauncherTheme.kt`):

- `background` del perfil activo (mockups: `#0A0910` → `#171122`)
- `surface` para tarjetas (`#211A2B`)
- `surfaceHi` para foco
- `accent` para foco, acciones propias y botón primario
- `onBackground` / `onMuted` para texto

Colores semánticos solo para los badges y estados:

| Origen | Color | Significado |
|---|---|---|
| PROPIO | `#37D9C5` (accent) | Lo implementa el launcher |
| SISTEMA | `#8AB4F8` | Abre un panel de `com.android.tv.settings` |
| SHIZUKU | `#BB86FC` | Requiere autorización de Shizuku |
| OEM | `#FFB74D` | Abre una app del fabricante |
| Éxito / conectado | `#7ED9A0` | Punto verde de estado |
| Error / no disponible | `#FF8A80` | Estado degradado |

Nunca se usa solo el color para comunicar el origen: siempre hay texto en el badge.

### 4.4 Foco y movimiento

| Propiedad | Valor |
|---|---|
| Anillo de foco | 3 px de `accent`, radio del componente |
| Escala en tarjetas | 1.02 (no en filas de texto) |
| Transición de foco | 150–180 ms, `decelerate` |
| Cambio de categoría | 220 ms, `decelerate` |
| Apertura del hub | 250 ms, `decelerate`; cierre 200 ms `accelerate` |
| Scroll | desplazamiento por página, sin scroll continuo en el rail |

El foco inicial al abrir es **Inicio**; al volver de una acción es el mismo ítem.

---

## 5. Las cuatro capas de origen

Cada entrada del catálogo declara una acción y un origen:

| Origen | Acción | Resolución |
|---|---|---|
| PROPIO | Destino interno del launcher | Siempre disponible |
| SISTEMA | `Intent` (`android.settings.*`) con componente de respaldo | `PackageManager.resolveActivity`; si falla, `ACTION_SETTINGS`; si también falla, se oculta |
| SHIZUKU | Operación de `ShizukuDeviceGateway` | Estado: no instalado / no iniciado / sin permiso / listo |
| OEM | Lista de `Intent` candidatos del fabricante | Se muestra solo si alguno resuelve; si no, la sección desaparece |

### Catálogo de intents reutilizables (verificado en el APK de referencia)

| Acción | Pantalla |
|---|---|
| `android.settings.SETTINGS` | Portada de Ajustes (fallback universal) |
| `android.settings.WIFI_SETTINGS` | Red |
| `android.settings.panel.action.WIFI` | Panel rápido de Wi-Fi |
| `android.settings.BLUETOOTH_SETTINGS` | Bluetooth |
| `android.settings.SOUND_SETTINGS` | Sonido |
| `android.settings.APPLICATION_SETTINGS` | Apps |
| `android.settings.APPLICATION_DETAILS_SETTINGS` | Detalle de una app (requiere `package:` URI) |
| `android.settings.DEVICE_INFO_SETTINGS` | Acerca de |
| `android.settings.DATE_SETTINGS` | Fecha y hora |
| `android.settings.LOCALE_SETTINGS` | Idioma |
| `android.settings.INPUT_METHOD_SETTINGS` | Teclado |
| `android.settings.ACCESSIBILITY_SETTINGS` | Accesibilidad |
| `android.settings.SECURITY_SETTINGS` | Seguridad |
| `android.settings.INTERNAL_STORAGE_SETTINGS` | Almacenamiento |
| `android.settings.HOME_SETTINGS` | App de inicio (puede no existir en TV) |
| `android.settings.APPLICATION_DEVELOPMENT_SETTINGS` | Opciones de desarrollo |

En el T6 Max (Android 9) hay que validar cuáles resuelven; el catálogo se construye
en tiempo de ejecución y las que no resuelven no se muestran.

### Acciones SHIZUKU del hub

- Aplicar/restaurar escalas de animación 0.5×.
- Quitar/restaurar apps preinstaladas para el usuario actual.
- (Futuro) cambiar densidad o tamaño de pantalla con `wm` si se valida.

Siempre con confirmación explícita y snapshot previo, como ya hace
`DeviceToolsDialog`.

---

## 6. Pantallas

### 6.1 Inicio (mockup `hub-overview.png`)

- **Cabecera:** “Ajustes”, subtítulo, chip del dispositivo y “Cerrar”.
- **Chips de estado:** Wi-Fi, almacenamiento libre, Shizuku, modo proyector.
- **Del sistema:** Red, Pantalla y sonido, Aplicaciones, Sistema (4 tarjetas 2×2).
- **Del launcher:** Apariencia, Clima, Accesos (3 tarjetas).
- **Pie:** estado de Shizuku y ayuda de teclas.

El chip “Modo proyector” solo aparece si se detecta una app OEM de proyector.

### 6.2 Categoría con panel de detalle (mockup `hub-display.png`)

- Lista de filas agrupadas: “Este dispositivo” (SISTEMA) y “Del launcher” (PROPIO).
- Al enfocar una fila, el panel derecho muestra: icono, título, badge,
  explicación, valor actual y acciones.
- Botón primario contextual: “Abrir panel del sistema” o “Cambiar aquí”.
- Si el panel del sistema no existe: badge “NO DISPONIBLE”, texto del motivo y
  botón “Usar Ajustes generales”.

### 6.3 Proyector + estado de Shizuku (mockup `hub-projector.png`)

- “Del fabricante” (OEM): corrección trapezoidal, enfoque, zoom.
  Nota: “Abren la app del fabricante. Si no existe, el acceso se oculta.”
- “Avanzado · Shizuku”: animaciones, quitar apps preinstaladas, restaurar.
- Panel derecho de estado cuando Shizuku no está listo, con CTA “Conectar Shizuku”
  y “Ver instrucciones ADB”.

Esta pantalla comunica la diferencia entre **no soportado** (OEM ausente) y
**no autorizado** (Shizuku), que es la principal fuente de confusión.

### 6.4 Avanzado / diagnóstico

Filas informativas, sin privilegios:

- Versión de Android, parche, ABI, modelo.
- Almacenamiento y memoria.
- Estado de Shizuku y versión.
- “Copiar instrucciones ADB”.
- “Abrir opciones de desarrollo” (SISTEMA).

---

## 7. Estados

| Estado | Representación |
|---|---|
| Cargando | Skeleton con la forma real de tarjetas/filas, shimmer 1200 ms, sin spinner de pantalla completa |
| Vacío | “No hay ajustes disponibles en este equipo” + acción “Abrir Ajustes del sistema” |
| Shizuku no instalado | Fila deshabilitada + CTA “Instalar Shizuku” (abre la web o el APK) |
| Shizuku sin permiso | Badge “SIN PERMISO” + “Autorizar” |
| OEM ausente | La sección no se renderiza |
| Acción fallida | Snackbar/toast con motivo y opción “Reintentar” |
| Sin red | El chip muestra “Sin conexión”; Red sigue abriendo el panel del sistema |

Nada bloquea la navegación: el hub siempre se puede cerrar con Atrás.

---

## 8. Arquitectura propuesta (Clean Architecture + MVI)

### Modelo (`:core:model`)

```kotlin
enum class SettingsOrigin { LAUNCHER, SYSTEM, SHIZUKU, OEM }

sealed interface SettingsAction {
    data class OpenLauncher(@StringRes val destination: Int) : SettingsAction
    data class OpenSystem(val intents: List<Intent>) : SettingsAction
    data class RunShizuku(val operation: DeviceOperation) : SettingsAction
    data class OpenOem(val intents: List<Intent>) : SettingsAction
}

data class SettingsEntry(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int?,
    val icon: SettingsIcon,
    val origin: SettingsOrigin,
    val action: SettingsAction,
    val status: SettingsStatus = SettingsStatus.Unknown
)

sealed interface SettingsStatus {
    data object Ready : SettingsStatus
    data class Unavailable(@StringRes val reasonRes: Int) : SettingsStatus
    data class NeedsAuthorization(val provider: String) : SettingsStatus
}
```

`Intent` no debería entrar en `:core:model` puro (JVM). Alternativa: `OpenSystem(action: String, dataUri: String?, packageName: String?)`
y construir el `Intent` en la capa Android. Se elige esta última para mantener el módulo puro.

### Catálogo (`:core:apps` o nuevo `:core:system`)

`SystemSettingsCatalog` construye el árbol de categorías y resuelve cada acción:

```kotlin
interface SettingsCatalog {
    fun categories(): List<SettingsCategory>
    fun resolve(entry: SettingsEntry): SettingsStatus
}
```

Responsabilidades:

- `PackageManager.resolveActivity` para SISTEMA/OEM.
- Lectura de estado: `ConnectivityManager`, `StatFs`, `Build`, `Shizuku.pingBinder`.
- Detección OEM por lista de acciones candidatas y paquetes conocidos.

### UI (`:feature:settings`)

- `SettingsHubDialog` (overlay) con rail + contenido + panel.
- `SettingsHubViewModel` (Hilt) con `StateFlow<SettingsHubUiState>` e `Intent`s:
  `SelectCategory`, `FocusEntry`, `ActivateEntry`, `Dismiss`, `Retry`.
- Effects: `OpenIntent`, `ShowMessage`, `LaunchShizukuAuth`.
- Reutiliza `LauncherSettingsDialog` como destino “Apariencia” y
  `DeviceToolsDialog` como motor de operaciones Shizuku.

### Host (`:app`)

- `LauncherScreen` abre el hub desde el engranaje y desde “Personalizar inicio”.
- Traduce `SettingsEffect.OpenIntent` a `startActivity` con `FLAG_ACTIVITY_NEW_TASK`
  cuando el contexto no sea Activity.
- Registra `ActivityResult` de autorización de Shizuku.

### Reglas de dependencia

```text
app ──> feature:settings ──> core:model / core:ui
 │                └────────> core:apps (catálogo, Shizuku)
 └──> core:apps
```

Nada de `Intent` en `core:model`; nada de `PackageManager` en la UI.

---

## 9. Accesibilidad

- Contraste mínimo AA (4.5:1) para texto; los badges usan texto + color.
- Anillo de foco de 3 px siempre visible, también sobre fondos con imagen.
- Área de foco mínima 48 × 48 px; en TV el foco debe verse a 3 m.
- `contentDescription` en iconos y badges; las filas anuncian título, estado y origen.
- No se depende del color para distinguir PROPIO/SISTEMA/SHIZUKU/OEM.
- Textos en `strings.xml` (es/en) y sin concatenaciones para permitir traducción.

---

## 10. Fases

| Fase | Alcance | Estimación |
|---|---|---|
| P0 | Shell del hub, rail, Inicio, catálogo SISTEMA + resolver, estados, navegación | 2–3 días |
| P1 | Integración SHIZUKU (animaciones, apps preinstaladas, autorización) | 1–2 días |
| P2 | Descubrimiento OEM en el T6 Max con REA + sección Proyector | 1–2 días |
| P3 | Accesibilidad, pruebas de navegación/foco, pulido visual | 1–2 días |

P2 depende de obtener el `TvSettings.apk` real del T6 Max y de localizar la app
del fabricante para keystone/enfoque/zoom.

---

## 11. No objetivos

- Reemplazar `com.android.tv.settings` o convertirse en el handler de `ACTION_SETTINGS`.
- Conectar Wi-Fi o emparejar Bluetooth por cuenta propia (requiere privilegios).
- Mostrar u ocultar ajustes que el sistema no exponga.
- Duplicar pantallas del sistema: se abren, no se reimplementan.

---

## 12. Criterios de aceptación

1. Cualquier fila está en uno de estos estados: funciona, deshabilitada con motivo,
   o no se muestra.
2. Si `com.android.tv.settings` no existe, el hub abre y muestra solo lo PROPIO/SHIZUKU.
3. Atrás sigue el orden definido en §3 sin salir del launcher por accidente.
4. El foco siempre es visible y no se pierde al cambiar de categoría.
5. Primera pintura < 300 ms; el estado del sistema se carga en segundo plano.
6. Sin permisos nuevos en el manifiesto para las funciones SISTEMA.
7. Pruebas: catálogo (resolución de intents con `PackageManager` falso),
   ViewModel (foco/categoría/activación), y UI de foco con Compose.

---

## 13. Relación con el análisis del APK

- El APK analizado (`TvSettings.apk`, API 36) es de referencia, no del T6 Max.
- Demuestra que las pantallas son reutilizables por 71 acciones de sistema.
- No contiene ajustes de proyector: esos viven en una app OEM.
- Por eso el hub **delega** el sistema y **descubre** el OEM; no clona nada.

Los mockups de este documento usan datos de ejemplo; los estados reales se
resuelven en el dispositivo.

---

## Anexo · Prototipo interactivo

Abre **`SETTINGS_HUB_PROTOTYPE.html`** en un navegador (Safari/Chrome, sin servidor).

Controles:

- **↑ ↓ ← →** mueve el foco; **Enter** abre; **Esc / Retroceso** vuelve.
- La barra superior permite cambiar de vista (Inicio / Pantalla / Proyector),
  simular los estados de **Shizuku** (conectado, sin autorizar, no instalado),
  ocultar la **app OEM**, mostrar las **especificaciones** y cambiar el **acento**.
- Las filas SISTEMA abren un panel simulado de `com.android.tv.settings`.
- Las filas OEM abren la app del fabricante simulada.
- Las filas SHIZUKU piden autorización y ejecutan la operación con confirmación.

Capturas del prototipo: `docs/design/settings-hub/prototype/`.

## Anexo · Mockups

### Inicio

![Inicio](docs/design/settings-hub/hub-overview.png)

### Pantalla y sonido (lista + panel de detalle)

![Pantalla y sonido](docs/design/settings-hub/hub-display.png)

### Proyector + estado de Shizuku

![Proyector](docs/design/settings-hub/hub-projector.png)
