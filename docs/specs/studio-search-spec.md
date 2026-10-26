# Búsqueda Studio

Estado: IMPLEMENTED

## Objetivo y alcance
Integrar `../design/section-shell.html` como búsqueda a pantalla completa en Compose TV.
La búsqueda actual se abre desde `HomeScreen` mediante `AppBrowserDialog`. Ese diálogo
también sirve para asignar accesos de webOS; dicho selector debe conservarse sin cambios.
No se añaden permisos, dependencias, persistencia ni navegación ficticia.

## Criterios de aceptación
- AC1: Buscar filtra las apps instaladas por nombre, sin distinguir mayúsculas y recortando espacios externos; una consulta vacía muestra todas, en su orden original.
- AC2: La selección se conserva por paquete mientras siga entre los resultados; de lo contrario se selecciona la primera app. Sin resultados no hay app seleccionada ni acción Abrir.
- AC3: Teclado de letras y números (conmutador 123/ABC), Espacio y Borrar, campo editable y limpiar actualizan los resultados. Foco visible, navegación D-pad, Atrás y botón Inicio cierran la búsqueda.
- AC4: El panel destacado muestra nombre e icono reales. Abrir o pulsar una tarjeta envía el paquete al callback existente. No se muestran datos de muestra en producción.
- AC5: El selector de accesos conserva título, limpiar asignación y comportamiento anterior.
- AC6: Composición grafito, rail lateral funcional, teclado izquierdo, destacado y cuadrícula desplazable a la derecha. Previews con contenido, vacío y nombre largo; recursos localizados.

## Estado y límites
Consulta y selección son estado local de presentación. La lista proviene del estado existente
de Home, incluyendo instalaciones/desinstalaciones. Selección derivada impide abrir resultados
obsoletos. Lista vacía: mensaje orientativo. Volver descarta consulta. Sin red ni analytics.
El diseño usa colores locales específicos de Studio porque los perfiles del launcher pueden
tener paletas claras o intensas incompatibles con la propuesta aprobada.

## Validación y trazabilidad
AC1–AC2: pruebas unitarias de filtrado y selección.
AC3–AC6: compilación, previews y comprobación manual en TV (D-pad, IME, Atrás,
lista larga, sin resultados y selector webOS). No existe infraestructura Compose UI de tests;
no añadir dependencias solo para esta integración visual.

## Fuera de alcance
Micrófono, búsqueda online, categorías inventadas, perfil o ajustes nuevos y cambios de Home.

## Evidencia de implementación
- `AppBrowserDialog` delega en `AppSearchDialog` solo cuando no recibe título ni acción de limpiar acceso; el selector webOS conserva el código anterior.
- `AppSearchScreen` usa los paquetes de Home y su callback de lanzamiento. Iconos mediante `AppIcon`, con fallback visual y previews sin PackageManager.
- Dos ciclos TDD ejecutados: RED por símbolos de filtrado y selección aún inexistentes; GREEN tras implementarlos. Regresiones adicionales: vacío, consulta sin coincidencias, etiquetas duplicadas, renombrado y paquete retirado.
- Gate ejecutado con Gradle 8.9: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` correcto. 52 tests, cero fallos/errores. Lint: cero errores, 117 advertencias en el proyecto, sin incidencias que señalen los archivos AppSearch.
- Prueba manual en `emulator-5554` (TV 3840×2160): entrada desde Buscar de Home, foco inicial en A, desplazamiento a resultados desde el borde del teclado, selección con D-pad, actualización del destacado y apertura real de YouTube (actividad `com.google.android.youtube.tv`).
- APK final reinstalado y comprobado visualmente: teclado alfabético sin recortes, cambio 123/ABC por D-pad, consulta AA con cero resultados y Abrir deshabilitado, Atrás vuelve a Home y devuelve foco al acceso Buscar.
- Previews compilados: contenido, cero apps, cero coincidencias y nombre largo con font scale 1.15.

## Riesgos residuales / gate parcial
Pendiente validación en TV física, IME externo, lista muy extensa, tipografía ampliada en dispositivo y asignación/limpieza de accesos webOS por interacción. El selector se conserva pero no se automatizó su UI. No hay pruebas instrumentadas Compose en el proyecto. No se declara VERIFIED hasta completar esas comprobaciones.
