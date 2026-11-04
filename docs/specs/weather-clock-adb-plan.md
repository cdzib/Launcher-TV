# Plan de clima, relojes, widgets y permisos avanzados

Estado: DRAFT — implementación parcial; no verificado en dispositivo
Fecha: 2026-10-25
Base revisada: commit `eba7b01`, rama `feature/theme-generation-engine`

## 1. Objetivo

Incorporar clima real, relojes configurables y acceso opcional a capacidades avanzadas
de Android, respetando los temas actuales, el buscador existente y la navegación TV.
Planificar también los widgets Android externos y las opciones de integración ADB,
sin convertirlos en requisitos para utilizar el launcher.

Este documento es un plan, no autorización para conceder permisos, activar depuración,
conectar dispositivos, contratar servicios ni ejecutar operaciones privilegiadas.

## 2. Evidencia del estado actual

| Área | Evidencia | Consecuencia para el plan |
|---|---|---|
| Plataforma | `app/build.gradle.kts`: minSdk 26, targetSdk/compileSdk 35 | Mantener compatibilidad desde Android 8; comprobar capacidades por dispositivo |
| Arquitectura | `../architecture/architecture.md`, `settings.gradle.kts` | Integración en `:app`; features independientes; contratos JVM en `:core:model` |
| Reloj compartido | `core/ui/.../components/ClockText.kt` | Ya calcula hora local y actualiza al cambio de minuto |
| Reloj del renderer | `core/theme/.../CompositionCanvas.kt`, función privada `ClockText` | Segunda implementación, formato fijo y actualización cada 15 segundos |
| Configuración de reloj | `ThemeComposition.kt`, `TopBarConfig.showClock`; inspector del editor | Existe visibilidad en cabecera, no un bloque configurable de reloj |
| Clima | No hay cliente, repositorio ni preferencias meteorológicas | Crear un corte vertical nuevo, sin datos ficticios |
| Red y persistencia | `core/data`: permiso INTERNET, cliente HTTPS para temas, JSON y DataStore | Reutilizar patrones; no acoplar clima al cliente ni a errores de descarga de temas |
| Widgets externos | No se encontró `AppWidgetHost` ni flujo de vinculación | Requiere soporte nuevo y prueba en los fabricantes objetivo |
| Permisos avanzados | No se encontró implementación ADB/Shizuku ni permisos asociados | Empezar por inventario de capacidades y consentimiento |
| Navegación | `LauncherScreen.kt`, `LocalOpenSearch`, `AppSearchDialog` | Preservar separación Buscar/Apps y acciones actuales de temas |
| ZIP | `ThemeManifestValidator` acepta v1/v2 y rechaza campos/versiones desconocidos | Evolucionar el formato explícitamente, conservando lectores legacy |
| Documentación | `docs/` está ignorada; hay especificaciones versionadas en raíz | Mantener este plan en raíz, sin publicar documentación local existente |

Los documentos históricos pueden describir estados anteriores. Para implementar se
reconfirmarán los símbolos, dependencias y comportamiento actuales; no se restaurarán
pantallas antiguas por seguir una especificación desactualizada.

## 3. Alcance y límites

### Primera entrega propuesta

- Una fuente común de hora, con relojes digital, analógico y digital con fecha.
- Open-Meteo con selección manual de ciudad, unidades y caché privada.
- Bloques declarativos opcionales CLOCK y WEATHER, editor y previews.
- Integración equivalente en layouts nativos sin duplicar controles existentes.
- Panel de capacidades/permisos: estado real, explicación y apertura de ajustes oficiales.
- Asistente de instrucciones ADB desde equipo externo para las operaciones aprobadas.

### Ampliaciones planificadas, con decisiones previas

- Hospedar widgets de otras aplicaciones instaladas.
- Ejecutar operaciones específicas mediante Shizuku autorizado por el usuario.
- Cliente ADB integrado solo si se demuestra que el asistente externo y Shizuku no
  satisfacen el caso de uso. Está incluido como fase de decisión/prototipo, no como
  implementación automáticamente aprobada.

### Fuera de alcance

