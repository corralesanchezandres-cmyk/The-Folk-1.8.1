# The Folk 1.81: mod de terror para Minecraft Java 1.20.1 (Forge)

El .jar se llama `thefolkforge-1.81-1.20.1.jar`. Para sacar una versión nueva, cambia el número en DOS lugares:
`version = '...'` en `build.gradle` y `version="..."` en `src/main/resources/META-INF/mods.toml`.

## Novedades de la 1.81
- **Apariciones más espaciadas** (2,5 veces) para The Folk, The Thing, Man y Uknow. Se cambia en `FolkDirector.java`:
  `SPAWN_DELAY_FACTOR` (1.0 = como en la 1.80, 4.0 = todavía más raras).
- **Uknow** tiene un nuevo aspecto (escuálido, manchado de sangre, boca enorme con dientes, ojos naranjas y dos cuernos
  largos) y espera **1,5 segundos** sin atacar después de soltarte.

## Novedades de la 1.80
- **Todas las criaturas son más lentas** (ver la tabla de velocidades).
- **Uknow**: nueva criatura cuadrúpeda con cuernos que mata a todo lo que ve.
- **Efecto Sangrado**: lo deja Uknow cuando escapas de su agarre.
- **Guía de supervivencia**: un libro que recibes equipado al aparecer; nunca se duplica.

## Velocidades (bloques por segundo)
El jugador camina a 4.3 y corre a 5.6. Son estimaciones calculadas, no medidas en el juego.

| Criatura | Antes | Ahora |
|---|---|---|
| The Folk, fase 2 | 1.2 | 1.0 |
| The Folk, fase 3 | 5.0 | 4.5 |
| The Folk, fase 4 | 20 | 8 |
| The Folk en el evento Stalk | 24 | 10 |
| The Thing | 24 | 10 |
| The Thing tras el evento Atrás | 55 | 15 |
| Man | 39 | 12 |
| Uknow (nueva) | n/a | 5.8, y 11.6 al abalanzarse |

Las velocidades se configuran en bloques por segundo y el código las convierte (`FolkUtil.speedAttr`).

## Uknow (solo THE FOLK IS COMING)
Bestia cuadrúpeda con cuernos. Aparece fuera de tu vista cada 5 a 10 minutos con un rugido que se oye de lejos
y desaparece a los 90 segundos.
- **Mata a todo lo que ve**: animales, aldeanos, monstruos... (no a las criaturas del mod ni a jugadores en
  creativo o espectador).
- Camina a 5.8 bloques/s y **se abalanza** al doble (11.6) cuando su presa está a menos de 12 bloques.
  Después de cada salto espera 4 segundos.
- Si atrapa a un jugador, lo sube a su lomo: queda **paralizado** y recibe 3 corazones de daño cada segundo.
- **Para escapar**: pulsa ESPACIO 3 veces en 2 segundos. Al soltarte quedas con **Sangrado** y Uknow espera
  1,5 segundos antes de poder atacar o abalanzarse otra vez.
- El Segador y el Paralizador también liberan a quien sujeta.
- Solo ataca lo que ve: un muro o una puerta cerrada lo detiene (no abre puertas ni rompe bloques).

## Sangrado
Efecto parecido al veneno: te quita medio corazón cada 1.5 s (nunca te mata: se detiene en medio corazón),
te ralentiza un 15% y deja partículas rojas. Dura 10 segundos.

## Guía de supervivencia
Libro escrito con todo lo necesario para sobrevivir a cada criatura. Se te entrega **equipado en la mano
izquierda** al entrar al mundo y al reaparecer tras morir. Si ya tienes una en el inventario no se te da otra,
así que nunca se acumula. Está en español o inglés según el idioma elegido en `/folklang`.
Comando para recuperarla (o cambiarle el idioma si ya la tienes): `/folkguide`.

## Armas
| Arma | Cómo se usa | Efecto | Espera |
|---|---|---|---|
| **Segador** | Golpear a la criatura | La ralentiza mucho y la vuelve tonta durante 3 segundos: deambula sin rumbo y no ataca | 5 s |
| **Paralizador** | Clic derecho apuntando (alcance 16 bloques, no atraviesa paredes) | La paraliza durante 2 segundos | 8 s |

Como las criaturas son invulnerables, el efecto se aplica con el golpe o el disparo, no con el daño.
Recetas (mesa de crafteo):
- Segador: hierro, hierro, arena de almas arriba y 2 palos debajo, en columna.
- Paralizador: arriba cobre, redstone, cobre; en medio hierro, perla de ender, hierro; abajo un palo.

También salen en los cofres de los laboratorios. Están en el modo creativo, pestaña "Combate".

## Laboratorios
Edificio pequeño de ladrillos de piedra (9x9) con ventanas, alambique, caldero, estanterías y 2 cofres.
Cada cofre puede llevar un Segador o un Paralizador, además de hierro, cobre, redstone y otras cosas.
Para encontrar el más cercano: `/locate structure thefolk:laboratory`. Solo aparecen en mundos nuevos o en zonas
que todavía no has explorado.

## Criaturas
### The Folk (dweller de 3 bloques)
| Fase | Normal | ERROR / COMING | Comportamiento |
|---|---|---|---|
| 1 | 5 días | 2 días | Aparece lejos, a la vista, y solo te mira. Desaparece si te acercas a menos de 12 bloques. |
| 2 | 8 días | 4 días | Aparece a la vista y camina lento hacia ti. Al llegar a unos 5 bloques se queda quieto y desaparece. |
| 3 | 5 días | 2 días | Aparece escondido (14-24 bloques), te persigue 1 minuto, rompe bloques y golpea (3 corazones). |
| 4 | no existe | 2 días | Igual que la 3, pero más rápido. |

