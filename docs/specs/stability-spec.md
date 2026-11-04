# Estabilización de Launcher-TV

Estado: IMPLEMENTED — quality gates automatizados PASS; validación manual Android TV pendiente.

## Objetivo

Corregir regresiones de navegación y persistencia confirmadas durante la auditoría sin revertir la modularización ni cambiar los formatos persistidos de temas.

## Contexto y alcance

- `:app` compone Home, búsqueda, ajustes y temas personalizados.
- `HomeViewModel` persiste apariencia mediante `SetAppearanceUseCase` y `ThemeRepository`.
- Los layouts personalizados pueden no definir destinos de navegación.
- La búsqueda tiene una acción de Ajustes que actualmente solo cierra el diálogo.
- El renderer `TopBarStyle.ICONS` no dibuja sus pestañas.

Este cambio estabiliza acceso a Ajustes, guardado de apariencia, callbacks de búsqueda, fondos y operaciones de paquetes en UI. No añade módulos ni cambia Gradle, catálogo de dependencias, esquemas persistidos o formatos ZIP/JSON.

## Reglas

- RB1: Ajustes debe poder abrirse desde el host independientemente del layout activo o del estado Home.
- RB2: una escritura de apariencia fallida no debe quedar como apariencia confirmada ni impedir informar el error.
- RB3: cambios consecutivos de apariencia deben basarse en el último valor confirmado y persistirse en orden.
- RB4: la acción Ajustes del diálogo de búsqueda debe abrir Ajustes, no limitarse a cerrar búsqueda.
- RB5: las pestañas `ICONS` deben renderizarse y conservar la acción asociada a su etiqueta.
- RB6: un tema personalizado debe respetar el fondo proporcionado por el host.
- RB7: lectura, validación, compresión e instalación de paquetes e imágenes no deben bloquear el hilo principal.

## Errores y recuperación

- Persistencia de apariencia fallida: conservar el valor anterior y emitir un mensaje localizado.
- Error de importar/exportar tema o imagen: mantener los mensajes existentes y permitir reintento.
- Cancelación de coroutine: propagarla, no mostrarla como error ordinario.

## Criterios de aceptación

- AC1: si falla el guardado de apariencia, Home emite `ShowMessage`, mantiene el valor anterior y los siguientes cambios válidos no incluyen el cambio fallido.
- AC2: Home acepta cambios consecutivos y los escribe serialmente.
- AC3: el botón de Ajustes del host está disponible con layout personalizado y estados Loading, Empty y Error.
- AC4: el callback de Ajustes del buscador invoca el destino provisto por el host.
- AC5: `TopBarStyle.ICONS` representa las pestañas configuradas.
- AC6: importación/validación y decodificación de imágenes se hacen fuera de Main; fallos siguen produciendo UI recuperable.
- AC7: los colores de fondo y acento seleccionados se leen de `LocalLauncherStyle` en los layouts integrados, no solo en búsqueda; un fondo de imagen mantiene transparentes sus raíces.
- AC8: al cambiar de tema se descarta el color sólido específico del tema anterior para que se aplique el fondo de perfil del nuevo tema; un fondo de imagen elegido por el usuario se conserva.

## Testing y Definition of Done

- Regression tests del ViewModel para fallo y secuencia de escrituras.
- Unit tests de la selección/renderizado de pestañas si la lógica se hace pura.
- Compilar Debug y Release, ejecutar pruebas unitarias de todos los módulos, `verifyModuleBoundaries` y `lintDebug`.
- Validación manual pendiente en Android TV: navegación D-pad, apertura/cierre de Ajustes, importación/exportación y layouts personalizados.

## Trazabilidad

| Criterio | Componente | Prueba/validación | Estado |
|---|---|---|---|
| AC1–AC2 | HomeViewModel | HomeViewModelTest, incluido fallo y cambios consecutivos | PASS |
| AC3–AC4 | Host y búsqueda | `:app:compileDebugKotlin` y `:app:assembleDebug`; D-pad pendiente en dispositivo | Código PASS; manual pendiente |
| AC5 | Renderer Compose | `:app:assembleDebug` y `:app:assembleRelease` | PASS |
| AC6 | LauncherScreen | `testDebugUnitTest`, builds Debug/Release y `lintDebug`; validación manual pendiente | Automatizado PASS; manual pendiente |
| AC7 | Layouts webOS, Tizen y referencias | `:core:theme:compileDebugKotlin`; prueba `LauncherAppearance.toStyle` existente | PASS |
| AC8 | HomeViewModel y paleta | `HomeViewModelTest`: rojo previo -> Roku usa morado de perfil | PASS |

## Ejecución final

- `verifyModuleBoundaries`: PASS.
- `testDebugUnitTest`: PASS en todos los módulos con source set de tests (módulos sin tests reportan `NO-SOURCE`).
- `:app:assembleDebug`: PASS.
- `:app:assembleRelease`: PASS.
- `lintDebug`: PASS.
- Advertencia de entorno: Android Gradle Plugin 8.5.2 avisa que fue probado hasta `compileSdk 34`, mientras el proyecto usa `35`; no bloqueó los gates.
