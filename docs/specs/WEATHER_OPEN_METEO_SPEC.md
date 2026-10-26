# Open-Meteo y selección manual de ciudad

Estado: IMPLEMENTED para consulta, configuración y presentación básica; caché offline pendiente

## Objetivo y comportamiento

El usuario elige una ciudad mediante búsqueda de Open-Meteo Geocoding. El launcher consulta
el clima de esa ubicación sin solicitar ubicación GPS. El fallo de red/clima no afecta
Inicio, Apps ni Buscar. No se muestran valores ficticios.

## Reglas

- RB1: solo se consultan los hosts HTTPS oficiales `geocoding-api.open-meteo.com` y
  `api.open-meteo.com`, con parámetros construidos por la app.
- RB2: búsqueda vacía o menor de 2 caracteres no produce solicitud.
- RB3: ubicación identificada por nombre, región/país y coordenadas; resultados ambiguos se
  presentan para selección explícita.
- RB4: el forecast se solicita por coordenadas seleccionadas, unidades elegidas, condición
  actual, temperatura y día/noche.
- RB5: WMO desconocido se representa como desconocido; no se inventa una condición.
- RB6: API/DTO no se filtran a la UI; errores de red/HTTP/JSON se representan como fallo.
- RB7: preferencias meteorológicas son locales y nunca forman parte del ZIP del tema.
- RB8: Open-Meteo requiere atribución visible y el uso del endpoint gratuito queda sujeto a
  uso personal/no comercial confirmado por el usuario.

## Caché y publicación

Política aún pendiente de confirmación: usar provisionalmente la propuesta del plan general
(30 minutos fresco, hasta 6 horas dato antiguo; pasado ese límite no es actual). Sin trabajo
periódico en background en el MVP. La configuración de ciudad/unidades y atribución ya se
conectaron; la caché offline se implementará en un corte posterior antes de declarar la fase
completa.

## Criterios de aceptación del corte actual

- AC1: modelos del dominio rechazan coordenadas fuera de rango y nombres vacíos.
- AC2: geocodificación mapea cero o más ubicaciones sin fabricar resultados.
- AC3: forecast mapea unidades, condición WMO y día/noche; valores ausentes/malformados
  fallan como respuesta inválida.
- AC4: requests solo se envían a endpoints HTTPS permitidos y con timeout acotado.
- AC5: tests HTTP son deterministas y no acceden a la red real.

## Pendiente para completar la fase

Caché persistente con frescura/antigüedad visible, renovación al volver a Home y pruebas de
preferencias DataStore. Los bloques WEATHER de tema/ZIP y widgets se mantienen fuera de esta
entrega; se conserva compatibilidad con composiciones y formatos existentes.

## Trazabilidad

| Regla/AC | Componente | Prueba | Estado |
|---|---|---|---|
| AC1 | `WeatherCity` | unit `:core:model` | verificado |
| AC2-4 | cliente y mapper Open-Meteo | unit `:core:data` con conexiones falsas | verificado |
| AC5 | `OpenMeteoClientTest` | JUnit | verificado |
| Configuración manual | `WeatherSettingsDialog` + `HomeViewModel` + DataStore | unit ViewModel + compile | verificado |
| Presentación | `WeatherSummaryCard` + enlace de atribución | compile + lint | verificado |
| Caché/antigüedad | pendiente | tests de política y persistencia | pendiente |
