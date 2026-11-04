# Selector de archivos interno para TV

Android TV no incluye `DocumentsUI` (la app “Archivos” que responde a
`ACTION_OPEN_DOCUMENT`). En esos equipos el único handler es
`com.google.android.tv.frameworkpackagestubs.Stubs$DocumentsStub`, que no abre nada.
Por eso el launcher incorpora su propio explorador.

## Comportamiento

- **Temas** → Personalización → *Importar tema ZIP* abre el explorador filtrado a `.zip`.
- **Apariencia** → *Elegir imagen de fondo* abre el explorador filtrado a `png`, `jpg`,
  `jpeg` y `webp`.
- **Editor de temas** → *Importar imágenes* usa el mismo explorador.
- Navegación con D-pad: carpetas primero, “Subir un nivel”, y accesos directos a
  Descargas, Imágenes, almacenamiento interno, USB y archivos de la app.
- *Temas incluidos* sigue accesible desde el explorador de temas.
- *Selector del sistema* solo aparece si existe un proveedor real (no el stub).

## Permisos

- El permiso se solicita solo al abrir el explorador de archivos, nunca al arrancar ni al
  reconectar el equipo; así el launcher no se interrumpe.
- Una vez concedido se recuerda y no vuelve a preguntarse; si se deniega, el explorador
  ofrece el botón *Conceder acceso* sin cerrarse.
- El explorador muestra un aviso con el botón *Conceder acceso* cuando solo puede ver las
  carpetas de la app.
- Android 11+ puede concederse *Acceso a todos los archivos* (`MANAGE_EXTERNAL_STORAGE`)
  desde ese botón; Android 13+ lo usa para explorar carpetas públicas, ya que
  `READ_EXTERNAL_STORAGE` dejó de aplicar. `READ_MEDIA_IMAGES` queda declarado para
  lecturas de imágenes.
- Al volver de Ajustes se refrescan los accesos sin cerrar el diálogo.
- Si se conecta o desconecta un USB mientras el explorador está abierto, se refrescan las
  carpetas sin cerrarlo.

## Carpetas

- Con acceso concedido: Descargas, Imágenes, almacenamiento interno y volúmenes USB.
- Sin acceso: solo las carpetas propias de la app, que siempre están disponibles.
- `Android/data/com.tv.launcher.custom/files` es accesible por ADB sin permisos:

```sh
adb push tema.zip /sdcard/Android/data/com.tv.launcher.custom/files/
adb push fondo.png /sdcard/Android/data/com.tv.launcher.custom/files/
```

## Imágenes de fondo

La imagen elegida se decodifica, se limita a 1920 px y se guarda como PNG en
`files/backgrounds/`. Así el fondo sigue disponible aunque el archivo original se mueva
o el USB se desconecte.

## Límites

- Los temas se validan igual que antes (ZIP de hasta 20 MiB, manifiesto y assets).
- En Android 13+ el acceso directo a carpetas públicas está restringido por scoped
  storage; el explorador interno puede no listar `Download` y se apoya en las carpetas
  de la app o en el selector del sistema.
- La importación de assets del editor es de una imagen por vez; se puede repetir.
