# Catálogo remoto e importación por URL de temas

Estado: READY

## Objetivo

Permitir descubrir temas publicados en un catálogo JSON alojado en GitHub (URL raw HTTPS
configurada por el usuario) y descargar un ZIP de tema desde un enlace HTTPS directo, sin
depender del explorador de archivos de Android TV.

## Contrato v1

- Catálogo: JSON UTF-8 de hasta 1 MiB, objeto `{ "formatVersion": 1, "themes": [...] }`;
  máximo 100 elementos.
- Elemento: `id` UUID, `name` (1..40), `description` (0..160), `version` (1..32),
  `downloadUrl` HTTPS y `sha256` hexadecimal de 64 caracteres.
- El `id` del catálogo debe coincidir con el UUID del manifiesto del ZIP y el SHA-256 descargado
  con el declarado antes de mostrar la vista previa.
- El enlace directo admite solo HTTPS; el ZIP sigue pasando por `ThemeArchiveReader` y
  `ThemeManifestValidator` antes de previsualizarse e instalarse.

## Reglas de red y actualización

- Usar conexión HTTPS, timeouts de conexión/lectura de 15 s, redirects manuales HTTPS hasta
  3 saltos y límites de 1 MiB para catálogo / 20 MiB para ZIP.
- No descargar automáticamente al iniciar la app; solo por acción explícita del usuario.
- Un catálogo inválido, descarga fallida, hash incorrecto o paquete inválido no cambia temas
  instalados ni el tema activo.
- Si el UUID ya está instalado, solicitar confirmación explícita antes de reemplazarlo con la
  nueva versión; conservar el marcador de tema predeterminado y otros paquetes.
- No aceptar HTTP, URLs con usuario/contraseña ni redirects a HTTP. No ejecutar código remoto.

## UX

- Ajustes incluye “Temas online”: URL de catálogo GitHub raw y URL directa de ZIP.
- El catálogo se carga solo al pulsar “Buscar”; cada tarjeta muestra nombre y versión y permite
  descargar. La descarga directa usa el mismo flujo de vista previa/aplicación.
- Tras descargar y validar, mostrar la vista previa existente; solo “Aplicar” instala o actualiza.
- Los demos incluidos y la biblioteca local siguen disponibles sin conexión.

## Criterios de aceptación

- AC1: parser acepta catálogo v1 válido y rechaza versión desconocida, duplicados, más de 100
  elementos, campos fuera de límites y URLs que no sean HTTPS.
- AC2: URL inválida, redirect inseguro, exceso de tamaño o error de red se muestra como error y
  no altera la biblioteca/tema activo.
- AC3: SHA-256 e ID del catálogo se verifican antes de instalar/previsualizar.
- AC4: URL directa HTTPS válida termina en la misma vista previa y validación del ZIP.
- AC5: actualizar un UUID instalado requiere confirmación y reemplaza el paquete de forma
  segura; cancelar conserva el paquete anterior.
- AC6: tests unitarios, APK, lint y smoke test en dispositivo Samsung/Android TV.

## Trazabilidad

| Requisito | Criterio | Nivel | Estado |
|---|---|---|---|
| Parser/URL/Hash | AC1, AC3 | unit | implemented |
| Downloader HTTPS | AC2, AC4 | unit + manual | implemented; faltan endpoints de usuario |
| Reemplazo de tema | AC5 | unit | implemented |
| UI remoto y dispositivo | AC6 | manual + build gates | pending |

## Limitaciones

- No hay un repositorio GitHub del usuario conectado a este workspace; el usuario proporcionará
  la URL raw del catálogo que administre. Los enlaces de descarga del catálogo pueden apuntar a
  GitHub Releases o a URLs HTTPS compatibles.