- Root, desbloquear bootloader, elevar el APK a aplicación de sistema o eludir permisos.
- Conceder permisos arbitrarios o ejecutar una terminal libre desde temas/importaciones.
- Instalar, deshabilitar o eliminar aplicaciones automáticamente.
- Cambiar o deshabilitar automáticamente el launcher del fabricante.
- Ubicación en segundo plano, geolocalización por IP y seguimiento del dispositivo.
- Catálogo multimedia, nuevos diseños por ThemeId y rediseño del buscador.
- Nuevas versiones de Gradle/SDK o migraciones arquitectónicas sin necesidad demostrada.

## 4. Decisiones propuestas y puertas de entrada

Las decisiones confirmadas por el usuario se marcan en la tabla. El plan permanece DRAFT
para las capacidades que aún dependen de identificar operaciones privilegiadas y firmware.

| ID | Decisión propuesta o pendiente | Bloquea |
|---|---|---|
| D1 | Confirmado: uso personal/no comercial de Open-Meteo alojado | Revisar antes de distribuir con uso comercial |
| D2 | Confirmado: ciudad manual; sin permiso de ubicación en la primera entrega | Ninguno |
| D3 | Confirmado: relojes propios y widgets de otras apps instaladas | F6 habilitada para implementación |
| D4 | TBD: definir qué operaciones concretas de sistema debe habilitar el launcher | Cualquier escritura privilegiada o comando Shizuku/ADB |
| D5 | Objetivo confirmado: Android TV y proyectores; marcas/modelos/firmwares TBD | No prometer compatibilidad universal sin matriz de dispositivos |
| D6 | Clima: actualización propuesta de 30 minutos y caducidad de 6 horas | Cierre de criterios de caché |
| D7 | Evolucionar a ZIP v3 para nuevas capacidades; seguir leyendo v1/v2 | Contrato final de F2, antes de modificar codecs |
| D8 | Sin trabajo meteorológico periódico en background en el MVP | Confirmar antes de añadir WorkManager u otro planificador |

Para uso comercial: elegir el servicio contratado o un despliegue propio antes de
publicar. No incrustar una clave comercial secreta en un APK o ZIP; si se necesita
protegerla, definir proxy/backend, coste, operación y autenticación como trabajo adicional.

## 5. Reglas que deben preservarse

- RB1. Inicio mantiene el contenido y la navegación del tema activo; no se altera el
  comportamiento de Apps ni se reemplaza el buscador con otra pantalla.
- RB2. Buscar sigue abriendo `AppSearchDialog`; se conservan teclado, resultados y retorno.
- RB3. Ajustes del sistema y personalización son acciones diferentes. No se agregan iconos
  de perfil, personalización, búsqueda o relojes duplicados por inyección automática.
- RB4. Un tema solo declara apariencia y recursos validados, nunca comandos, permisos,
  código ejecutable, credenciales o endpoints arbitrarios.
- RB5. Los datos del clima provienen del proveedor o de caché identificada como antigua.
  Nunca se usan valores de muestra como fallback de producción.
- RB6. La ubicación, caché, identificadores de widgets y autorizaciones pertenecen al
  dispositivo/usuario. No se exportan con el tema.
- RB7. Clima, reloj y launcher básico funcionan sin ADB ni Shizuku.
- RB8. Denegar/revocar permisos, perder conectividad o no soportar widgets no bloquea Home.
- RB9. No hay red, lectura de permisos ni operaciones privilegiadas desde el renderer.
- RB10. Toda escritura avanzada requiere capacidad disponible, consentimiento específico,
  operación permitida y comprobación posterior del resultado.

## 6. Diseño técnico previsto

### Distribución de responsabilidades

