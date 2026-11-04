# Personalización avanzada y temas de usuario

Estado: READY (fondo/temas); Watch Next en investigación separada

## Objetivo

Ampliar el launcher con herramientas de fondos, componentes configurables, contenido
Watch Next y temas importables creados por usuarios, tomando Arc Launcher como referencia
funcional sin copiar su código o asumir que todas sus funciones son portables.

## Evidencia revisada

- Arc README: Watch Next, wallpapers de imagen/vídeo, presentaciones, wallpaper por hora,
  Unsplash con clave propia, slideshow Reddit, banners personalizados, presets de UI,
  widgets y acciones de sistema.
- El proyecto actual ya persiste tema/acento/fondo preset/forma y cinco atajos webOS.
- No existe hoy modelo de plugin o importación/exportación de tema.
- `LauncherAppearance` y DataStore usan enums del binario; un tema externo no debe ejecutar código.
- Android Watch Next es una API de publicación del contenido de la propia app en el proveedor TV;
  su documentación no garantiza que un launcher pueda leer las filas privadas de otras apps.

## Alcance propuesto

1. Fondo local mediante Storage Access Framework, color sólido y perfil original; persistir
   URI con permiso de lectura persistente. No pedir permiso amplio de almacenamiento.
2. Ajustar gradualmente componentes presentes: visibilidad/orden de filas, tamaño/radio de
   tarjetas, densidad, transparencia de superficie, intensidad del fondo y accesos.
3. Watch Next solamente con datos publicados por apps compatibles a través de contratos
   Android TV accesibles. Soportar estado no disponible y proveedor vacío; no inspeccionar
   pantallas ajenas ni prometer cobertura de todas las apps.
4. Temas de usuario limitados a apariencia segura: paleta, densidad/radio, visibilidad/orden
   de filas, referencias a assets y (formato v2) composición con bloques nativos permitidos.
5. Exportar/importar paquetes de tema declarativos (manifest JSON + imágenes locales), sin
   scripts, DEX, URLs de recursos ejecutables ni permisos incluidos por el paquete.
6. Validar tamaño, MIME, dimensiones, campos requeridos e identificador; vista previa antes
   de activar; eliminar/volver al perfil integrado sin perder los otros ajustes.
7. Importar/exportar archivo `.zip` local mediante Storage Access Framework. Instalación desde tiendas/repositorios remotos,
   galería compartida y firmas/versionado son decisiones posteriores.
8. Preparar proveedores separados para Watch Next soportado y catálogo externo; TMDB queda
   desactivado hasta que se defina configuración de credenciales.

## Fuera de alcance inicial

- Integración con APIs privadas de Netflix, Disney+, Prime Video u otros servicios.
- Código/plugin ejecutable y layouts arbitrarios aportados por paquetes externos.
- Reproducción de contenido, scraping de interfaces o captura de texto de otras apps.
- Fondos de vídeo, Reddit/Unsplash, scheduler de brillo/horarios y automatización ADB/Shizuku
  hasta definir fuente, permisos, UX y presupuesto de red/batería por separado.
- Código de terceros ejecutable en el proceso del launcher.

## Reglas

- RB1: los temas instalados son datos declarativos e inertes; el launcher controla las
  acciones, permisos, destinos y componentes disponibles.
- RB2: la instalación es local, validada y reversible; perfil actual y tema integrado quedan
  intactos ante archivo inválido, URI revocada o eliminación de un tema.
- RB3: Watch Next muestra solo contenido accesible por contrato del sistema; no fabrica
  recomendaciones ni convierte una ficha en una promesa de reproducción.
- RB4: todo contenido seleccionable indica su proveedor; al seleccionar se resuelve la acción
  compatible o se informa que no hay destino.
- RB5: el usuario concede cada documento/imagen de fondo con selector del sistema; no se
  requiere Shizuku/ADB ni permiso de almacenamiento general para la personalización normal.

## Decisiones pendientes

- Watch Next: investigación separada; no bloquea fondos o temas. No declarar disponible hasta
  demostrar lectura cross-app permitida en el hardware objetivo y un destino de apertura válido.
- Tema ZIP v1: `manifest.json` requerido; solo paleta, fondo sólido o asset local,
  estilo de tarjetas y layout integrado elegido. Tema v2 agrega composición limitada a bloques
  nativos permitidos; nunca contiene código ejecutable ni acciones personalizadas.
