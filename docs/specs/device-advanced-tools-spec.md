# Herramientas avanzadas del dispositivo

Estado: READY para la lista de operaciones confirmadas; integración pendiente

## Objetivo

Permitir al propietario optimizar opciones visuales de Android TV/proyector y retirar
aplicaciones seleccionadas por usuario, usando instrucciones ADB desde PC o Shizuku con
consentimiento explícito.

## Decisiones confirmadas

- Cambiar las tres escalas de animación (`window_animation_scale`,
  `transition_animation_scale`, `animator_duration_scale`) a un preset de 0.5x, con
  restauración de los valores previos guardados.
- No modificar depuración USB/inalámbrica, procesos en segundo plano, límites de procesos,
  permisos de seguridad ni ajustes no enumerados.
- Apps de usuario: usar el desinstalador oficial de Android con confirmación del sistema.
- Apps preinstaladas seleccionadas por el usuario: retirar solo del usuario actual (`pm
  uninstall --user current`), sin eliminar el APK del firmware. Ofrecer restauración con
  `cmd package install-existing --user current` si el dispositivo lo permite.
- Nunca actuar en lote; una app y una confirmación por operación.
- El flujo ADB externo produce instrucciones explícitas para PC. La aplicación no abrirá
  un servidor ADB TCP ni conectará al puerto de depuración automáticamente.
- Shizuku es optativo y requiere servicio activo, autorización Shizuku y consentimiento
  por operación. Sin servicio, la app permanece completamente funcional.

## Seguridad y límites

- Sin root requerido ni garantía de que un fabricante permita la operación.
- Solo operaciones enumeradas; no hay consola, comandos recibidos de red/ZIP ni argumentos
  libres.
- Bloquear el paquete de este launcher, Settings, System UI y componentes críticos conocidos.
- Validar nombre de paquete con regex y pasar argumentos al proceso por separado, nunca
  interpolar texto del usuario en un shell.
- Consultar resultado y estado posterior; no informar éxito si el firmware lo rechaza.
- Mostrar el impacto y la restauración conocida antes de confirmar.
- Desinstalar una app puede perder sus datos. `install-existing` solo restaura paquetes que
  permanecen preinstalados; no reinstala una app descargada ni recupera sus datos.

## Criterios de aceptación

- AC1: el panel ofrece únicamente preset 0.5x y volver a los valores guardados.
- AC2: valores previos se capturan antes de aplicar cambios; no se sobrescriben en una
  segunda aplicación del preset.
- AC3: las instrucciones ADB contienen solo las tres claves aprobadas y comandos de lectura
  para registrar/restaurar valores; ninguna opción de depuración se activa.
- AC4: el usuario escoge una app instalada; apps de usuario abren la confirmación del
  Package Installer.
- AC5: retirar una app del sistema afecta solo al usuario actual, muestra una confirmación
  distinta y nunca desinstala el APK del firmware.
- AC6: apps críticas y el propio launcher no se pueden retirar desde el panel.
- AC7: Shizuku ausente, revocado o detenido deja la operación en estado no disponible y no
  bloquea Home/Apps/Buscar.
- AC8: después de cada cambio se comprueba el estado; error o resultado desconocido se
  muestra como fallo, no como éxito.
- AC9: operaciones sobre widgets externos se limitan a un appWidgetId/proveedor que el
  usuario eligió en el selector del sistema o lista; nunca proceden de un ZIP.

## Validación

- Unit: allowlist de claves, validación de paquete, construcción de instrucciones y
  restricciones de usuario actual.
- Instrumented/manual: Shizuku no disponible, permiso concedido/revocado, firmware que
  rechaza `settings put`, app normal, app del sistema, cancelación y restauración.
- Quality gates: tests de modelo, app assemble/lint y prueba física por fabricante.

No se ejecutan comandos ni se modifican ajustes de un dispositivo durante build/tests.