| Módulo | Trabajo previsto |
|---|---|
| `:core:model` | Modelos/contratos de clima, configuración de reloj y capacidades; extensión del modelo declarativo sin Android/Compose |
| `:core:data` | API meteorológica/geocodificación, mapeo de respuestas, caché y preferencias; lector/escritor v3 compatible con archivos legacy |
| `:core:ui` | Componentes de reloj/clima y estados renderizables reutilizables, recursos, accesibilidad y previews |
| `:core:theme` | Renderizar los bloques a partir de datos recibidos; política anti-duplicados; adaptación mínima de layouts nativos |
| `:feature:home` | Coordinar datos/estado de widgets internos, visibilidad y actualización sin convertir un error meteorológico en error global de Home |
| `:feature:settings` | Configuración de reloj/clima, capacidades y guías; conservar accesos de personalización existentes |
| `:feature:editor` | Agregar/editar bloques, validaciones y previews sin red ni permisos reales |
| `:app` | Composición Hilt, contratos entre features, Activity Results, consentimientos y ciclo de vida |

Adaptadores Android para permisos, widgets externos y Shizuku quedarán fuera de Compose
y de `:core:model`. Antes de F4/F6 se elegirá un límite concreto: no añadirlos a
`:core:ui` ni a la lógica de datos de temas. Si su tamaño justifica módulos dedicados,
la propuesta es `:core:device` y `:core:widgets`, consumidos por `:app`; las features
seguirán recibiendo contratos/estado/callbacks. Esta decisión se registra antes de tocar
Gradle y se actualiza `../architecture/architecture.md` y la validación de fronteras si procede.

No se introducen clases base, Room, Retrofit, WorkManager ni un cliente ADB por defecto.
Las dependencias nuevas deben resolver una necesidad concreta y revisar licencia y
compatibilidad con API 26. Los DTO meteorológicos no llegan a Presentation.

### Reloj y ciclo de vida

- Fuente común con tiempo sustituible en pruebas; respetar formato 12/24 h y locale.
- Digital, analógico y digital con fecha; sin segundero por defecto.
- Sincronizar con el minuto y recalcular al volver a foreground o cambiar hora/zona/formato.
- Pausar actualizaciones cuando no se muestran; no pedir alarmas exactas ni servicio
  foreground para dibujar la hora.
- Si existe un bloque CLOCK explícito, no añadir además el reloj automático de cabecera.
  Los temas legacy sin el bloque conservan `showClock` y sus controles actuales.
- No quitar relojes explícitamente diseñados por el autor por una heurística global.

### Clima y datos

- Consulta a hosts HTTPS aprobados de Open-Meteo; ubicación seleccionada en la aplicación.
- Contenido inicial: ciudad, temperatura, condición WMO y día/noche. Mostrar que las
  condiciones son estimaciones del modelo cuando se describa su origen.
- Compartir la solicitud/caché entre bloques, evitar llamadas por recomposición y cancelar
  consultas de ciudades anteriores para no sobrescribir la selección más reciente.
- Preferencias en DataStore; snapshot pequeño y acotado en almacenamiento privado con
  escritura segura. No crear una base de datos por un único resultado meteorológico.
- Clave de caché por ubicación y parámetros; mostrar únicamente datos de la ciudad activa.
- Política propuesta: hasta 30 min, dato fresco; de 30 min a 6 h, dato marcado como antiguo;
  más de 6 h, no presentarlo como temperatura actual y ofrecer reintento.
- Al volver a Inicio: usar caché y renovar solo si vence. Al ocultar/desactivar el clima,
  detener refrescos automáticos. Reintentos acotados con espera y respeto a HTTP 429.
- Separar fecha válida del dato del proveedor y fecha de consulta. Validar tiempos, unidades,
  códigos desconocidos y respuestas incompletas sin mostrar datos engañosos.
- Selección de ciudad, borrado y desactivación explícitos; no exportar coordenadas ni
  registrar ubicación o credenciales en logs.
- Atribución visible y enlace accesible a proveedor/licencia; los previews usan fixtures,
  nunca llamadas reales.

### Contrato de temas y exportación

- Propuesta v3: CLOCK y WEATHER con opciones visuales acotadas. APP_WIDGET se especifica
  aparte en F6, con marcador portable y selección local posterior del proveedor.
- v1/v2 continúan leyéndose sin cambios de claves, UUIDs, ThemeId ni apariencia guardada.
- No etiquetar como v1/v2 un paquete que utiliza bloques nuevos. Definir exportación de la
  versión mínima compatible y mensaje claro ante una versión no soportada.