- Esquema `manifest.json` v1: objeto JSON con `formatVersion` (entero 1), `id` (UUID),
  `name` (1..40 caracteres), `layout` (uno de los IDs integrados), `accentArgb` (ARGB
  hexadecimal opcional), `background` (objeto con `type`: `solid` y `argb`, o `type`: `asset`
  y `path` bajo `assets/`), `tileShape` (`profile`, `rounded` o `square`). Campos desconocidos
  se rechazan en v1 para evitar interpretaciones distintas entre versiones.
- Los assets importados se copian al almacenamiento privado de la app usando nombres
  generados; el nombre/ruta original del ZIP no se usa como ruta de escritura.
- Límites ZIP v1: 20 MiB total, 10 assets, 5 MiB por asset, imagen raster PNG/JPEG/WebP hasta
  4096x4096; rutas planas bajo `assets/`, sin symlinks, traversal, duplicados ni archivos anidados.
  Rechazar formatos desconocidos y cualquier entrada ejecutable.
- El tema se previsualiza antes de aplicar; tema importado se identifica por UUID y puede
  exportarse/eliminarse sin alterar el perfil integrado.
- En el primer acceso a Perfil visual, inicializar la biblioteca vacía con un paquete demo
  webOS. Guardar un marcador para que una eliminación posterior sea permanente y no reinstale
  el demo en cada visita.
- Decidido: temas v1 conservan layouts integrados; temas v2 pueden definir una composición con
  bloques cerrados nativos, renderizada por la app. No contienen código Compose/plugins
  ejecutables. Contrato v2 en `theme-composition-editor-spec.md`.
- Decidido: paquetes de tema se comparten como `.zip` local vía SAF.
- Fuera del formato v1: mapeo de apps, accesos, acciones personalizadas y widgets configurables.
- TBD: servicio de metadatos externo. No es requisito para Watch Next; decidir aparte si se
  quiere un catálogo general de títulos, coste, clave y atribución. Usuario eligió ambas
  fuentes pero pidió no configurar una clave TMDB todavía; fuente externa queda inactiva.

## Criterios de aceptación propuestos

- AC1: cambiar entre color, fondo local y perfil original persiste al reiniciar; URI revocada
  cae a color/perfil seguro sin crash.
- AC2: un paquete declarativo válido puede previsualizarse, instalarse, activarse, exportarse
  y eliminarse con mando.
- AC3: paquetes malformados, demasiado grandes o que intenten añadir código/URL ejecutable
  se rechazan sin modificar el tema actual.
- AC4: archivo ZIP conforme al formato v1 puede validarse, previsualizarse, aplicarse,
  exportarse y eliminarse; no ejecuta contenido del archivo.
- AC5: archivo corrupto, con zip bomb, rutas inseguras, límites excedidos o MIME/dimensiones
  no soportadas se rechaza y preserva el tema activo.
- AC6: Watch Next solo se implementa si una prueba en dispositivo demuestra acceso autorizado
  a datos cross-app y navegación compatible; de lo contrario se informa no disponible y no se
  inspeccionan datos privados. TMDB no realiza solicitudes sin configuración explícita.
- AC7: tarjetas con destino abren el destino registrado; sin destino no afirman reproducir.
- AC8: APK, unit tests, lint y prueba manual D-pad pasan en Android TV objetivo.

## Trazabilidad

| Requisito | Criterio | Nivel | Estado |
|---|---|---|---|
| Fondo con SAF | AC1 | Compose + instrumented | pending |
| Lectura segura ZIP | parte de AC5 | unit | implemented; firma y dimensiones de cabecera |
| Validacion manifiesto v1 | AC3-AC5 | unit | implemented |
| Previsualizar/aplicar importacion ZIP | AC2-AC5 | unit + manual | implemented; falta probar en TV |
| Exportar apariencia activa a ZIP | AC2-AC4 | unit + manual | implemented; falta probar en TV |
| Biblioteca persistente, reactivar y eliminar temas | AC2 | unit + manual | implemented; falta probar en TV |
| Watch Next / fuentes externas | AC6 | spike de dispositivo + manual | investigación |
| D-pad y validación final | AC8 | manual + build gates | pending |
