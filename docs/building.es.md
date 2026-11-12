# Compilar Talkies

*(Este documento en: [English](building.md) · español)*

Para modificar Talkies, o instalarlo desde el código fuente. Si solo quieres instalarlo, es más simple el APK
listo del [README](../README.es.md#instalación). Todo lo que hace Talkies está en la [guía](guide.es.md).

- [Qué necesitas](#qué-necesitas)
- [Conectar con el TV](#conectar-con-el-tv)
- [Compilar e instalar](#compilar-e-instalar)
- [Instalar a mano](#instalar-a-mano)
- [Scripts](#scripts)
- [Firma y versiones publicadas](#firma-y-versiones-publicadas)
- [En Windows](#en-windows)

## Qué necesitas

| | |
|---|---|
| **El TV** | Android TV o Google TV, **Android 8.0 (API 26) o posterior**. El script de instalación lo comprueba antes de compilar. |
| **Un ordenador** | En la misma Wi-Fi que el TV. macOS y Linux ejecutan los scripts tal cual; en Windows ejecútalos en Git Bash (mira [En Windows](#en-windows)), o usa las órdenes de [Instalar a mano](#instalar-a-mano). |
| **Herramientas** | `adb` (Android platform tools), JDK 17 y las Android SDK command-line tools con la plataforma 35. |

Instala las herramientas en macOS con [Homebrew](https://brew.sh):

```bash
brew install openjdk@17
brew install --cask android-platform-tools android-commandlinetools
sdkmanager "platforms;android-35" "build-tools;35.0.0"
```

En Linux, instala el JDK 17 y `adb` con tu gestor de paquetes y las
[command-line tools](https://developer.android.com/studio#command-line-tools-only) de Google, y ejecuta la misma
línea de `sdkmanager`. Si los tuyos no están en las rutas de Homebrew, define `JAVA_HOME` y `ANDROID_HOME`. La
primera compilación descarga Gradle, así que tarda unos minutos.

## Conectar con el TV

1. **En el TV**, activa las opciones de desarrollador (Settings → Device Preferences → About → pulsa **Build**
   siete veces) y después **USB debugging** / **Network debugging** en Developer options. Apunta la dirección IP
   del TV (Settings → Network & Internet → tu red Wi-Fi), algo como `192.168.0.105`.

   **¿Sin Network debugging, solo Wireless debugging?** Los TV más recientes (Android 11 o posterior) a veces solo
   ofrecen *Wireless debugging*, que hay que emparejar una vez. Ábrelo en el TV, elige **Pair device with pairing
   code** y, en el ordenador, ejecuta `adb pair` con la dirección y el código que muestra (el puerto de emparejamiento
   es distinto del de conexión):
   ```bash
   adb pair 192.168.1.20:37123
   ```
   Después usa en los pasos de abajo la dirección y el puerto que aparecen dentro de **Wireless debugging**
   (no el 5555).

   > **Todas las `192.168.1.20` de este doc son un ejemplo.** Pon en su lugar la dirección de tu TV; `:5555` se
   > queda igual (es el puerto que usa network debugging).
2. **Conecta una vez** y acepta "Allow debugging?" en el TV:
   ```bash
   adb connect 192.168.1.20:5555
   ```
3. **Dile a los scripts qué TV** (este archivo se queda en tu ordenador; git lo ignora):
   ```bash
   echo 192.168.1.20:5555 > .tv
   ```
   Para las órdenes `adb` de esta documentación, exporta también `TALKIES_TV=192.168.1.20:5555`.

## Compilar e instalar

1. **Elige tu dock** en `app/src/main/res/values/dock.xml` (nombres de paquete; los tuyos los listas con la orden
   de [Reordenar apps](guide.es.md#reordenar-apps)). Después puedes reordenarlos en el TV.
2. **Compila e instala**: `./build-and-install.sh`. Deja Talkies como pantalla de inicio, concede los permisos que
   puede (ubicación para el nombre de la Wi-Fi, lista de canales para Watch Next) y restaura las claves que
   guardaron los scripts de claves.
3. **En el TV**, completa la [configuración inicial](../README.es.md#configuración-inicial) (Accessibility y acceso a las
   notificaciones).
4. **Las claves y tu zona del tiempo**: en el TV, en Talkies settings → **Keys and location**, o desde aquí:
   ```bash
   ./set-tmdb-key.sh                    # picks (paste the key when it asks)
   ./set-location.sh "Camden, London"   # weather: a place, or "lat,lon"
   ```
   Las claves se guardan en el TV y en archivos junto a los scripts que git ignora; tu ubicación solo en el TV.
   Ninguna de las dos está en la app ni en el repositorio.

## Instalar a mano

Lo que hace `build-and-install.sh`, una orden cada vez (y el camino también sin Git Bash, donde `gradlew.bat` es la
misma compilación).

```bash
./gradlew assembleRelease                                   # → app/build/outputs/apk/release/app-release.apk
adb connect 192.168.1.20:5555
adb -s 192.168.1.20:5555 install -r app/build/outputs/apk/release/app-release.apk
adb -s 192.168.1.20:5555 shell cmd package set-home-activity --user 0 com.techpp.glass/.HomeActivity
adb -s 192.168.1.20:5555 shell pm grant com.techpp.glass android.permission.ACCESS_FINE_LOCATION   # Wi-Fi name
adb -s 192.168.1.20:5555 shell pm grant com.techpp.glass android.permission.READ_TV_LISTINGS       # Watch Next
```

Las claves opcionales son archivos de texto plano que la app lee de su carpeta en el TV:

```bash
adb -s 192.168.1.20:5555 shell mkdir -p /sdcard/Android/data/com.techpp.glass/files
adb -s 192.168.1.20:5555 push tmdb_key.txt /sdcard/Android/data/com.techpp.glass/files/
```

**Solo Google TV:** deja activados su launcher y su setup wizard (`com.google.android.tungsten.setupwraith`), aunque
los dos responden a Home: el interruptor de Accessibility de Talkies mantiene el botón Home en Talkies. El
salvapantallas necesita el wizard para tus fotos de Google Photos. Más en la guía:
[El asistente de configuración de Google TV](guide.es.md#el-asistente-de-configuración-de-google-tv).

## Scripts

```bash
./build-and-install.sh                 # optimised build (about 260 KB, no libraries), install, set as home
./restore.sh                           # the TV's own home screen back, Talkies removed
./restore.sh --keep-talkies            # the TV's own home screen back, Talkies kept
./switch-home.sh list|talkies|stock    # choose the home screen (see the guide)
./set-region.sh GB                     # use another country for picks and scores, like Talkies settings → Country (--remove: the TV's own)
./set-tmdb-key.sh / set-location.sh   # key and weather area (--remove clears one)
./backup-settings.sh                   # copy Talkies' settings from the TV to talkies-backup.json here
./restore-settings.sh                  # put them back (after reinstalling, or on another TV)
./release-apk.sh                       # maintainer only: attach the signed talkies.apk to this version's release
```

Todos los scripts aceptan la dirección del TV como último argumento opcional; si no, usan `TALKIES_TV` o el archivo
`.tv`. El script de compilación usa las rutas de Homebrew salvo que `JAVA_HOME` / `ANDROID_HOME` digan otra cosa
(en Windows decide `android-env.sh`: mira [En Windows](#en-windows)).

**Guarda tus ajustes:** `./backup-settings.sh` guarda los ajustes de Talkies, el dock y el orden de las apps, las
apps ocultas, las teclas del mando, los canales, las ligas de fútbol, las claves y la zona del tiempo (y tu
`wallpaper.jpg`) en `talkies-backup.json`, junto a los scripts. Tras desinstalar, o en otro TV, instala Talkies,
ábrelo una vez y ejecuta `./restore-settings.sh`: arranca de nuevo con todo como estaba. Las recomendaciones, las
valoraciones y el tiempo no están en el archivo; se vuelven a descargar. El archivo guarda tus claves, así que git
lo ignora; mantenlo en privado.

**Pantallas:** el diseño se dibuja para una pantalla de 960 × 540 dp y se escala solo al ancho de cualquier TV, así
que ocupa la pantalla igual sea cual sea la densidad que reporte el TV.

## Firma y versiones publicadas

- **Tu compilación** va firmada con la clave de depuración de tu ordenador. Una compilación de otro ordenador, o el
  APK de la release, no se puede instalar encima: desinstala primero (antes guarda tus ajustes). Tampoco puede
  recibir actualizaciones desde Talkies settings → Updates; solo se le avisa cuando sale una versión nueva.
- **El APK de la versión publicada** (`talkies.apk` en cada release) va firmado con la clave del proyecto, que solo
  tiene el mantenedor (`release.properties`, nunca en git). Talkies solo instala actualizaciones firmadas con esa
  clave.
- **Hacer una release** (mantenedor): sube `versionCode` y `versionName` en `app/build.gradle` y en el CHANGELOG,
  escribe en `app/src/main/assets/whats_new.md` las notas breves que Talkies muestra tras la actualización (un
  nombre en negrita, una frase, dónde encontrarlo),
  commitea, crea la etiqueta `vX.Y.Z`, haz push, `gh release create vX.Y.Z` y después `./release-apk.sh` compila,
  comprueba la firma y adjunta `talkies.apk`.
- Talkies usa un servicio de accesibilidad, que es válido para sideloading pero Google Play no lo permite para este
  fin.

## En Windows

Los scripts corren bajo [Git Bash](https://git-scm.com/downloads/win) (no con `cmd` ni PowerShell) y no hacen falta
WSL ni `/opt/homebrew`.
`android-env.sh`, nuevo y en la raíz, detecta el JDK y el SDK según el sistema operativo (el JDK 17 de Microsoft por
la `java` del PATH, el SDK en `C:\Android` o donde lo tengas) y escribe `local.properties` si falta; lo que tú
pongas en `JAVA_HOME` / `ANDROID_HOME` manda sobre todo eso.

- Los `.sh` están forzados a LF con un `.gitattributes`. Si el árbol aparece "modificado" sin que hayas tocado
  nada, es la caché de `stat` de Git para Windows: no hay cambios reales (`git diff` sale vacío).
- `release-apk.sh` busca `apksigner.bat` en Windows y elige los build-tools por versión, no por orden lexicográfico.
- `release-apk.sh` se niega a publicar si el árbol no está commiteado, si `CHANGELOG.md` no tiene la sección de la
  versión, si el `versionCode` no ha subido o si el release de GitHub no apunta al commit que se compila
  (`--force` para saltarse esos comprobadores).
- `restore.sh` pregunta antes de desinstalar (`--yes` para scripts) y `switch-home.sh` / `restore.sh` ya no definen
  una función llamada `sh`, que en Git Bash tapaba la real.
- La CI de `.github/workflows/build.yml` compila la debug y la release (la de minificación) y comprueba la versión
  y el CHANGELOG.