- No prometer que versiones antiguas del launcher importarán v3: sus lectores son estrictos.
- Validar campos permitidos, rangos, número de bloques y referencias a recursos; ninguna
  importación inicia consultas, solicita permisos ni vincula widgets automáticamente.
- ZIPs excluyen snapshots del clima, ciudad personal, appWidgetId, grants y claves ADB.

## 7. Fases de ejecución y dependencias

### F0 — Baseline y especificaciones de cada entrega

Dependencias: ninguna. Riesgo: medio por regresiones recientes de navegación.

- [ ] Confirmar D1–D8 según la fase que vaya a ejecutarse.
- [ ] Registrar baseline de tests/build y flujo manual de Inicio/Apps/Buscar/Personalización.
- [ ] Documentar comportamiento actual y distinguir deuda previa de errores introducidos.
- [ ] Preparar pruebas de regresión para accesos de búsqueda, perfil y personalización.

Salida: especificación READY del primer corte, sin obligar a resolver todavía las
opciones de Shizuku/widgets externos. Primer corte TDD: actualización del reloj al
cambio de minuto y retorno a foreground con tiempo controlado.

### F1 — Reloj común y variantes propias

Dependencias: F0. Riesgo: bajo/medio.

- [x] Unificar las dos implementaciones sin cambiar la visibilidad de los temas legacy.
- [ ] Implementar estilos digital, analógico y digital con fecha, formato y ciclo de vida.
- [ ] Agregar preferencias y controles dentro de Personalización, sin iconos globales nuevos.
- [ ] Probar cambio de fecha, 12/24 h, zona horaria, suspensión y reanudación.

Avance: reloj digital compartido integrado en la barra declarativa; respeta 12/24 h del
sistema y admite fecha. Hay tests de formato y preview. Estilo analógico, controles de
personalización y validación de ciclo de vida siguen pendientes.

Salida: relojes internos funcionales offline en temas nativos, sin duplicados automáticos.

### F2 — Bloques declarativos y formato de temas

Dependencias: F0/F1 y D7. Riesgo: alto por persistencia e importación/exportación.

- [ ] Especificar JSON v3, defaults y límites para CLOCK/WEATHER antes de escribir el parser.
- [ ] Extender modelo, validadores, store y writer; conservar fixtures v1/v2.
- [ ] Conectar estado de runtime al canvas mediante un contrato recibido, no repositorios.
- [ ] Añadir inspectores, preview determinista y política para cabecera/reloj explícito.
- [ ] Reservar el espacio/contrato visual del clima sin inventar datos mientras falta proveedor.
- [ ] Exportar/reimportar composición y assets sin incluir configuración privada.

Salida: variantes portables y compatibles hacia atrás, sin nuevos ThemeId por combinación.

### F3 — Open-Meteo y configuración meteorológica

Dependencias: F0/F2, D1/D2/D6/D8 para completar publicación. Riesgo: medio.

- [x] Implementar búsqueda de ciudades, selección inequívoca y persistencia de preferencias.
- [x] Implementar primer corte de cliente/repositorio, mapeo de respuesta y tests HTTP controlados.
- [x] Integrar el resumen meteorológico y su estado en Home; bloque declarativo WEATHER sigue pendiente.
- [ ] Incorporar estados sin configurar, cargando, disponible, antiguo y no disponible.
- [x] Añadir controles de unidades, habilitar/deshabilitar, borrar ubicación y reintentar.
- [x] Añadir atribución enlazada y explicación de privacidad de ubicación manual.

Salida: clima real en Inicio, sin GPS/ADB, sin bloquear apps ni búsqueda por fallo de red.

Avance 2026-10-25: `:core:model` tiene contratos de ciudad, unidades, clima actual y
repositorio; `:core:data` consulta los endpoints oficiales de geocodificación/forecast con
hosts fijos, HTTPS, límites de respuesta y timeout, e incluye mapeo de coordenadas,
temperatura, código WMO, día/noche y hora local con offset. Las preferencias de ciudad y
unidades se guardan en DataStore local. Home muestra clima y atribución con link; la
configuración permite buscar, seleccionar, cambiar unidades, activar/desactivar y borrar.
Tests usan conexiones falsas y un fake de repositorios, sin red real. Caché offline, estados
de antigüedad, bloques WEATHER y validación en dispositivo siguen pendientes.

