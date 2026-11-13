# TODO

Nota de trabajo, no documentación. Cada punto está también en la lista de tareas de la sesión.
Referencias a `archivo:línea` de `app/src/main/java/com/techpp/glass`, comprobadas al escribir esto.

## Orden

Primero se mide, luego se dibuja: **#17** (números reales de los tres TVs) desbloquea **#20**
(una sola escala de separaciones y radios), y **#20** desbloquea los dos rediseños de carteles.
El clima (**#21**, **#22**, **#23**) es independiente y puede ir en paralelo. El modo compacto
(**#24**) va al final: hecho antes, cada cambio de tamaño pelea con cifras sueltas.

## Diseño de carteles

- **#18 · Fila de Continuar (cartel grande).** Se dibuja en `HomeActivity.java:1743`
  (`pickView`): dos `ImageView` apilados con `CENTER_CROP` (`:1751-1760`), barra de progreso
  (`:1761-1776`), y debajo título de 16 sp en Plus Jakarta Sans 700 (`:1778-1788`) y meta de
  12.5 sp (`:1789-1800`). Esquinas cuadradas (`:1749`), elevación 14 al enfocar (`:2079`), escala 1.04
  (`:2247`). Qué arte se muestra según el tamaño lo decide `updatePickImages` (`HomeActivity.java:1810`).
  **Decidir antes de programar:** reparto 16:9 / 2:3; si el nombre va sobre el arte con degradado (hoy
  no hay scrim: el texto vive fuera) o sigue debajo; qué se ilumina (el anillo es `HighlightView`, con
  `snapRing` en `:2138` y `followRing` en `:2145`).
  **Ya resuelto aparte (2026-11-13):** cuánto sale de cada fila se eligió en *Apps para continuar*
  (`openContinueApps`, `HomeActivity.java:4668`: dos bloques, interruptor por app y tope por fila).
  Quedan de este punto el orden cuando una tarjeta no trae hora y el techo de `MAX_CONTINUE` (16), que
  deja fuera lo que una fila generosa ya permite sacar.
- **#19 · Filas de servicios y apps.** No hay logotipos propios: `AppEntry.load`
  (`AppEntry.java:143-171`) prueba un jpg en `files/tiles/<paquete>.jpg` (`:204`), los glifos de
  entrada del TV, el banner del launcher a la máxima densidad (`sharpBanner`, `:178`) y, si no, el
  icono. Se dibuja en `appTile` (`HomeActivity.java:1062-1078`, radio 11, icono de respaldo al 60% de
  la altura) y `showAppLogo` (`:1843-1860`, con el desenfoque de `RecentsCards.blurOf`). La cabecera de
  fila es el icono del launcher metido en un bitmap de 20 dp (`HomeActivity.java:1392-1403`).
  **Decidir:** si se normaliza el marco para que un gran servicio y un canal suelto pesen lo mismo, y
  si las filas de canales (`channelCard`, `HomeActivity.java:1476`, ya cuadradas) comparten el lenguaje.
- **#20 · Una sola escala de separaciones y radios.** Hoy: `PicksRow.java:16`
  (331/134/186/18/40/48), `HomeActivity.java:98` (548/212/200/112/280) y los radios que quedan, 11 y 16
  (`appTile`, `HomeActivity.java:1062`, y `RecentsCards.java:126`): las filas de contenido ya son
  cuadradas. Sin cambiar el aspecto: es el suelo que necesitan #18 y #19.
- **#24 · Modo compacto.** Caben 2 filas de recomendaciones y 3 canales apilados
  (`HomeActivity.java:1443-1460`); dos carruseles, uno de apps vistas y otro de recomendaciones. Opciones:
  que los canales ocupen el hueco de las recomendaciones, o sacar las apps ocultas del carrusel y dejarlas
  en la rejilla. Falta el número de #17 para saber cuánto se gana.

## Arte de las filas (#25, #26) — antes que el aspecto

Síntomas que cuenta él de memoria, con su explicación en el código. Los tres de abajo están ya corregidos; lo que
falta es comprobarlos en los TVs (#17), porque cada número de tamaño está medido solo en el MiTV.

- **Continuar sale recortado, con fragmentos del título.** HECHO (2026-11-13): el hueco elige la forma del arte en
  `HomeActivity.showIn` (`:1944`). Una foto cuya proporción se aleja más de un 15 % de la del hueco se ve entera
  (`FIT_CENTER`) sobre un plato de sus propios colores (`RecentsCards.blurOf`); CENTER_CROP solo queda cuando las dos
  formas ya se parecen, y ahí no recorta nada. Queda por ver en el TV si el plato convence a las tarjetas pequeñas.
- **TVE repite imágenes.** HECHO la parte que era fallo nuestro: los archivos de arte se nombran con un MD5 de la
  dirección entera (`WatchNext.key`, `:221`) y no con el hashCode de 32 bits, así que dos direcciones que solo
  diferían en un token ya no caen en el mismo archivo. Si aún se repiten, es dato real del proveedor: publica la misma
  URI para varios episodios, y `census-art.sh` lo dice.
- **Plex: solo la primera y las demás ni salen.** HECHO con el límite al decodificar (`WatchNext.cached`, `:246-264`):
  las bytes se guardan, se miden y se leen a un `inSampleSize`; un fanart de 4000 px ya no pide más memoria de la que
  hay y el descodificador no devuelve null para siempre.

Estrategia, y dónde está: límite al decodificar (hecho) → tamaños que corresponden al hueco (hecho: TMDB w780/w342 y
el arte de los proveedores a 780 px; los pósteres se quedan en w342 porque el hueco pequeño son 268 px) → clave de
archivo por la dirección entera (hecho) → el hueco elige la forma del arte (hecho) → plato con el nombre cuando el
proveedor no publica nada utilizable (pendiente; hoy es `showAppLogo`, que pone el logotipo de la app en vez del
nombre del título).

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
