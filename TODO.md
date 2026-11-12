# TODO

Nota de trabajo, no documentación. Cada punto está también en la lista de tareas de la sesión.
Referencias a `archivo:línea` de `app/src/main/java/com/techpp/glass`, comprobadas al escribir esto.

## Orden

Primero se mide, luego se dibuja: **#17** (números reales de los tres TVs) desbloquea **#20**
(una sola escala de separaciones y radios), y **#20** desbloquea los dos rediseños de carteles.
El clima (**#21**, **#22**, **#23**) es independiente y puede ir en paralelo. El modo compacto
(**#24**) va al final: hecho antes, cada cambio de tamaño pelea con cifras sueltas.

## Diseño de carteles

- **#18 · Fila de Continuar (cartel grande).** Se dibuja en `HomeActivity.java:1748`
  (`pickView`): dos `ImageView` apilados con `CENTER_CROP` (`HomeActivity.java:1760-1769`), barra de
  progreso (`:1770-1785`), y debajo título de 16 sp en Plus Jakarta Sans 700 (`:1787-1797`) y meta de
  12.5 sp (`:1798-1809`). Radio 14 (`:1755`), elevación 14 al enfocar (`:2090`), escala 1.04 (`:2258`).
  Qué arte se muestra según el tamaño lo decide `updatePickImages` (`HomeActivity.java:1819`).
  **Decidir antes de programar:** reparto 16:9 / 2:3; si el nombre va sobre el arte con degradado (hoy
  no hay scrim: el texto vive fuera) o sigue debajo; qué se ilumina (el anillo es `HighlightView`,
  `HomeActivity.java:2104-2149`).
- **#19 · Filas de servicios y apps.** No hay logotipos propios: `AppEntry.load`
  (`AppEntry.java:143-171`) prueba un jpg en `files/tiles/<paquete>.jpg` (`:204`), los glifos de
  entrada del TV, el banner del launcher a la máxima densidad (`sharpBanner`, `:178`) y, si no, el
  icono. Se dibuja en `appTile` (`HomeActivity.java:1062-1078`, radio 11, icono de respaldo al 60% de
  la altura) y `showAppLogo` (`:1852-1869`, con el desenfoque de `RecentsCards.blurOf`). La cabecera de
  fila es el icono del launcher metido en un bitmap de 20 dp (`HomeActivity.java:1392-1403`).
  **Decidir:** si se normaliza el marco para que un gran servicio y un canal suelto pesen lo mismo, y
  si las filas de canales (`channelCard`, `HomeActivity.java:1476`, radio 10) comparten el lenguaje.
- **#20 · Una sola escala de separaciones y radios.** Hoy: `PicksRow.java:16`
  (440/134/186/18/40/48), `HomeActivity.java:98` (548/212/200/112/280) y radios 14, 11, 10 y 16
  (`HomeActivity.java:1755`, `:1062`, `:1476`, `RecentsCards.java:126`). Sin cambiar el aspecto: es el
  suelo que necesitan #18 y #19.
- **#24 · Modo compacto.** Caben 2 filas de recomendaciones y 3 canales apilados
  (`HomeActivity.java:1443-1460`); dos carruseles, uno de apps vistas y otro de recomendaciones. Opciones:
  que los canales ocupen el hueco de las recomendaciones, o sacar las apps ocultas del carrusel y dejarlas
  en la rejilla. Falta el número de #17 para saber cuánto se gana.

## Arte de las filas (#25, #26) — antes que el aspecto

Síntomas que cuenta él de memoria, con su explicación en el código:

- **Continuar sale recortado, con fragmentos del título.** Bug de montaje, no de red: a las entradas de Continuar
  solo se les guarda la miniatura horizontal del proveedor (`WatchNext.java:107`); el póster vertical se pone en el
  único camino de ocultar spoilers (`:104`). El hueco pequeño es vertical (268x372 px) y se rellena con la horizontal
  a `CENTER_CROP` (`HomeActivity.java:1819-1846`): se pierden ~2 de cada 5 px de ancho. Si el proveedor trae el título
  quemado en la imagen, sale cortado.
- **TVE repite imágenes.** Dos causas posibles, hoy indistinguibles: el proveedor publica la misma URI de arte para
  varios episodios (una imagen por serie), o el archivo se nombra con el hash de la cadena URI
  (`WatchNext.java:220`) y dos URIs distintas colisionan en 32 bits. La primera es dato real del proveedor; la segunda
  es un fallo que hay que quitar.
- **Plex: solo la primera y las demás ni salen.** Sospechoso firme: la imagen se decodifica entera en memoria
  (`WatchNext.java:228`) y se encoje después (`:230`). Un fanart grande no cabe, `decodeStream` da null o lanza, la
  función devuelve null (`:237-240`) y no se reintenta nunca. `landscape()` (`:172-177`) ya usa `inJustDecodeBounds`,
  así que el patrón correcto existe en el repo; solo falta usarlo al decodificar.

Estrategia, por coste: límite al decodificar y tamaños TMDB que corresponden al hueco (w780 / w500 en vez de
w1280 / w342) → clave de archivo por contenido y columna de origen registrada → el hueco elige la forma del arte
(FIT_CENTER sobre plato desenfocado si no hay póster; nunca CENTER_CROP a otro ratio) → plato con el nombre cuando el
proveedor no publica nada utilizable.

## Clima

- **#21 · Modelo de datos en vez de cadenas con tabuladores.** El pronóstico se guarda como texto en
  preferencias y se vuelve a parsear en tres sitios: `Weather.java:85` (`highLow`, salvapantallas),
  `:335` (`forecast`, Control Center) y `:72` (`rainLater`, la píldora), claves `hours` y `days`.
  La conversión a Fahrenheit está escrita dos veces (`degrees`, `:389`; `line`, `:426`) y las condiciones
  en inglés se guardan ya formadas (`CONDITIONS`, `:487`; `wmoCondition`, `:516`), con la traducción solo
  al dibujar (`shown`, `:494`). Tipo pequeño guardado una vez, una conversión, y guardar el código numérico.
- **#22 · Red y refresco.** Un solo intento por refresco (`fetch`, `:539`) sin cabeceras condicionales
  (lo único que se manda es el User-Agent, `:543`), así que cada 30 minutos (`REFRESH_MS`, `:38`) se
  vuelven a bajar entero el modelo, la METAR y el MET Norway. La METAR se pide dos veces dentro del mismo
  bucle (`nearestReport`, `:394`). El geocodificador lleva `language=en` fijo (`:199`), así que una zona
  del tiempo en España busca ciudades por su nombre inglés. `files/location.txt` se lee en cada llamada
  (`:170`). Los fallos solo se registran y la línea vieja se queda muda (`:135-137`).
  **Ojo:** las claves nuevas van a `backup-settings.sh` / `restore-settings.sh`.

## API

- **#23 · Cadena de proveedores.** Sin claves y sin bibliotecas: Open-Meteo
  (`:226`) → MET Norway (`metForecast`, `:254`) y, para la temperatura real, aviationweather.gov
  (`nearestReport`, `:394`), que sustituye la del modelo (`:245`). Opciones, de coste/beneficio:
  1. El respaldo MET Norway llega sin probabilidad de lluvia y sin amanecer/atardecer (lo dice su comentario,
     `:250-252`; se devuelven vacíos en `:306` y se conservan los últimos conocidos). El amanecer y el
     atardecer se pueden calcular en casa por fórmula solar: cero red, cero biblioteca, cero proveedor.
  2. La temperatura de estación es un valor puntual y puede venir de 150 km; con umbral de distancia o
     mezclando con el modelo en vez de sustituir.
  3. Tres geolocalizadores por IP (`IP_LOOKUPS`, `:41`: ipwho.is, ipapi.co, geojs.io) para algo que solo se
     usa si no hay zona escrita, y ninguno dice cuál funcionó.
  4. Ninguna dirección tiene límite propio: si Open-Meteo cambia de plan, el clima entero se apaga. Decidir
     si se mantiene la promesa de "sin claves" o se admite una opcional leída de `/sdcard`, como TMDB.
  Nada de esto falla hoy: son mejoras, no bugs.