### F4 — Panel de capacidades y permisos

Dependencias: F0; independiente de F1–F3. Riesgo: medio.

- [ ] Definir catálogo de capacidades por función, no una lista indiscriminada de permisos.
- [ ] Consultar estado real al abrir/reanudar; no persistir un booleano como prueba del grant.
- [ ] Mostrar disponible, requiere autorización, denegado, no soportado y estado desconocido.
- [ ] Preferir ajustes oficiales y flujos de consentimiento: acceso de uso, selección de
  Inicio y vinculación de widgets según la función aprobada.
- [ ] Manejar ActivityNotFound/SecurityException y fabricantes sin la pantalla correspondiente.

Salida: el usuario entiende qué habilita cada permiso y puede mantener el launcher en
modo normal sin conceder ninguno de los opcionales.

### F5 — Asistente ADB desde PC

Dependencias: F4 y D4/D5. Riesgo: alto; no ejecutar escrituras hasta aprobar operaciones.

- [ ] Aprobar una matriz función -> permiso/comando -> versiones soportadas -> verificación
  -> método de revocación/restauración cuando exista.
- [ ] Generar instrucciones para el applicationId real y usuario Android correcto; evitar
  argumentos libres interpolados en comandos.
- [ ] Separar permiso runtime, acceso especial/AppOps, rol y operación de shell. No tratarlos
  como si todos se concedieran con `pm grant`.
- [ ] Explicar conexión y autorización del equipo; verificar el efecto real desde la app.
- [ ] Documentar permisos concedidos una vez frente a servicios que requieren sesión activa.
- [ ] Documentar cómo revocar cada capacidad y conservar los valores anteriores de cambios
  reversibles realizados por el launcher. No ofrecer una restauración universal ficticia.

Salida: guía reproducible para operaciones concretas aprobadas; no incluye terminal embebida,
escaneo de red, activación automática de depuración ni eliminación de apps del sistema.

### F6 — Hospedaje de widgets Android externos

Dependencias: F0/F2/F4, aceptación D3 y dispositivo de D5. Riesgo: alto.

- [ ] Prototipo en TV: detectar FEATURE_APP_WIDGETS, proveedores y Activity de vinculación.
- [ ] Registrar decisión continuar/no soportado según firmware y navegación por D-pad.
- [ ] Implementar AppWidgetHost, IDs persistentes locales y AppWidgetHostView con AndroidView.
- [ ] Usar bindAppWidgetIdIfAllowed y el flujo ACTION_APPWIDGET_BIND si no está autorizado;
  declarar lo requerido por el host sin asumir que una declaración concede BIND_APPWIDGET.
- [ ] Manejar configuración del proveedor, cancelación, borrado de IDs y errores de retorno.
- [ ] Soportar tamaño mínimo, redimensionamiento, start/stop listening y recreación del host.
- [ ] Manejar proveedor desinstalado, restauración de backup/tema sin binding y widgets vacíos.
- [ ] Solicitar selección/autorización local tras importar un marcador; no enlazar por ZIP.

Salida: widgets externos opcionales en dispositivos compatibles; reloj interno disponible
como fallback. Glance no sustituye el host: crear nuestros propios AppWidgetProvider sería
otro alcance, no necesario para dibujar relojes Compose dentro del launcher.

### F7 — Shizuku opcional

Dependencias: F4/F5, D4/D5 y aprobación de instalar/usar un servicio externo. Riesgo: alto.

- [ ] Validar instalación, versión y licencia de las dependencias antes de añadir SDK.
- [ ] Gestionar consentimiento, Binder disponible/muerto, permiso revocado y reinicio.
- [ ] Implementar solo las operaciones ya aprobadas detrás del mismo contrato de capacidades.
- [ ] Tratar identidad shell y root como capacidades distintas; no requerir root ni asumir
  que Shizuku autoriza cualquier acción o transfiere todos los permisos al proceso del APK.
