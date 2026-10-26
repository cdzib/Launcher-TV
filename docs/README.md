# Documentación del launcher

Este es el índice y punto de entrada de la documentación del proyecto. La documentación versionada vive bajo `docs/`; el README de la raíz se reserva para presentar y ejecutar el producto.

## Estándar documental

### Estructura

| Ruta | Contenido |
|---|---|
| `architecture/` | Arquitectura actual, límites de módulos y guías técnicas duraderas |
| `specs/` | Requisitos, criterios de aceptación y planes de cambios o features |
| `design/` | Decisiones UX/UI, prototipos y capturas relacionadas |
| `operations/` | Procedimientos de desarrollo, validación, publicación y soporte |
| Raíz de `docs/` | Este índice y ejemplos de formatos compartidos |

No dupliques el mismo contenido en varias carpetas. Las guías vigentes describen el estado actual; las propuestas deben indicar su estado y enlazar la implementación cuando exista.

### Formato y ciclo de vida

- Markdown (`.md`) para documentación explicativa; conservar HTML/PNG/SVG cuando sean prototipos o evidencia visual necesarios.
- Nombres descriptivos en `kebab-case` para documentos nuevos. Mantener nombres existentes mientras moverlos no aporte claridad.
- Las specs de cambios sustanciales indican estado (`DRAFT`, `READY`, `IMPLEMENTED`, `VERIFIED`), alcance, criterios verificables, riesgos y validación. No marcar un estado sin evidencia.
- Los planes son temporales: al terminar, registrar resultado y enlazar la documentación vigente; después archivar o eliminar el plan obsoleto con revisión explícita.
- Enlaces relativos a partir del documento. No incluir secretos, datos personales ni rutas locales de máquina.
- La evidencia distingue pruebas ejecutadas de validaciones pendientes (por ejemplo, hardware o CI remoto).

## Arquitectura

- [Arquitectura modular](architecture/architecture.md)

## Especificaciones y planes

- [Catálogo remoto de temas](specs/remote-theme-catalog-spec.md)
- [Editor de composición de temas](specs/theme-composition-editor-spec.md)
- [Temas y personalización avanzada](specs/advanced-theme-customization-spec.md)
- [Personalización del launcher](specs/android-launcher-customization-spec.md)
- [Creador y personalización de temas](specs/theme-creator-and-customization-spec.md)
- [Búsqueda en Studio](specs/studio-search-spec.md)
- [Referencias visuales de temas](specs/theme-visual-references.md)
- [Herramientas avanzadas del dispositivo](specs/device-advanced-tools-spec.md)
- [Seguridad e imágenes de fondo](specs/device-safety-and-background-images-spec.md)
- [Estabilidad](specs/stability-spec.md)
- [Motor de generación de temas](specs/theme-generation-engine-spec.md)
- [Plan de clima, reloj y ADB](specs/weather-clock-adb-plan.md)
- [Clima Open-Meteo](specs/weather-open-meteo-spec.md)

## Diseño

- [Navegación de secciones por tema](design/section-navigation-design.md)
- [Hub de Ajustes](design/settings-hub-design.md)
- [Prototipo del Hub de Ajustes](design/settings-hub-prototype.html)
- Los prototipos y recursos gráficos se agrupan bajo `design/` junto con su documento de contexto.

## Ejemplos de contratos

- [Ejemplo de catálogo de temas](theme-catalog-example.json)
- [Ejemplo de manifiesto de tema](theme-manifest-v2-example.json)

## Revisión de documentación

Al cambiar comportamiento, actualizar la spec o guía que corresponda y enlazarla aquí si es un documento de referencia. En revisiones periódicas, comprobar enlaces, estados, pasos reproducibles y documentos duplicados u obsoletos.
