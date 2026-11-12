# Guía de Talkies

*(Este documento en: [English](guide.md) · español)*

Todo lo que hace Talkies, pantalla a pantalla. Para instalarlo y para el arranque de la primera vez, mira
el [README](../README.es.md); para compilarlo tú mismo, [building.es.md](building.es.md).

**Los scripts de esta guía** (`./set-tmdb-key.sh`, `./set-location.sh`…) se ejecutan en un ordenador con el código
fuente de Talkies y `adb` (mira [building.es.md](building.es.md)); `$TALKIES_TV` es la dirección de tu TV, como
`192.168.1.20:5555`. Casi todo lo que hacen también puede hacerse en la TV, desde Talkies settings.

- [Controles del mando](#controles-del-mando)
- [Control Center](#control-center)
- [Talkies settings](#talkies-settings)
- [Tendencias y New in …](#tendencias-y-new-in-)
- [Continue](#continue)
- [Canales](#canales)
- [El tiempo](#el-tiempo)
- [Cricket](#cricket)
- [Fútbol](#fútbol)
- [Mantener OK: menús de apps y de Continue](#mantener-ok-menús-de-apps-y-de-continue)
- [Reordenar apps](#reordenar-apps)
- [Cambiar el fondo de pantalla](#cambiar-el-fondo-de-pantalla)
- [Usar otra pantalla de inicio](#usar-otra-pantalla-de-inicio)
- [Google Assistant y el launcher de Google TV](#google-assistant-y-el-launcher-de-google-tv)
- [El asistente de configuración de Google TV](#el-asistente-de-configuración-de-google-tv)

## Controles del mando

| Dónde estás | Arriba | Abajo | Izquierda / Derecha | OK | Atrás |
|---|---|---|---|---|---|
| Botones de iconos | — | Pestañas (o el dock cuando no hay sugerencias) | Siguiente botón; se para en los extremos | Abrir | Dock |
| Pestañas | Botones de iconos | Sugerencias | Cambiar de pestaña; se para en los extremos | Sugerencias | Dock |
| Sugerencias | Pestañas | Dock | Siguiente sugerencia (crece, la fila se desliza); se para en los extremos | Abrirla | Dock |
| Dock | Sugerencias (o los botones de iconos) | Primera fila de canales (o All apps) | Siguiente app; tras la última, el botón redondo **All apps** | Abrir (mantener: dock, Move, Change image, Hide, Uninstall, App info); en el botón All apps, saltar a All apps | — |
| Canales | Fila de arriba (el dock desde la primera) | Fila de abajo (All apps tras la última) | Siguiente tarjeta; la fila se desliza | Abrir ese título | Dock |
| All apps | Una fila arriba (la última fila de canales, desde la fila de arriba) | Una fila abajo | Siguiente app de la fila | Abrir (mantener: dock, Move, Change image, Hide, Uninstall, App info) | Dock |
| Inputs / Control Center / ajustes | Anterior (desde el primero, el último) | Siguiente (desde el último, el primero) | Cerrar | Elegir | Cerrar |

**Home** siempre te devuelve al dock, arriba, en la pestaña Tendencias. **Mantener Home** abre el panel de Google TV
(Screensaver, Inputs, Picture, Wi-Fi…), igual que sin Talkies.

**Sonidos de navegación:** Talkies reproduce los sonidos propios de Android de clic y de movimiento cuando el
interruptor **System sounds** de la TV está encendido (Settings → Display & sound, o Sound), y guarda silencio cuando
está apagado, como el resto de la TV.

## Control Center

Sube hasta los botones de iconos y elige el primero (los deslizadores). Tiene lo que usas mientras ves algo; todo lo
que se ajusta una vez está en **Talkies settings**, arriba.

| Fila | Qué hace OK |
|---|---|
| Fix Home and Turn off | Solo cuando el servicio de accesibilidad de Talkies se ha parado (mira más abajo): abre Accessibility, donde apagas y vuelves a encender Talkies |
| Talkies settings | Abre Talkies settings (más abajo) |
| Now playing | Solo mientras una app reproduce (o ha pausado) algo: la canción o el programa, su imagen, el artista y la app, y después **Pause** / **Play**, **Next**, **Previous** (cuando la app permite saltar) y **Open** la app. Sigue al reproductor cuando cambian las pistas |
| Weather | El tiempo actual a la derecha; OK muestra las seis horas siguientes, y OK otra vez los seis días siguientes (máxima / mínima y el estado del cielo) |
| Sleep timer | Pasa de Off → 30 → 60 → 90 min; el tiempo que queda se muestra a la derecha. Un minuto antes, una tarjeta hace la cuenta atrás sobre lo que esté sonando: **OK** da 15 minutos más |
| Turn off now | Pone la TV en standby |
| Recent apps | Tarjetas grandes de las apps que abriste con Talkies (dock, All apps, sugerencias, canales, botones del mando) o de las que saliste con Home, la más nueva primero. Una tarjeta muestra el logo de la app sobre su color. Izquierda y derecha mueven, OK abre, Atrás cierra. Abajo (o Menu, o mantener OK) ofrece **Remove from list** y **App info**, donde Android mantiene Force stop. **Pulsa Home dos veces rápido seguidas** para abrirla, desde Talkies o desde dentro de una app (necesita Remote keys en On). También en un botón del mando: Remote keys → Recent apps |
| Storage | Muestra el espacio libre ("12.4 GB free of 32 GB"); OK abre los ajustes de almacenamiento de la TV |
| Animations | Recorre la velocidad de todas las animaciones de la TV: 1× → 0.5× → Off. Más rápido hace que los menús y los cambios de app se noten más ágiles; Off además quieta el movimiento propio de Talkies. Necesita el permiso `WRITE_SECURE_SETTINGS` (mira [Google Assistant](#google-assistant-y-el-launcher-de-google-tv)) |
| Network | Muestra el nombre de la Wi-Fi (o Ethernet); OK abre los ajustes de Wi-Fi |
| Speed test | Unos segundos de descarga del speed test de Cloudflare; muestra p. ej. "206 Mbps" |
| Bluetooth | Muestra el altavoz o los auriculares conectados. OK lista los que emparejaste: OK en uno lo conecta o lo desconecta; **Pair a new device** abre los ajustes de Bluetooth de la TV. Bluetooth no puede apagarse aquí, porque muchos mandos lo usan |
| Sound output | Muestra *TV speakers*, o *Soundbar connected* cuando hay una barra en HDMI ARC (Android no dice si es ella la que reproduce), y abre los ajustes de sonido de la TV: en Sony, Display & sound, donde Audio output elige los altavoces |
| Screensaver | Arranca el salvapantallas de la TV enseguida (el de Talkies, si lo elegiste: mira más abajo), igual que Google Assistant (o por el atajo propio de Android). No se muestra en dispositivos que tampoco ofrecen apps |
| Restart… | Abre el menú de encendido propio de la TV, que pregunta antes de reiniciar. En algunas TVs (OnePlus) ese menú duerme la TV a los 10 segundos si no eliges nada; Atrás lo cierra |

## Talkies settings

Control Center → **Talkies settings**, o **mantener OK sobre el icono ⚙** (una pulsación sigue abriendo los ajustes de
la TV).

| Fila | Qué hace OK |
|---|---|
| Finish setup | Solo mientras queda un paso de la primera configuración: los lista (el interruptor de Accessibility para el botón Home, el acceso de notificaciones, el nombre de la Wi-Fi y los permisos de Watch Next), y OK abre el interruptor de cada uno o pregunta |
| **Look** | |
| Appearance | Pasa de Auto (claro de día, oscuro de noche) → Light → Dark. Con un fondo de pantalla, Auto elige más bien el aspecto que combina con la foto, y cada aspecto añade justo el velo detrás del texto para que se lea |
| Wallpaper | Una foto tuya **from your phone** (un código QR) o **from this TV** (fotos en la TV o en un pendrive USB, o una carpeta para una foto nueva cada día), la **picture of the day** de Bing (se comprueba cada pocas horas mientras Talkies está en primer plano), o **Remove your picture**. Mira [Cambiar el fondo de pantalla](#cambiar-el-fondo-de-pantalla) |
| Background | Sin fondo de pantalla, qué hay detrás de Home. **App colour** (por defecto): un degradado suave en el color de la app enfocada. **Blurred art**: la propia foto de la sugerencia enfocada o de la tarjeta de Continue, desenfocada; las apps del dock conservan su color. *Not in use* con un fondo de pantalla o con Effects en Lite |
| Screensaver | **Clock** (por defecto), **Board**, **Wallpaper** o **Picks**: lo que muestra el salvapantallas de Talkies ([Salvapantallas y lo que está sonando](#salvapantallas-y-lo-que-está-sonando)). *Not in use* hasta que eliges Talkies en los ajustes Screen saver de la TV; *none set* en Wallpaper sin fondo de pantalla; *may be slow* en Picks en una TV con poca memoria |
| TV's screensaver | **Talkies** o **Not Talkies**: qué salvapantallas usa la TV. OK lo cambia con el permiso `WRITE_SECURE_SETTINGS`; sin él, muestra cómo elegirlo |
| Screensaver message | Dos líneas cortas para el salvapantallas ("Cena a las 8"), escritas en tu teléfono: un código QR abre la página, que muestra el mensaje tal cual. Envía las dos líneas vacías para quitarlo |
| Quiet hours | Cuándo se atenúa el salvapantallas: **11 PM – 7 AM** (por defecto), 10 PM – 6 AM, Midnight – 7 AM, u Off |
| Focus | Cómo se ve el mosaico o la tarjeta seleccionada. **Outline** (por defecto): crece y se eleva sobre una sombra suave, con una línea fina de luz alrededor. **Lift**: solo la elevación. **Ring**: un anillo blanco grueso, más fácil de ver sobre un fondo de pantalla recargado |
| Effects | **Auto** (por defecto): Lite en una TV con menos de 1,6 GB de memoria, o en una que resulte lenta (Talkies mide sus fotogramas las primeras veces que te mueves con las flechas), Full en el resto. **Full**: el fondo brilla en el color de la app enfocada y el foco y el desplazamiento se deslizan. **Lite**: sin brillo, y el foco y el desplazamiento se mueven de golpe, lo que mantiene ágiles las TVs lentas |
| RAM and storage | Shown (por defecto) u Hidden: los dos medidores junto a los botones de iconos, con la RAM libre y el almacenamiento libre, actualizados cada minuto |
| Apps open from their tile | Con On por defecto: la ventana de una app crece desde el mosaico o la tarjeta que elegiste. Es la animación de apertura normal de Android, empezada desde el mosaico, así que no cuesta nada extra |
| **For You and scores** | |
| Country | **Automatic** (donde está la TV, por su zona horaria), unos 25 países habituales, o **More countries** (de la A a la Z): el país para el que son las sugerencias y los marcadores; mira [¿Qué país?](#tendencias-y-new-in-) |
| For You | Shown u Hidden. Con Hidden desaparecen las pestañas y las sugerencias (el tiempo, la hora y la fecha se quedan, más grandes), Arriba desde el dock pasa a los iconos, y no se piden sugerencias a TMDB |
| Apps on Home | Solo con For You en Hidden. Shown pone All apps encima del dock, dos filas de cinco, y Abajo desplaza al resto. Hidden (por defecto) deja ese espacio vacío, con All apps debajo del dock |
| Picks from | Solo en India: TMDB o StreamRadar, de dónde vienen las sugerencias ([En India sin clave](#en-india-sin-clave)) |
| Picks languages | Solo mientras For You está en Shown: **All** (por defecto) o los idiomas que elijas para Tendencias y New in …. Mira [Idiomas](#idiomas) |
| Hide spoilers | Off (por defecto) u On. Con On: un episodio que aún no has empezado muestra la imagen propia de la serie en Continue en lugar del fotograma de la app de ese episodio, o el fotograma desenfocado cuando la app no da otra cosa. Los episodios a media ver mantienen su fotograma |
| Sharp YouTube pictures | Con Off por defecto. Con On: las tarjetas de YouTube en Continue reciben las miniaturas de 1280 píxeles de YouTube, buscadas por título y canal |
| Sports | Cricket, Football, NBA y NFL, cada una en On u Off: lo que muestran la línea de marcador y las filas de marcadores. Football para empezar, cricket también donde se siga mucho, además de la NBA y la NFL en Estados Unidos y Canadá |
| Football leagues | Solo con Football en On: las ligas que sigue la fila de Football; mira [Fútbol](#fútbol) |
| Continue apps | Cada app con algo que continuar: Shown u Hidden, para la pestaña Continue y la fila Watch Next |
| Channels | Elige qué canales de apps se muestran debajo del dock, y su orden |
| **Apps and inputs** | |
| Dock apps | Todas las apps, la primera del dock (numerada): OK pone una en el dock o la saca, igual que **Add to dock** y **Remove from dock** cuando mantienes OK sobre una app. Para cambiar el orden, mantén OK sobre una app del dock y elige **Move** |
| Dock size | 4, 5, 6, 7 (por defecto), 8, 9 o 10 apps. Los mosaicos crecen o se encogen para que el dock ocupe siempre el ancho de la pantalla; las apps que ya no caben pasan al principio de All apps, y vuelven cuando el dock vuelve a crecer |
| All apps button | **Shown** (por defecto) u Hidden: el botón redondo del final del dock. Con Hidden, las apps del dock llenan su ancho; All apps sigue estando más abajo en la página |
| All apps size | 6 (por defecto), 5 o 4 por fila. Menos por fila son mosaicos más grandes |
| All apps order | **Your order** (por defecto: las apps que moviste con mantener OK → Move se quedan, el resto de la A a la Z), **Recently opened** (primero las apps que abriste por última vez con Talkies, el resto de la A a la Z) o **A to Z**. En los dos órdenes con ordenación solo se pueden mover las apps del dock; el dock siempre conserva tu orden |
| Hidden apps | Las apps que ocultaste, y los manuales, los actualizadores, las demos de tiendas y las herramientas de fábrica que instalan los fabricantes de TVs (ocultos desde el principio; la lista viene de Home Deck); OK devuelve una |
| Phone apps | Solo en una TV con alguna (con cuántas): **Shown** (por defecto) u Hidden: las apps sin versión para TV (Chrome, MacroDroid… instaladas desde un archivo) en All apps, con un mosaico hecho desde su icono. Hidden las deja fuera, para las TVs que así muestran pantallas raras del sistema; mantener OK → **Hide** oculta solo una |
| Inputs on Home | Solo en una TV con puertos HDMI o AV: cada puerto, con el dispositivo que hay en él cuando lo dice (*HDMI 2 · PlayStation 5*). OK lo pone en Home como un mosaico en el primer sitio del dock, con el nombre de ese dispositivo, que cambia la TV a él de un solo pulsado; OK otra vez lo quita. Mantén OK sobre el mosaico para moverlo, ponerle una foto, o **Remove from Home** |
| Edit inputs | Solo en una TV con puertos HDMI o AV: cada puerto y Live TV. OK en uno: **Name** (el tuyo, como "Caja del salón"; Remove lo devuelve al suyo) y **In Inputs** (Shown u Hidden: el puerto ARC de una barra de sonido, o Live TV sin antena). Los ocultos siguen fuera de Inputs, de Inputs on Home y de At power-on |
| A device turns on | **Offer to switch** (por defecto) o **Nothing**. Con Home en pantalla, una consola o un box en HDMI que se enciende recibe una tarjeta pequeña: "PlayStation 5 is on · Switch to HDMI 2". Se va a los 15 segundos. Muchos dispositivos cambian la TV a sí mismos de todas formas (HDMI-CEC); esto es para los que no |
| Empty inputs | **Shown** (por defecto), o **Under More inputs**: Inputs lista solo los puertos con algo conectado, y una fila More inputs devuelve el resto |
| **Remote and power** | |
| Remote keys | Dale a un botón del mando otro trabajo: abrir una app, ir a una entrada, Previous app, Resume watching (la primera tarjeta de Continue), Inputs, Recent apps, Control Center, Talkies settings, TV settings, Wi-Fi, Sound output, Bluetooth, Play / pause, Mute, Screensaver, Turn off, Talkies, o nada. **Add a key**, pulsa el botón, elige. Cuando ya tiene un trabajo, **When held** le da un segundo para mantenerlo pulsado (más de medio segundo). Navegación, OK, Atrás, Home, volumen y encendido no se pueden cambiar. Los botones propios de las marcas muestran su nombre cuando Talkies conoce el mando (Sony, TCL, Hisense, Philips, Xiaomi, Google TV Streamer); cualquier otro botón para el que Android no tenga nombre se muestra como *Button 0x…* (su código del mando). En Sony, cada botón de app puede tener su propio trabajo, aunque el mando envíe a Android una tecla para varios de ellos. Si un botón sigue haciendo solo su propio trabajo mientras Talkies lo espera (los botones de apps de algunas TVs), el software de la TV lo gestiona antes de que Android lo vea, y ninguna app puede cambiarlo. **Remote keys: On u Off** arriba, con On por defecto: Home va directo a Talkies. En algunas TVs, mientras está en On, mantener Volume lo cambia solo un paso por pulsación, en todas las apps: Android deja de repetir un botón mantenido en cuanto una app vigila el mando, y la TV actúa sobre Volume antes de que cualquier app lo vea, así que ninguna app puede repetirlo tampoco |
| At power-on | Qué aparece cuando la TV se enciende. **Talkies, after a restart** (por defecto; tras el standby la TV muestra su última entrada o app), **Talkies, every time**, una de las entradas de la TV (*HDMI 1 · Set-top box*), o **Open an app**: esa entrada o app sale tras un reinicio y cada vez que la TV despierta. Pulsar un botón mientras despierta deja lo que esté en pantalla. Necesita el interruptor de Accessibility |
| Home app | Muestra la pantalla de inicio actual y abre el selector *Default home app* de Android (en Google TV la elección no se queda; mira [Usar otra pantalla de inicio](#usar-otra-pantalla-de-inicio)) |
| Control Center shortcuts | Qué pantalla abren Network, Bluetooth, Sound output y Storage en Control Center: la predeterminada de la TV, o cualquiera que ofrezcan sus apps de ajustes (primero las que más coinciden, **Show every screen** para el resto). Útil cuando la TV guarda la página útil en otro sitio |
| **Talkies** | |
| Keys and location | Tu clave de TMDB (las sugerencias) y el área del tiempo. **Send from your phone** muestra un código QR: escanéalo con un teléfono en la misma Wi-Fi y pega ambas en la página que abre (funciona una vez, durante 5 minutos, solo en tu red de casa, y las claves se quedan en la TV). O escribe o pega cada una; las claves se prueban primero con el servicio y solo se guardan si lo acepta. **Remove** borra una |
| Updates | La versión instalada, y si hay una release más nueva (se comprueba una vez al día mientras Talkies está en primer plano). OK comprueba ahora y, con el APK de las releases, descarga el nuevo, lo comprueba (el tamaño y el SHA-256 de GitHub, que sea más nuevo, firmado con la clave del proyecto) y se lo pasa al instalador de Android, que pregunta primero; la primera vez Android también pide permitir que Talkies instale apps. A un Talkies compilado en un ordenador solo se le avisa de que hay una versión nueva |
| Network debugging | "On · port 5555" mientras los ordenadores de tu red pueden controlar la TV por ADB (así se instaló Talkies). **Turn off** (necesita el permiso de más abajo) o abre Developer options; volver a encenderlo se hace en Developer options |
| About | Créditos de los datos y del código que usa Talkies. **What's new** lista en notas cortas lo que cambió esta versión, con Updates arriba; tras una actualización Home muestra los primeros, una sola vez, con **See all changes**. **Report a problem** muestra un código QR que abre en tu teléfono un reporte de errores de GitHub con el modelo de la TV, las versiones de Android y de Talkies, la memoria y los ajustes de Accessibility y de Remote keys ya rellenados; desde la TV no se envía nada |

**Configuración única de Turn off now, el Sleep timer y la búsqueda de cricket:** esta TV no admite "device admin", así
que el launcher apaga la TV mediante un servicio pequeño de accesibilidad. El mismo servicio escribe un partido en la
búsqueda de JioHotstar cuando una tarjeta de Cricket no tiene enlace directo. Solo ve la pantalla de JioHotstar, solo
actúa durante 30 segundos después de que elijas una tarjeta, y solo busca la caja de búsqueda. Enciéndelo una vez en
**Settings → Device Preferences → Accessibility →
Talkies**. Hasta entonces, elegir Turn off o el Sleep timer te lleva a esa pantalla.

**Si el servicio se para:** Android puede parar el servicio (un fallo, o la memoria baja) mientras su interruptor sigue
pareciendo encendido, y entonces Home, Turn off y Remote keys dejan de funcionar en silencio. Talkies lo comprueba unos
segundos después de abrirse: con el permiso `WRITE_SECURE_SETTINGS` (mira [Google Assistant](#google-assistant-y-el-launcher-de-google-tv))
apaga y enciende el servicio él solo; sin él, una nota arriba de la pantalla de inicio lo dice, y Control Center
recibe una fila **Fix Home and Turn off**.

## Tendencias y New in …

La fila de sugerencias está encima del dock. Dos de sus pestañas vienen de TMDB, para el país de la TV:

- **Tendencias**: lo que es popular ahora mismo en tus apps de aquí: títulos de tu país mezclados con los de inglés,
  dos tuyos por cada uno en inglés (en India, títulos en hindi, tamil, telugu y otros de India junto a los de inglés;
  la lista de tendencias de TMDB es mundial y casi toda en inglés). En los países de habla inglesa (Estados Unidos,
  Reino Unido, Canadá, Australia, Nueva Zelanda, Irlanda) es simplemente lo que es popular allí. Los títulos que ya
  están en la pestaña "New in" se dejan fuera.
- **New in *tu país*** ("New in India", "New in the US"): las películas de tu país de los últimos 6 meses y las series
  con episodios nuevos en los últimos 60 días.

Ambas solo incluyen títulos que se emiten en streaming (incluidos en tu suscripción, no de alquiler o compra) en tu
país en una app instalada en esta TV: Netflix, Prime Video, Disney+ / JioHotstar, Apple TV, Max, Hulu, Peacock,
Paramount+, ZEE5, SonyLIV, MX Player, Xstream Play, FanCode, Crunchyroll o MUBI, o cualquier otra app instalada con un
nombre como el de un servicio de streaming que TMDB lista para tu país (Viaplay, Canal+, RTL+, Joyn, Stan, Crave…).

**¿Sin apps de streaming?** Sin ninguna de ellas instalada, las pestañas muestran igualmente el mismo tipo de títulos,
y OK reproduce el tráiler del título en YouTube. La línea debajo de la tarjeta dice dónde se ve ("Trailer · on
Netflix").

### Idiomas

Talkies settings → **Picks languages**. La lista ofrece los idiomas en los que llegan las sugerencias de tu país, los
más comunes primero, y el inglés (se añade después de la primera carga de las sugerencias). OK en uno muestra solo los
idiomas que elijas; OK otra vez lo deja fuera; **All languages** vuelve a todo. Las sugerencias se renuevan enseguida.

Con idiomas elegidos, Tendencias toma títulos populares en esos idiomas de cualquier sitio (una serie coreana que se
emite en Francia, no solo las producidas en Francia), dos por cada título popular en inglés si el inglés es uno de
ellos. New in … sigue queriendo decir hecho en tu país, en esos idiomas, así que puede quedar corto. Es el idioma
original del título: una película doblada al tamil sigue contando como hindi, y TMDB no distingue el portugués de
Brasil del europeo.

**¿Qué país?:** la zona horaria de la TV (donde está de verdad; muchas TVs se quedan en "English (United States)"
estén donde estén), y después su ajuste de idioma. Para usar las sugerencias de otro país, o si eso no te va bien:
Talkies settings → **Country** (o `./set-region.sh GB` desde un ordenador). Cambia las sugerencias, la pestaña
*New in …*, Picks from (solo India), qué equipos de cricket y qué ligas de fútbol van primero, y Soccer para Football
en Estados Unidos y Canadá. El °C o °F del tiempo y qué app tiene el cricket siguen mirando dónde está la TV.
Cada pestaña guarda hasta 24 y muestra 12 de ellas, y avanza de 4 en 4 cada hora, así que la tarjeta grande y la fila
cambian a lo largo del día y el mismo juego vuelve solo después de 6 horas. Las listas se renuevan como mucho cada
6 horas (los rankings de popularidad de TMDB cambian más o menos a diario, así que renovarlas más a menudo descargaría
casi siempre las mismas listas); una renovación tarda unos segundos. Debajo de la tarjeta grande: película o serie,
género, idioma (cuando no es inglés) y la app.
Las imágenes: la tarjeta grande usa el fondo de 1280 píxeles de TMDB y las tarjetas pequeñas el cartel del título, así
que ninguna se estira; solo la imagen a tamaño completo de la tarjeta grande se guarda en memoria.
**OK** en una sugerencia abre esa app.
TMDB no conoce el ID de cada título dentro de Netflix, Hotstar y el resto, así que la app abre en su pantalla de
inicio, no en el título.

### En India sin clave

Hasta que añadas una clave de TMDB, una TV en India saca sus sugerencias de [StreamRadar India](https://streamradar.bricksgenius.com),
una lista gratuita y abierta de lo que hay en las apps de India (una idea de Swakshan):

- **Tendencias**: el Top 10 de películas y series de cada app de esta TV (JioHotstar, Netflix, Prime Video, ZEE5,
  SonyLIV, Apple TV), tomadas de una en una.
- **New in India**: lo que acaba de salir en esas apps, lo más nuevo primero.
- **OK** abre la página propia del título en su app cuando StreamRadar tiene un enlace (comprobado en JioHotstar, ZEE5
  y SonyLIV), si no, la app.
- **¿Ninguna de esas apps en esta TV?** Sigues teniendo las listas, los Top 10 de las seis apps y lo nuevo, marcados
  "Trailer · on Netflix". OK busca en YouTube el tráiler del título. Sin YouTube tampoco, las pestañas quedan ocultas.

Las listas de StreamRadar no dan géneros ni idiomas, así que la línea debajo de la tarjeta es solo película o serie y
la app, y **Picks languages** no se ofrece. Añade una clave de TMDB y las sugerencias de TMDB toman el mando enseguida.

**Picks from** (Talkies settings, solo India) cambia entre las dos en cualquier momento: **TMDB** (necesita tu clave;
sugerencias en tus idiomas, con géneros) o **StreamRadar** (el Top 10 propio de cada app, y OK abre el título). Las
sugerencias se vuelven a pedir enseguida.

Las sugerencias vienen de [TMDB](https://www.themoviedb.org), que necesita una clave gratuita tuya (en India,
StreamRadar cubre el tipo hasta que añadas una):

1. Crea una cuenta en [themoviedb.org](https://www.themoviedb.org/signup) y abre
   [Settings → API](https://www.themoviedb.org/settings/api). Pide una clave de desarrollador (uso personal).
2. En la TV: Talkies settings → **Keys and location** → **Send from your phone**, escanea el código y pega el **API
   Read Access Token** (o la **API Key** más corta) en tu teléfono. O desde un ordenador, ejecuta el script de la clave
   y pégala cuando pregunte (no se muestra nada mientras pegas):

   ```bash
   ./set-tmdb-key.sh
   ```

3. Pulsa Home en el mando. Las primeras sugerencias tardan cerca de un minuto.

La clave se guarda en `tmdb_key.txt` aquí (legible solo por ti) y en la carpeta del launcher en la TV; nunca va dentro
de la app, y `build-and-install.sh` la devuelve tras una reinstalación. Para apagar la fila:
`./set-tmdb-key.sh --remove`. Si la fila no aparece, mira el registro:

```bash
adb -s "$TALKIES_TV" logcat -d -s GlassTrending
```

## Continue

La primera pestaña muestra hasta dieciséis títulos recientes: primero lo más nuevo de cada app que has usado en las dos
últimas semanas (para que una sola app no se quede con la cabeza de la fila), y después lo siguiente más nuevo. YouTube
recibe hasta cuatro tarjetas de los dos últimos días, dos si no; **OK** en una abre ese vídeo, o lo busca cuando Talkies
no lo ha emparejado del todo (Sharp YouTube pictures encuentra la coincidencia). JioHotstar solo informa "S1 E2 Allen"
mientras reproduce, así que un título que abriste desde su tarjeta de Continue conserva el nombre, la foto y el enlace
de la serie; uno que empezaste dentro de la app es una tarjeta simple que abre la app. Las tarjetas muestran hasta
dónde vas cuando la app lo comparte, y una tarjeta de YouTube que emparejaste abre el vídeo donde lo dejaste. Un título
pasado del 97% cuenta como terminado: sale de las tarjetas de Watch Next, y una tarjeta de lo que una app reprodujo se
queda, pero empieza desde el principio. Une tres fuentes:

- **La lista Watch Next de Android TV.** Netflix, Prime Video, JioHotstar, Apple TV y Xstream Play publican en qué
  estás, con la serie, la temporada, el episodio y su imagen. **OK** abre ese título o episodio exacto (probado con
  Netflix: abrió el episodio y lo reprodujo). Solo se usan las entradas "continue" y "next episode"; las de
  "new season" y las de la watchlist son promociones y se dejan fuera.
- **Lo que las apps informan mientras reproducen** (sesiones de medios), para apps que no usan Watch Next, como YouTube
  y ZEE5, y para apps cuyo Watch Next va retrasado: JioHotstar no siempre lo actualiza, así que también aparece un
  título que reprodujo más recientemente que su entrada más nueva de Watch Next. Watch Next se vuelve a leer cada vez
  que una app lo cambia (Netflix lo actualiza unos segundos después de que salgas), y la fila de canales Watch Next
  muestra esta misma lista.
- **Tarjetas marcadas en las filas propias de las apps.** La fila de canales de Netflix empieza con lo que estás
  viendo, marcado en el enlace y la descripción de la tarjeta, y una fila que tenga "continue", "keep watching" o
  "resume" en su nombre cuenta entera. Estas añaden títulos que Watch Next no tiene, y cuando el primero cambia, ese
  título pasa a la cabeza de Continue. Todo lo demás de esas filas es una promoción y se deja fuera. Con Home abierto,
  Talkies nota que una app cambia su fila y la vuelve a leer, como mucho una vez por minuto.
  Las imágenes: YouTube solo comparte una imagen de 640 píxeles mientras reproduce (sin ID del vídeo). Con Talkies
  settings → **Sharp YouTube pictures** en On (viene con Off por defecto), Talkies busca el vídeo en YouTube por título
  y canal, lo confirma por el endpoint oEmbed público de YouTube, y cambia la miniatura por la de 1280 × 720 (solo se
  envían el título y el canal; una vez por vídeo, mientras la pantalla de inicio está en primer plano). Esto lee la
  página de búsquedas de YouTube, que las condiciones de YouTube no invitan de verdad; por eso hay que activarlo. Las
  apps que no comparten imagen (JioHotstar) reciben una tarjeta en el color de la app con su logo, nítido, en el
  centro.
  Un título se añade tras 20 segundos de reproducción, así que se saltan los anuncios y los avances. **OK** abre la app.

De una app que publica en Watch Next se toma solo de ahí, así que no aparece dos veces. Las apps de música (Spotify,
YouTube Music) quedan fuera.

| App | Fuente | Qué ves |
|---|---|---|
| Netflix, Prime Video, Apple TV, Xstream Play | Watch Next | Serie, temporada y episodio con su imagen; abre ese episodio |
| JioHotstar | Watch Next | Igual que arriba (su reproducción solo informaba el nombre del episodio) |
| YouTube | Al reproducir | Título y miniatura del vídeo; abre YouTube |
| ZEE5 | Al reproducir | El título (p. ej. "Max") sobre la banda de ZEE5; abre ZEE5 |

**Permisos:** Watch Next necesita el permiso de *TV listings*: Talkies lo pide la primera vez que arranca (o Talkies
settings → **Finish setup**), y `build-and-install.sh` se lo concede.
Para los títulos "al reproducir", Android solo comparte lo que suena con una app que tenga *notification access*.
En la TV: **Settings → Apps → Special app access → Notification access → Talkies → Allow.** Muchas TVs con Android 8
no tienen esa pantalla: ahí, con el permiso `WRITE_SECURE_SETTINGS`, Finish setup lo enciende él solo; sin él, lo hace
`adb shell cmd notification allow_listener com.techpp.glass/com.techpp.glass.WatchService` desde un ordenador.
Talkies no lee notificaciones; usa ese acceso solo para ver las sesiones de medios de las apps (lo que suena), y
guarda la lista en la TV (`/sdcard/Android/data/com.techpp.glass/files/recent/`). Para pararlo, apaga el mismo
interruptor; para borrar la lista:

```bash
adb -s "$TALKIES_TV" shell rm -rf /sdcard/Android/data/com.techpp.glass/files/recent
```

## Canales

Filas debajo del dock, leídas de los canales que tus apps publican a Android TV (los mismos que muestra Projectivy),
más **Watch Next**. Cada fila muestra hasta 15 tarjetas, primero la última que viste cuando la app dice cuándo (Nuvio
lo hace; otras mantienen su propio orden); el título y la descripción de la tarjeta enfocada aparecen debajo, y **OK**
abre ese título en su app (por ejemplo, una tarjeta de JioHotstar abre la página de esa película). Las filas se
renuevan cada vez que se abre la pantalla de inicio.

**Elegir canales:** Talkies settings → **Channels** lista todos los canales que publican tus apps.

- **OK** muestra o esconde un canal.
- **Mantener OK**, después **Arriba/Abajo**, lo mueve; **OK** lo coloca, **Atrás** cancela. El primer canal que se ve
  es la fila de arriba.

Para empezar, se muestran Watch Next y la fila de pantalla de inicio propia de cada app (las filas que una app pide a
Android TV que muestre, o, cuando la TV no lo dice, como en Google TV, la primera fila de cada app); el resto empieza
oculto, y también los canales que una app añada después. Una TV que ya tuvo Talkies antes de esto conserva las filas
que tenía. Las imágenes se guardan en caché en la TV, en `files/channels/`.

## El tiempo

El tiempo aparece arriba a la izquierda, por ejemplo "27° SHOWERS", en °C, o en °F donde esa es la costumbre (Estados
Unidos y unas pocas más, según dónde esté la TV), después de un dibujo pequeño a línea del cielo: sol, luna, sol o
luna detrás de una nube, nube, lluvia, nieve, tormenta o niebla.
La parte del tiempo combina dos fuentes:

- **La temperatura** se mide en la **estación meteorológica del aeropuerto más cercano**. Los aeropuertos publican
  observaciones (METAR) cada 30–60 minutos; Talkies las lee del servicio gratuito del Servicio Meteorológico Nacional
  de Estados Unidos, [aviationweather.gov](https://aviationweather.gov), y elige el informe más cercano de las últimas
  dos horas dentro de unos 200 km (en una gran ciudad, suele ser el aeropuerto de la ciudad). Puede que haya estaciones
  más cercanas (servicios meteorológicos nacionales, estaciones personales), pero pocas publican gratis para apps.
- **La lluvia y las nubes** vienen del modelo de [Open-Meteo](https://open-meteo.com) para tus coordenadas exactas,
  porque un aeropuerto a 15–20 km puede estar seco mientras en tu casa llueve.

Cuando ahora no llueve pero es probable que llueva (50% o más) en las próximas 12 horas, la línea añade cuándo: "27°
SUNNY · RAIN AROUND 4 PM". Se va en cuanto llega esa hora.

Sin un informe reciente de aeropuerto, la temperatura también viene de Open-Meteo. Si Open-Meteo no responde, la
previsión de [MET Norway](https://api.met.no) cubre el estado del cielo, la nota de lluvia y la previsión (sin una
probabilidad de lluvia a la que agarrarse, una hora con lluvia cuenta como probable). Se renueva como mucho cada
30 minutos, y solo mientras la pantalla de inicio está visible.

**Sin lugar ajustado,** Talkies se guía por tu dirección de internet: pregunta a ipwho.is (o ipapi.co, o geojs.io) una
vez por semana, y Keys and location → **Weather location** dice "Near Bengaluru". Puede fallar una ciudad, o más con
una VPN. Poner un lugar lo sustituye para siempre; **Remove** ahí lo apaga y deja solo la hora y la fecha.

Ajusta tu área una vez, en la TV desde Talkies settings → **Keys and location** (o **Send from your phone**), o desde
un ordenador:

```bash
./set-location.sh "12.97,77.64"     # your latitude,longitude
```

Las coordenadas son lo mejor. Un nombre de lugar también funciona (`./set-location.sh "Indiranagar, Bengaluru"`),
buscado con el buscador de lugares de Open-Meteo; si no conoce la localidad exacta, usa el centro de la ciudad. Volver a
ejecutar el script renueva el tiempo enseguida; `./set-location.sh --remove` deja solo la hora y la fecha. La salida y
la puesta del sol de hoy salen de la misma petición a Open-Meteo y fijan cuándo la apariencia Auto cambia entre claro y
oscuro.

## Cricket

Mientras hay un partido en directo o de hoy, la línea debajo del tiempo muestra el más relevante, por ejemplo
"● LIVE  ODI · AFG v BAN 116/4 (29.5)" (primero el equipo de tu país, después otros internacionales, después los
nacionales), y aparece una fila **Cricket** en lo alto de las filas de canales: cada tarjeta lleva las banderas y los
marcadores de los dos equipos, el tipo de partido y el estado ("LIVE · Test · Day 4: 3rd Session…"); el pie debajo de
ella nombra la serie.

En el cierre de la jornada, en la pausa de la comida, en la del té o con la lluvia parada nadie juega, así que un
partido no se muestra como en directo: la línea pone "Test · AUS v RSA 329 · Day 1: Stumps", se sigue comprobando cada
minuto a ver si se reanuda, y un partido de fútbol en juego pasa primero (la fila Football queda entonces encima de
Cricket).

- **Tipo de partido:** Test, ODI o T20I en los internacionales; First-class, One-day o T20 en los nacionales.
- **Los Tests muestran todas las entradas:** "425/9d & 84/1 (25)" y "99": all out muestra las carreras, una declaración
  añade "d", y la entrada en curso muestra wickets y overs.

- **¿Qué partidos?** Internacionales entre selecciones (masculinos y femeninos; los equipos "A" y las ligas de
  franquicias en el extranjero quedan fuera) y las competiciones nacionales de tu país: en India la IPL, la WPL, la
  Ranji Trophy, la Syed Mushtaq Ali, la Vijay Hazare, la Duleep, la Irani, la Deodhar y la Cooch Behar; en Australia la
  Big Bash, la Sheffield Shield y la Marsh Cup; en Reino Unido la County Championship, The Hundred y la T20 Blast; y
  las ligas principales de Nueva Zelanda, Sudáfrica, Pakistán, Sri Lanka, Bangladés, el Caribe, los EAU y Estados
  Unidos.
- **OK** abre el streaming: si una de tus apps tiene una tarjeta del partido en su canal de TV (una que nombre los dos
  equipos), el enlace de esa tarjeta abre el partido en sí. Si no, fuera de India, la tarjeta dice que ninguna app lo
  tiene (los derechos varían demasiado por país para adivinar). En India se abre la app que tiene allí los derechos:
  JioHotstar para los partidos de la BCCI (internacionales en casa, nacionales, IPL) y los eventos de la ICC, FanCode
  para la Lanka Premier League, SonyLIV para la Asia Cup. En las giras de India fuera y las series de otros países los
  derechos varían; el recurso es JioHotstar, que puede no tenerlos. Cuando es JioHotstar, su búsqueda se abre con los
  dos equipos escritos ("Afghanistan vs Bangladesh"), así que el partido está a un clic si JioHotstar lo emite. Los
  enlaces de búsqueda de JioHotstar hacen caso omiso del texto, así que el servicio de accesibilidad de Talkies lo
  escribe (mira [Talkies settings](#talkies-settings)); con el servicio apagado, la página de búsqueda se abre vacía.
  La lista de resultados de JioHotstar incluye también resúmenes y repeticiones, y una serie que no emite solo muestra
  clips más viejos.
- **Datos:** el panel de marcadores público de ESPN (`site.web.api.espn.com/…/cricket/scorepanel`, sin clave; como el
  de fútbol, no es una API documentada), una petición por cada día (los días de ESPN son UTC) que toque tu día. Se
  renueva cada minuto mientras hay un partido y cada 30 minutos si no, solo mientras Talkies está en primer plano con
  la pantalla de la TV encendida. Los internacionales se distinguen por la clase de partido de ESPN (Test, ODI, T20I y
  los femeninos), así que los equipos A y los sub-19 quedan fuera. ESPN actualiza bola a bola; el marcador puede seguir
  yendo un minuto por detrás de la emisión. Gracias @Swakshan por señalarlo.

Para ocultar cricket, Talkies settings → **Sports** → Cricket. El registro muestra lo que encontró y a dónde apunta cada
botón Watch: `adb -s "$TALKIES_TV" logcat -d -s GlassCricket`.

## Fútbol

Football va como cricket, sin clave: la línea debajo del tiempo muestra el partido más relevante
("● LIVE  67'  ·  ARS 2–1 CHE"; un partido de cricket en directo pasa primero), y una fila **Football** con los escudos
de los dos clubes, el marcador y el minuto ("LIVE · Premier League · 67'"), la hora de inicio ("starts Wed 7:30 pm") o
el resultado.

- **¿Qué partidos?** en directo ahora, que empiezan dentro de un día, o terminados en las últimas 12 horas, de tus
  ligas.
- **Tus ligas:** Talkies settings → **Football leagues**. Cada país empieza con las suyas (India: Premier League,
  Champions League y Indian Super League; Reino Unido: Premier League, Champions League y Scottish Premiership; Estados
  Unidos: MLS, Premier League y Champions League; Turquía: Süper Lig, Champions League y Premier League…), más el Mundial
  y la Eurocopa cuando se juegan. Pulsa OK para encender una o apagarla; **Back to the defaults** deshace tus cambios.
- **Soccer:** en Estados Unidos y Canadá, donde football quiere decir la NFL, Talkies dice Soccer en su lugar (Sports,
  **Soccer leagues**, el nombre de la fila).
- **NBA y NFL:** Talkies settings → **Sports**, en cualquier sitio (en On para empezar en Estados Unidos y Canadá). Sus
  partidos se leen al estilo americano, el equipo visitante primero
  ("CHI at SEA", "8:28 - 4th"). La fila se llama **Football**, **NBA** o **NFL** cuando es todo lo que tiene, y
  **Sports** cuando es un mix.
- **Renovaciones:** cada 15 minutos, y cada minuto para una liga con un partido en curso, solo mientras Talkies está en
  primer plano.
- **Datos:** el marcador público de ESPN. Es gratis y no necesita clave, pero no es una API oficial: ESPN puede
  cambiarlo sin avisar, y entonces la fila de Football simplemente no aparece hasta que se actualice Talkies.
- **Verlo:** todavía no. Qué app emite qué liga cambia por país, así que OK en una tarjeta de partido por ahora lo dice.

## Salvapantallas y lo que está sonando

**El salvapantallas de Talkies:** un reloj atenuado (con los segundos, más pequeños), la fecha y el tiempo, que cambia
de sitio cada 20 segundos para que nada se queme (útil en TVs OLED). Elígelo en Settings → Device Preferences →
Screen saver de la TV → **Talkies**; no es el salvapantallas de la TV hasta que lo hagas. Detrás del reloj, con Talkies
settings → **Screensaver**:

- **Clock** (por defecto): negro.
- **Wallpaper**: tu fondo de pantalla (tu foto o el de Bing), atenuado, a la deriva y creciendo despacio para que no
  se quede quieto nunca.
- **Picks**: las imágenes de las sugerencias de Tendencias, una nueva cada minuto, con el título y la app debajo del
  reloj. Guarda dos imágenes en memoria, así que en una TV con menos de 1,6 GB la fila dice *may be slow*: Clock o
  Wallpaper pesan menos.
- **Board**: en lugar del reloj, una tablilla de letras que voltean, de 22 letras por 6 filas, como un tablero de
  salidas de una estación antigua: la hora al segundo, la fecha, el tiempo, y después lluvia que viene o la máxima y la
  mínima del día. Solo voltean las letras que cambian, cada una pasando por unas pocas de camino, y cada 15 segundos
  una onda desde arriba a la izquierda da la vuelta entera a todas las letras. Se mueve un poco cada 20 segundos, sobre
  negro puro. Una idea de [Departures](https://github.com/0xdravyn/departures).

**Screensaver message:** Talkies settings → **Screensaver message** muestra un código QR; en tu teléfono, escribe hasta
dos líneas de 22 letras ("Cena a las 8", "¡Feliz cumpleaños Asha!"), emoji incluidos. Board las muestra en ámbar abajo,
cada emoji en su propio mosaico; los otros salvapantallas las muestran debajo del reloj (Picks usa esa línea para el
título). Envía las dos líneas vacías para quitarlo.

**Quiet hours:** de 11 PM a 7 AM el salvapantallas se atenúa a cerca de un tercio. Talkies settings → **Quiet hours**
elige otras horas u Off.

Se enciende tras el tiempo de reposo de la TV, sobre cualquier app, y cualquier botón despierta la TV. Talkies settings
→ **TV's screensaver** hace que Talkies sea el salvapantallas de la TV si le diste el permiso
`WRITE_SECURE_SETTINGS`, y OK otra vez devuelve el que sustituyó. Sin el permiso, elígelo en los ajustes de la TV. Si
no lo enlistan (el modo Ambient de Sony, por ejemplo), desde un ordenador:
`adb shell settings put secure screensaver_components com.techpp.glass/.Screensaver`.

**What's playing:** mientras suena música (o cualquier otra cosa) de fondo, la línea debajo de los iconos muestra eso
en lugar del marcador: "♪ Song · Artist · Spotify". Los marcadores se quedan en sus filas, y la línea vuelve a ellos
cuando para la música. El Play/Pause del mando funciona como en cualquier lado; Control Center → Now playing tiene los
botones.

## Mantener OK: menús de apps y de Continue

**Mantener OK** (cerca de medio segundo) en:

- **Un partido** en una fila de marcadores: **Pin over TV** deja una tarjeta pequeña de marcador en directo en una
  esquina mientras ves cualquier cosa, y se oculta en el Home de Talkies y en el salvapantallas. No puede quitarle los
  botones del mando a la app de debajo, y se va unos cinco minutos después de que acabe el partido. **Corner** lo
  mueve; **Unpin from TV**, o Control Center → **Unpin score**, lo quita. Necesita el interruptor de Accessibility de
  Talkies.

- **Una app** en el dock o en All apps:
  - **Add to dock** (desde All apps): ocupa el último sitio del dock, y la app que estaba ahí pasa al principio de
    All apps. **Remove from dock** hace lo contrario: la primera app de All apps ocupa su sitio.
  - **Move** (más abajo), para colocarla exacta.
  - **Change image**: una foto tuya para su mosaico, **From your phone** (escanea un código QR y elige una foto; se
    recorta al mosaico en el teléfono) o **From this TV** (Pictures, Downloads, memorias USB). **Original image**
    devuelve la propia de la app.
  - **Hide**: sale del dock y de All apps pero sigue instalada (la búsqueda por voz y Settings siguen abriéndola).
    Talkies settings → **Hidden apps** las lista; OK devuelve una, al final de All apps.
  - **Uninstall** abre el cuadro propio de Android "Uninstall this app?", y solo aparece para apps que instalaste (no
    las del sistema ni Talkies). **App info** abre la página de la app en Settings.
- **Una entrada** (Talkies settings → Inputs on Home): Add to dock o Remove from dock, Move, Change image, y
  **Remove from Home**.
- **Una tarjeta de Continue** (la pestaña Continue o la fila Watch Next): **Refresh Continue** (vuelve a leer lo que
  han informado las apps; Continue también lo hace cada vez que vuelves a Home), **Remove from Continue**, o **Open** la
  app en sí antes que el episodio. Un título quitado se queda fuera hasta que la app informe un episodio nuevo suyo.

**Atrás** cierra un menú sin hacer nada.

## Reordenar apps

1. **Mantén OK** sobre cualquier app en el dock o en All apps y elige **Move**. Sale levantada, con un anillo
   alrededor, y aparece una pista arriba.
2. **Usa las flechas** para moverla. Dentro de una fila troca el sitio con su vecina; en la cuadrícula, Arriba y Abajo
   la mueven una fila y las otras apps se corren. Pasar del dock a All apps o al revés la intercambia con la app de
   ahí, así que el dock siempre conserva siete.
3. **Pulsa OK** para colocarla, o **Atrás** para cancelar y dejar todo como estaba.

El orden se guarda en la TV y sobrevive reinicios y reinstalaciones. Las apps que instales después se añaden al final
de la cuadrícula. Las sugerencias (Continue, Tendencias, New in …) no se pueden mover.

El dock de inicio, antes de que hayas movido nada, viene de `app/src/main/res/values/dock.xml`. Los nombres de paquete
se listan con:

```bash
adb -s "$TALKIES_TV" shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER
```

## Cambiar el fondo de pantalla

Talkies settings → **Wallpaper**. Tu foto sustituye el brillo del color de app, y Talkies añade justo el velo detrás
del texto para que se siga leyendo.

- **From your phone:** escanea el código QR con tu teléfono (en la misma Wi-Fi que la TV) y elige una foto. Tu teléfono
  la recorta a la pantalla y manda solo esa imagen, sin su ubicación ni otros datos. La página funciona una vez,
  durante 5 minutos.
- **From this TV:** las fotos más nuevas de la TV y de cualquier memoria USB conectada a ella (Pictures, Downloads,
  DCIM…), cada una con una vista previa pequeña; OK en una la usa. Android pregunta una vez para dejar a Talkies ver
  tus fotos.
- **Una carpeta, una foto al día:** las carpetas van primero en esa lista, cada una con su foto más nueva. OK en una
  usa ahora su primera foto y la siguiente cada día, por orden de nombre, y vuelve a empezar tras la última. Elegir una
  sola foto, o **Remove your picture**, lo para. Si la carpeta ya no está (un USB desconectado), la última foto se
  queda.
- **Picture of the day:** la de Bing, comprobada cada pocas horas. Tu foto gana mientras haya una.
- **Remove your picture:** de vuelta al brillo (o a la foto de Bing, cuando esa está encendida).

Desde un ordenador, copia en su lugar una imagen a la carpeta de Talkies. Puede ser `wallpaper.jpg`, `wallpaper.png` o
`wallpaper.webp`; 1920×1080 es lo que mejor va:

```bash
adb -s "$TALKIES_TV" push my-picture.jpg /sdcard/Android/data/com.techpp.glass/files/wallpaper.jpg
```

Pulsa Home en el mando para verla. Para quitarla desde un ordenador, borra el archivo:

```bash
adb -s "$TALKIES_TV" shell rm /sdcard/Android/data/com.techpp.glass/files/wallpaper.jpg
```

## Usar otra pantalla de inicio

- **Google TV:** Google TV se queda con su propio launcher como la app de inicio pase lo que elijas en *Default home
  app* (comprobado en esta TV), así que los launchers de terceros, Talkies incluido, se quedan con el botón Home con un
  servicio de accesibilidad. Para usar otro, apaga **Settings → Device Preferences → Accessibility → Talkies** (Talkies
  settings → **Home app** lo abre) y enciende la opción Home propia de ese launcher. Vuelve a encender la de Talkies
  para regresar.
- **Android TV:** Talkies settings → **Home app** abre el selector de Android; elige otro launcher y el servicio de
  Talkies le deja el botón Home a él. Elige Talkies ahí para volver.
- **Desde un ordenador:**

  ```bash
  ./switch-home.sh list                       # the home screens on the TV
  ./switch-home.sh talkies                    # Talkies
  ./switch-home.sh stock                      # the TV's own home screen
  ./switch-home.sh com.spocky.projengmenu     # any launcher you installed, by package name
  ```

  Avisa cuando la TV hace caso omiso de la elección (Google TV).

Para solo echar un vistazo a otro launcher, abre su mosaico debajo de All apps; Home sigue trayéndote de vuelta a
Talkies.

## Google Assistant y el launcher de Google TV

El launcher propio de la TV se queda **enabled**. En Google TV (`com.google.android.apps.tv.launcherx`) el Assistant
muestra muchas respuestas dentro de él: "show me comedy shows" abre su página de resultados, "Stranger Things" su
página de título. Con él desactivado esas búsquedas fallaban con "Google Assistant is having a problem processing your
request", mientras que abrir apps sí funcionaba.

Con él activado, se queda con el botón Home, así que **el servicio de accesibilidad de Talkies debe estar
encendido** (Settings → Device Preferences → Accessibility → Talkies). El servicio:

- abre Talkies cuando pulsas Home;
- abre Talkies sobre la pantalla de inicio integrada de la TV cuando esa aparece sola (tras un reinicio, o al salir
  con Atrás de sus páginas), salvo dentro de los 8 segundos del Assistant, para que sus resultados se queden. Los
  launchers integrados se encuentran cuando el servicio arranca (el de Google TV, el de Android TV o el del fabricante
  de la TV: apps del sistema que pueden ser la pantalla de inicio); los launchers que instalaste tú, como Projectivy,
  se dejan en paz;
- vigila solo las ventanas de JioHotstar, esos launchers y el Assistant, y filtra solo Home, OK mientras está el aviso
  del Sleep timer, y los botones del mando a los que hayas dado un trabajo;
- cuando pulsas Home dentro de una app, pregunta qué app era esa, para Recent apps.

Si alguna vez el servicio está apagado, Home muestra Google TV (con sus anuncios) hasta que lo enciendas de nuevo.
`build-and-install.sh` avisa cuando está apagado. Android lo apaga si a Talkies se le fuerza la parada (cuenta como un
fallo), así que no uses "Force stop" con Talkies; los scripts de aquí no lo hacen.

**Opcional: devolverlo solo.** Dale a Talkies el permiso para cambiar ajustes seguros, una vez:

```bash
adb -s "$TALKIES_TV" shell pm grant com.techpp.glass android.permission.WRITE_SECURE_SETTINGS
```

Así, si una parada forzada apaga el servicio, Talkies lo vuelve a encender la siguiente vez que arranca (lo abres desde
las apps de Google TV, la TV se reinicia, o se actualiza). Solo hace eso si el servicio estaba encendido cuando lo
mataron; si lo apagas tú, se queda apagado. El mismo permiso deja que Talkies settings → Network debugging apague la
depuración, y que Control Center → Animations cambie la velocidad de animación de la TV. Es un permiso potente (una app
que lo tiene puede cambiar ajustes del sistema), así que decide tú;
`adb shell pm revoke com.techpp.glass android.permission.WRITE_SECURE_SETTINGS` lo devuelve.

Los botones del mando y Home solo los ve el servicio si vienen de un mando de verdad: las teclas enviadas con
`adb shell input keyevent` se saltan el filtro de teclas de accesibilidad de Android, así que un Home enviado así abre
Google TV. Hueco conocido: elegir una película dentro de la lista de resultados del propio Assistant no abre su página
(la app de Google se niega a arrancarla); pedir el título por nombre sí.

## El asistente de configuración de Google TV

El asistente de configuración de Google TV (`com.google.android.tungsten.setupwraith`) también atiende el botón Home,
pero déjalo encendido: el interruptor de Accessibility de Talkies manda Home a Talkies de todas formas, y el
salvapantallas del modo Ambient necesita al asistente para mostrar tus Google Photos (sin él, muestra en silencio el
arte de Google). Talkies no apaga nada.

Las versiones anteriores de `build-and-install.sh` apagaban el asistente. Si tu salvapantallas perdió tus fotos,
vuelve a encenderlo (`build-and-install.sh` ahora también hace esto):

```bash
adb -s "$TALKIES_TV" shell pm enable com.google.android.tungsten.setupwraith
```

Puede que Android ponga entonces el asistente o el launcher de Google TV como la app de Home predeterminada. Con el
interruptor de Accessibility encendido eso da igual, pero para que Talkies vuelva a ser la predeterminada:

```bash
adb -s "$TALKIES_TV" shell cmd package set-home-activity --user 0 com.techpp.glass/.HomeActivity
```