- [ ] En equipos sin root, explicar que el servicio normalmente debe iniciarse de nuevo al
  reiniciar. Volver al modo normal cuando no esté disponible.

Salida: funciones concretas sin PC durante su uso, sujetas a autorización y soporte real.

### F8 — Decisión sobre cliente ADB integrado

Dependencias: F5/F7 o decisión explícita de no usar Shizuku; D4/D5. Riesgo: muy alto.

- [ ] Identificar qué función no resuelven las opciones anteriores y justificar el coste.
- [ ] Prototipar emparejamiento/conexión en el firmware objetivo, incluida conexión al propio
  dispositivo si ese es el requisito; no dar por hecho que loopback funciona.
- [ ] Separar puerto de emparejamiento y de conexión, cancelación, TLS, renovación y claves.
- [ ] Revisar biblioteca/protocolo/licencia y protección de claves en almacenamiento privado.
- [ ] No abrir ADB TCP heredado por defecto ni intentar activar depuración desde una app normal.
- [ ] Si no hay beneficio o compatibilidad suficiente, cerrar la fase como NO-GO documentado;
  si lo hay, crear especificación y aprobación de seguridad antes de implementar producción.

Salida: decisión GO/NO-GO basada en dispositivo y necesidad, no un cliente inseguro agregado
por defecto. El emparejamiento moderno documentado para TV parte de Android TV 13+, sujeto
al fabricante; no basta con comprobar `SDK_INT >= 30`.

### F9 — Estabilización y publicación por entregas

Dependencias: las fases incluidas en cada entrega. Riesgo: alto si se publica sin dispositivo.

- [ ] Ejecutar suites completas, compilación, lint y revisión de fronteras.
- [ ] Recorrer todos los temas incluidos, demos e importados con D-pad y toque donde aplique.
- [ ] Comprobar que clima, reloj, permisos y widgets no sustituyan Inicio/Apps/Buscar.
- [ ] Medir llamadas HTTP, actualizaciones de reloj, listeners y recursos al suspender Home.
- [ ] Probar actualización desde instalación anterior sin pérdida de temas ni preferencias.
- [ ] Registrar limitaciones por fabricante y no publicar capacidades experimentales como
  universales. Firma release y credenciales quedan fuera del repositorio.

## 8. Secuencia de entregas

```text
F0 -> F1 -> F2 -> F3 -> validación F9 de relojes/clima
F0 -> F4 -> F5      -> validación F9 de permisos/asistente
F2 + F4 + D3/D5 -> F6 -> validación F9 de widgets externos
F5 + aprobación -> F7 -> validación F9 de Shizuku
F5/F7 + necesidad demostrada -> F8 (decisión y especificación independiente)
```

Preparar cortes pequeños, no un único cambio que mezcle codecs, red y operaciones shell.
Cada fase entra con especificación READY y sigue RED -> GREEN -> REFACTOR para las reglas
observables. La ruta crítica inicial es baseline -> reloj común -> contrato -> clima.
Widgets/ADB no bloquean la publicación de relojes/clima si no fueron incluidos en esa entrega.

## 9. Criterios de aceptación y trazabilidad

Todos están PENDIENTES; los tests mencionados a continuación son trabajo por implementar,
no evidencia de que esta planificación ya fue ejecutada.