### The Thing (solo THE FOLK IS COMING)
Aparece escondida, muestra "Im C0MING" en rojo, abre puertas, hace ruidos saturados, hace 12 corazones de daño
y desaparece a los 20 segundos.

### Man (solo THE FOLK IS COMING)
Cuerpo de jugador, pálido, con ojos blancos brillantes. Aparece 1 bloque detrás de ti y se queda parado hasta que
lo miras. 3 segundos después de que lo miras, muestra "CORRE" en rojo y corre hacia ti (hasta 30 s). Hace 10
corazones de daño. Si en 45 segundos no lo miras, se va. Mientras exista, y unos 3 minutos después, las demás
criaturas (incluida Uknow) aparecen 3 veces más seguido. Aparece cada 10 a 20 minutos.

## Eventos
- **Atrás** (The Thing, solo cuando el ciclo está en las fases 2 a 4): aparece el texto "atrás tuyo", suena una voz y
  The Thing se coloca 2 bloques detrás de ti, sin moverse y en silencio. Cuando LA MIRAS, se queda parada 2 segundos
  y después te persigue 1.5 veces más rápido. Si no la miras en 6 segundos, se va sin haberse movido. Pasa en el 70%
  de las apariciones.
- **Stalk** (The Folk, fases 1 y 2, modos ERROR y COMING): The Folk aparece pegado a una ventana de vidrio cerca de ti
  (o asomándose por una esquina), te mira unos 4 segundos, silba, sale corriendo a la velocidad de The Thing hacia la
  puerta más cercana a ti, la rompe y te persigue unos 15 segundos (solo rompe puertas, no paredes). Reemplaza al 40%
  de las apariciones de fase 1 y 2.

## FolkConfig (el engranaje)
Item `thefolk:folkconfig`. Clic derecho abre un menú con 3 botones:
- **Normal**: el mod funciona como siempre (sin fase 4, sin eventos, sin The Thing, Man ni Uknow).
- **ERROR**: fases más rápidas, fase 4 y evento Stalk.
- **THE FOLK IS COMING**: todo lo de ERROR, más The Thing, Man, Uknow y el evento Atrás.

El modo se guarda en el mundo. Consíguelo en el modo creativo (pestaña "Herramientas y utilidades") o con
`/give @s thefolk:folkconfig`. Las armas, la guía y los laboratorios funcionan en los tres modos.

## Idiomas (FolkLang)
`/folklang` abre un panel para elegir **Español** o **English**. Funciona sin trucos activados.
También puedes usar `/folklang es` y `/folklang en`. El idioma cambia los textos en pantalla, qué voz suena,
los mensajes de los comandos y la guía. Los nombres de los items siguen el idioma del propio Minecraft.
Para agregar un texto nuevo, añade una línea en `FolkLang.java`.

## Sonidos propios
Los silbidos y las voces usan sonidos de Minecraft por defecto. Para poner los tuyos:
1. Guarda tus archivos `.ogg` (mono) en `src/main/resources/assets/thefolk/sounds/`.
2. En `src/main/resources/assets/thefolk/sounds.json` cambia la línea `"name"` de cada sonido por
   `"thefolk:nombre_del_archivo"` (sin `.ogg`) y borra `"type": "event"` de esa línea.
   Sonidos: `whistle` (silbido), `voice.behind_es` y `voice.behind_en` (voz del evento Atrás).

## Comandos de prueba (necesitan permisos de operador o trucos activados)
- `/thefolk spawn 1|2|3|4`: invoca a The Folk en esa fase
- `/thefolk thing`: invoca a The Thing (con el evento Atrás)
- `/thefolk stalk`: evento Stalk (mejor dentro de una casa con ventanas de vidrio)
- `/thefolk man`: invoca a Man
- `/thefolk uknow`: invoca a Uknow
- `/thefolk mode normal|error|coming`: cambia el modo sin usar el item
- `/thefolk status`: muestra el modo, el día del mundo, el día del ciclo y la fase
- `/give @s thefolk:reaper` y `/give @s thefolk:paralyzer`: te da las armas
- `/time add 24000`: suma un día para saltar de fase

Comandos sin permisos: `/folklang` y `/folkguide`.
Las criaturas son invulnerables (solo el modo creativo o /kill las eliminan). Aparecen solas únicamente en el
Overworld, fuera de dificultad Pacífico y no con jugadores en creativo o espectador (con los comandos sí funcionan).

## Ajustes
Bloque CONFIGURACIÓN al inicio de estos archivos:
- `FolkDirector.java`: días por fase, cada cuánto aparece cada cosa, distancias, probabilidad de eventos, aumento de Man
- `TheFolkEntity.java`: velocidades (BPS_...), daño, tiempos del evento Stalk
- `TheThingEntity.java`, `TheManEntity.java`: velocidad (BPS), daño y tiempos
- `UknowEntity.java`: velocidad, salto, daño del agarre, pulsaciones para escapar, duración del Sangrado
- `BleedingEffect.java`: ralentización y daño del Sangrado
- `ReaperItem.java` y `ParalyzerItem.java`: duración del efecto, espera entre usos y alcance
- `FolkGuide.java`: textos de la guía
- Laboratorios: `src/main/resources/data/thefolk/worldgen/structure_set/laboratory.json` y
  `data/thefolk/loot_tables/chests/laboratory.json`.

## Compilar e instalar
Sube el contenido de esta carpeta a tu repositorio (reemplaza `src` y `build.gradle`; el workflow de GitHub Actions
se queda igual) y descarga el .jar desde Actions, en Artifacts. Borra los .jar anteriores de la carpeta `mods`,
copia el nuevo en `%appdata%\.minecraft\mods` y abre TLauncher con Forge 1.20.1.
