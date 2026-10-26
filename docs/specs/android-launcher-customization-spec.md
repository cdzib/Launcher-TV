# Personalización del launcher Android

Estado: IMPLEMENTED (pendiente de verificación visual)

## Objetivo
Permitir seleccionar perfiles visuales Android TV y Google TV y personalizar el aspecto
del launcher con opciones locales persistidas, sin prometer instalación en webOS o Tizen.

## Contexto y comportamiento actual
- La app es un único módulo Android (`:app`) con Jetpack Compose for TV.
- `ThemeId` ofrece WEBOS, FIRE_TV, ROKU y TIZEN; `LauncherTheme.toStyle()` determina paleta,
  layout, radio y foco.
- `DataStoreThemeRepository` persiste solo el tema. `HomeScreen` no tiene pantalla de ajustes;
  `ThemeChip` recorre los temas en ciclo.
- Los nombres webOS/Tizen son perfiles visuales Android, no compatibilidad con sus sistemas.

## Alcance
- Agregar perfiles Android TV y Google TV sin eliminar perfiles existentes.
- Añadir controles de personalización accesibles con D-pad: color de acento, fondo mediante
  presets integrados y forma de tarjeta (redondeada/cuadrada).
- Persistir perfil y opciones localmente con DataStore; valores desconocidos/corruptos usan
  defaults seguros.
- Incorporar un punto de entrada de Ajustes desde la pantalla Home y conservar foco/navegación
  básica con control remoto.
- Incluir todos los temas y su preview dentro de Ajustes del launcher; no mantener un acceso
  independiente para cambiar tema.
- Diferenciar las composiciones visuales por perfil manteniendo una pantalla minimalista.
- Documentar que los perfiles de otras plataformas son solo estilos visuales.

## Fuera de alcance
- Instalar launcher en webOS/Tizen o integrar sus SDKs.
- Fondos personalizados desde archivos/URLs, widgets de terceros o backup/restore.
- Integración Shizuku/ADB y cambios del sistema; se planificará en fase separada con acciones
  reversibles, consentimiento explícito y recuperación.
- Desactivar paquetes del sistema o sustituir automáticamente el launcher OEM.

## Reglas de negocio
- RB1: cada perfil define layout y estilo base; preferencias de acento, fondo y forma se aplican
  como overrides sin cambiar el perfil seleccionado.
- RB2: el usuario puede recorrer/seleccionar perfiles y cada cambio se refleja sin reiniciar.
- RB3: personalizaciones permanecen tras reiniciar la app.
- RB4: valores persistidos no reconocidos se sustituyen por defaults y no bloquean Home.
- RB5: las opciones se presentan como controles seleccionables con foco visible para TV.

## Estado y eventos de UI
- Home conserva estados Loading, Empty, Error y Success existentes.
- Ajustes del launcher contiene selección de tema, preview dinámico y controles de acento, fondo
  y forma de tarjeta. Los cambios se emiten como intents y se persisten al seleccionarse.
- No se requieren eventos Android privilegiados en esta entrega.

## Persistencia
Ampliar las preferencias DataStore existentes. Sin esquema Room ni migración SQL.

## Seguridad y privacidad
Sin red ni datos personales. No se solicitan permisos privilegiados.

## Criterios de aceptación
- AC1: aparecen perfiles Android TV y Google TV junto con los perfiles preexistentes.
- AC2: Ajustes del launcher muestra los perfiles visuales, un preview del perfil seleccionado y
  controles de acento, fondo preset y forma de tarjeta, operables con el control remoto.
- AC3: el cambio actualiza visualmente Home en la sesión actual.
- AC4: al recrear el ViewModel/reiniciar, el perfil y overrides elegidos se restauran.
- AC5: preferencias ausentes o inválidas usan defaults sin crash.
- AC6: build Debug y pruebas unitarias relevantes pasan.

## Casos límite
- Preferencias guardadas con valores de enum eliminados/renombrados.
- Cambio repetido rápido de preferencias.
- Sin apps instaladas o error al cargar apps mientras se conserva el perfil.
- Foco remoto tras abrir/cerrar el panel de ajustes.

## Plan de testing
- Unit tests del contrato de preferencias/mapping y persistencia usando la frontera disponible.
- ViewModel tests para selección y restauración de preferencias.
- Build Debug para verificar Compose, recursos y wiring.
- Validación manual con mando en un dispositivo/emulador Android TV para navegación y foco.

## Trazabilidad
| Requisito | Criterio | Componente previsto | Nivel | Estado |
|---|---|---|---|---|
| RB1 | AC2, AC3 | Modelo de estilo/personalización | unit + Compose | implemented |
| RB2 | AC1, AC2 | Home/settings presentation | ViewModel/UI | implemented |
| RB3 | AC4 | ThemeRepository DataStore | unit/integration | implemented, execution pending |
| RB4 | AC5 | Lectura tolerante de preferencias | unit | implemented, execution pending |
| RB5 | AC2 | Controles TV en panel Ajustes | manual/Compose | implemented, manual pending |

## Riesgos, conflictos y TBD
- TBD no bloqueante: lista definitiva de presets de fondo/acento; se usarán opciones integradas
  nombradas y acotadas, evitando selector arbitrario de color/archivo en este MVP.
- Los perfiles webOS/Tizen no implican compatibilidad nativa con esos sistemas.
- Se añadieron perfiles visuales Android TV y Google TV. El panel de ajustes permite elegir
  perfil, acento, fondo y forma de tarjeta; persistencia implementada en DataStore. Build, tests
  y navegación con mando pendientes de verificación.
- Fire TV y Roku se simplificaron, Android TV ahora usa cuadrícula y Google TV una portada
  horizontal. El selector y preview de temas quedaron dentro de Ajustes del launcher. La
  verificación visual con mando sigue pendiente.

## Definition of Done
AC1–AC6 satisfechos, pruebas ejecutadas, build Debug validado y navegación TV manual revisada;
la documentación no afirma soporte de instalación webOS/Tizen.