| ID | Comportamiento observable | Fase | Prueba mínima |
|---|---|---|---|
| AC01 | Buscar abre el buscador existente; Apps mantiene su destino y retorno; Inicio conserva tema | F0/F9 | Regresiones existentes + integración UI/dispositivo |
| AC02 | El reloj muestra la hora correcta al cambiar minuto, fecha, zona, 12/24 h y reanudar | F1 | Unit con tiempo controlado + prueba de plataforma |
| AC03 | Elegir un reloj explícito no añade otro automáticamente; legacy conserva su configuración | F1/F2 | Unit de resolución + preview/UI por tema |
| AC04 | ZIP v1/v2 sigue importándose y conserva assets/apariencia; v3 hace round-trip | F2 | Unit codecs/store/exportación con fixtures |
| AC05 | Campos/versiones inválidos se rechazan sin efectos ni ejecución | F2 | Pruebas negativas del codec y frontera host |
| AC06 | Elegir ciudad devuelve datos de esa ubicación y el cambio no acepta respuesta anterior | F3 | Repository/ViewModel con red simulada y scheduler |
| AC07 | Repetir bloques/recomponer no duplica solicitudes; sin TTL vencido no renueva | F3 | Unit de caché, concurrencia y llamadas |
| AC08 | Offline muestra dato antiguo identificado o estado no disponible; nunca datos ficticios | F3 | Unit estados + UI con caché/sin caché/caducidad |
| AC09 | HTTP 429, timeout, JSON inválido y código WMO desconocido no bloquean Home | F3 | HTTP simulado, mapper y ViewModel |
| AC10 | Desactivar/borrar clima detiene refrescos y elimina ubicación/caché según la acción | F3 | Unit persistencia/ciclo de vida |
| AC11 | Fuente/licencia son visibles y el ZIP no contiene ubicación, datos ni claves | F2/F3 | UI + inspección del archivo exportado |
| AC12 | Permiso denegado/revocado o no soportado se refleja al reanudar sin bloquear launcher | F4 | Fake de capacidades + prueba real de ajustes |
| AC13 | Solo operaciones aprobadas generan instrucciones; se rechazan parámetros no válidos | F5 | Unit allowlist/argumentos, sin ejecutar shell real |
| AC14 | El estado tras ADB se verifica; fallos no se presentan como permiso concedido | F5 | Fake + prueba autorizada en dispositivo |
| AC15 | Vinculación cancelada/configuración fallida no deja IDs huérfanos ni pierde el tema | F6 | Instrumented con proveedor de prueba |
| AC16 | Widget externo se restaura localmente y se elimina limpiando su ID | F6 | Instrumented recreación/eliminación |
| AC17 | Sin soporte de widgets o proveedor instalado, hay fallback recuperable | F6 | Fake + matriz de dispositivos |
| AC18 | Shizuku ausente, revocado, muerto o tras reboot desactiva solo funciones dependientes | F7 | Fake Binder + E2E autorizado |
| AC19 | ZIP/importación no dispara solicitudes de permisos, red, pairing ni comandos | F2/F5–F8 | Integración con espías en fronteras |
| AC20 | Home oculta/suspendida no mantiene refrescos meteorológicos ni ticks innecesarios | F1/F3/F9 | Scheduler/lifecycle + medición en dispositivo |

## 10. Plan de pruebas y quality gates

### Automatización

- Unit: formato/hora, resolución de bloques, WMO/unidades, TTL, URLs/argumentos, estados
  de permisos y compatibilidad de codecs, sin red ni reloj real ni `Thread.sleep`.
- Repository/ViewModel: concurrencia, cancelación, ciudad anterior, fallos de persistencia,
  transiciones y ausencia de efectos duplicados.
- HTTP: respuestas locales mediante fake o servidor de pruebas; evaluar la dependencia
  mínima necesaria, sin usar Open-Meteo real en CI.
- Compose/instrumented: accesos y regreso al buscador, foco inicial/restauración, estados
  de clima, configuración/vinculación de widgets. Preparar runner/dependencias porque no
  se encontró una suite instrumented existente.
- Previews: reloj digital/analógico/fecha, clima sin configurar/cargando/disponible/antiguo/
  error y host de widget no soportado. Datos deterministas, sin red ni operaciones ADB.

### Comandos previstos sobre tareas existentes

Con JDK 17, Gradle 8.9 y SDK configurados; ejecución futura, no realizada al redactar:

```text
gradle :core:model:test :core:ui:testDebugUnitTest :core:data:testDebugUnitTest
gradle :feature:home:testDebugUnitTest :feature:editor:testDebugUnitTest :feature:search:testDebugUnitTest
gradle verifyModuleBoundaries
gradle :app:assembleDebug :app:assembleRelease
gradle lintDebug
```

Añadir tareas concretas para módulos/tests nuevos solo cuando existan. `:app:testDebugUnitTest`
por sí solo no cubre los tests de las bibliotecas. Las pruebas instrumented se ejecutarán
contra un dispositivo/emulador seleccionado; las que escriben ajustes o prueban ADB/Shizuku
solo con autorización explícita y restauración de las modificaciones propias.

### Matriz manual mínima

- Android TV API 26–32: reloj/clima y modo normal; no asumir pairing moderno.
- Android TV API 33–35 o versiones objetivo acordadas: validar widgets y depuración según
  capacidades reales, no solo número de API.
- Emulador TV y al menos un TV/box físico del fabricante objetivo.
- Temas nativos, Free My TV, Launch Dock, Side Stream y ZIP legacy/importado.
- Offline, suspensión, cambio de red/fecha/hora, reinicio, revocación y proveedor eliminado.

Si falta hardware, registrar esa limitación y no marcar la compatibilidad como VERIFIED.

## 11. Riesgos y medidas

| Riesgo | Medida |
|---|---|
| Regresión del buscador o controles duplicados | Baseline y regresiones primero; no reescribir navegación en estas fases |
| Llamadas de clima por bloque/recomposición | Caché compartida, deduplicación y propietario de lifecycle |
| Alojamiento gratuito usado comercialmente | Gate D1 y comprobación de términos antes de publicar |
| Despliegue propio de Open-Meteo costoso | No asumir infraestructura gratuita; cotización/operación fuera del APK |
| Ubicación filtrada por ZIP/logs | Preferencias privadas, separación visual/runtime e inspección de exportación |
| Archivo v3 mal etiquetado o pérdida de datos legacy | Versionado explícito y pruebas de ida/vuelta de todos los formatos |
| Widgets ausentes o controles táctiles no accesibles en TV | Prototipo temprano, fallback interno y soporte por fabricante |
| Permisos/operaciones distintos por firmware/usuario Android | Detección y comprobación real; allowlist por capacidad/versión |
| Pérdida de sesión Shizuku/ADB | Degradación al modo normal; nunca bloquear el launcher |
| Exposición de ADB o ejecución desde un tema | Sin terminal libre, sin activar debugging automáticamente, sin comandos importados |
| Nueva infraestructura invade renderer o features hermanas | Contratos recibidos, composición en app y revisión de fronteras |

## 12. Definition of Done por entrega

- [ ] Decisiones bloqueantes resueltas y criterios de la entrega identificados.
- [ ] Implementación y regresiones cubiertas con evidencia RED/GREEN cuando aplique.
- [ ] Tests, codecs, fronteras, Debug/Release y lint ejecutados con resultados registrados.
- [ ] Previews seguros compilables; recorrido D-pad y retorno al buscador validado.
- [ ] Privacidad/atribución y compatibilidad de ZIP verificadas.
- [ ] Permisos opcionales no bloquean reloj, clima ni funciones básicas.
- [ ] Limitaciones por firmware documentadas; no declarar soporte sin dispositivo.
- [ ] Actualizar estado DRAFT -> READY -> IMPLEMENTED -> VERIFIED solo con evidencia.

## 13. Referencias para cerrar las especificaciones

- Open-Meteo API: https://open-meteo.com/en/docs
- Geocodificación: https://open-meteo.com/en/docs/geocoding-api
- Licencia del servidor/datos: https://open-meteo.com/en/licence
- Servicio gratuito y comercial: https://open-meteo.com/en/pricing
- Host de widgets Android: https://developer.android.com/develop/ui/views/appwidgets/host
- ADB y requisitos de conexión TV: https://developer.android.com/tools/adb
- Shizuku API/limitaciones: https://github.com/RikkaApps/Shizuku-API
- Inicio y reinicio de Shizuku: https://shizuku.rikka.app/guide/setup/

Licencias y condiciones fueron consultadas en el análisis previo; reconfirmarlas al
seleccionar dependencias, servicio y canal de distribución. El proyecto actual es MIT;
consumir una API, distribuir una biblioteca y autoalojar el servidor son casos distintos.
