# Eazzy Server Utils

Un mod de utilidades para servidores de Minecraft **1.20.1 (Forge)** que añade teletransporte, comandos de staff, gestión del chat, anuncios y protecciones básicas contra exploit.

> **Nota sobre versiones:** Este repositorio contiene dos directorios. `eazzy-server-utils-1.20.1/` es el proyecto fuente completo y compilable. `eazzy-server-utils-1.21.8/` es un artefacto NeoForge ya compilado y **no contiene fuentes**.

---

## Características

- **Sistema de hogares** (`/sethome`, `/home`, `/homes`, `/delhome`, `/renamehome`, `/homepublic`).
- **Warps globales** (`/setwarp`, `/warp`, `/warps`).
- **TPA** (`/tpa`, `/tpahere`, `/tpaccept`, `/tpacancel`).
- **Teletransporte útil** (`/back`, `/spawn`, `/near`).
- **Mensajería privada** (`/msg`, `/reply`).
- **Utilidades de staff** (`/tps`, `/freeze`, `/unfreeze`, `/iv`, `/vanish`).
- **Sanciones** (`/warn`, `/warns`, `/clearwarns`, `/kick`, `/ban`, `/tempban`, `/unban`, `/ipban`).
- **Gestión del servidor** (`/broadcast`, `/announce`, `/lockchat`, `/lag`, `/mspt`).
- **GUIs portátiles** (`/crafting`, `/anvil`, `/smithing`).
- **Cabeza de jugador** (`/head`).
- **Chat personalizado** con cabezas de jugador (requiere el mod en el cliente).
- **Anti-exploit básico** para `/home` y `/warp`: cooldown compartido, dimensiones bloqueadas y bloqueo en combate.
- **Persistencia JSON** para hogares, warps, advertencias y baneos.

---

## Comandos

### Jugadores

| Comando | Descripción |
|---|---|
| `/sethome [nombre]` | Establece un hogar. Por defecto se llama `home`. |
| `/home [nombre]` | Te teletransporta a un hogar. |
| `/homes` | Lista tus hogares. |
| `/delhome <nombre>` | Elimina un hogar. |
| `/renamehome <antiguo> <nuevo>` | Renombra un hogar. |
| `/homepublic <nombre>` | Alterna la visibilidad pública/privada de un hogar. |
| `/warp <nombre>` | Te teletransporta a un warp global. |
| `/warps` | Lista los warps disponibles. |
| `/tpa <jugador>` | Solicita teletransportarte a un jugador. |
| `/tpahere <jugador>` | Solicita que un jugador se teletransporte a ti. |
| `/tpaccept` | Acepta una solicitud de TPA. |
| `/tpacancel` | Cancela una solicitud de TPA. |
| `/back` | Vuelve a tu ubicación anterior (muerte o teletransporte). |
| `/spawn` | Te teletransporta al spawn. |
| `/near` | Muestra jugadores cercanos. |
| `/msg <jugador> <mensaje>` | Envía un mensaje privado. Alias: `/tell`. |
| `/reply <mensaje>` | Responde al último mensaje privado. Alias: `/r`. |
| `/head <jugador>` | Obtiene la cabeza de un jugador. |
| `/crafting` | Abre una mesa de crafteo portátil. |
| `/anvil` | Abre un yunque portátil. |
| `/smithing` | Abre una mesa de herrería portátil. |

### Staff

| Comando | Descripción |
|---|---|
| `/setwarp <nombre>` | Crea un warp global. |
| `/tps` | Muestra los TPS del servidor. |
| `/lag` o `/mspt` | Informe privado de rendimiento (TPS, MSPT, memoria, uptime, jugadores, chunks, entidades). |
| `/freeze <jugador>` | Congela a un jugador. |
| `/unfreeze <jugador>` | Descongela a un jugador. |
| `/iv <jugador>` | Abre el inventario de un jugador. |
| `/vanish` | Activa/desactiva el vanish. |
| `/warn <jugador> <razón>` | Advierte a un jugador. |
| `/warns <jugador>` | Muestra las advertencias de un jugador. |
| `/clearwarns <jugador>` | Limpia las advertencias. |
| `/kick <jugador> [razón]` | Expulsa a un jugador. |
| `/ban <jugador> [razón]` | Banea permanentemente a un jugador. |
| `/tempban <jugador> <duración> [razón]` | Banea temporalmente. Formatos: `30s`, `5m`, `2h`, `7d`. |
| `/unban <jugador>` | Desbanea a un jugador. |
| `/ipban <jugador> [razón]` | Banea la UUID y la IP actual del jugador. |
| `/broadcast <mensaje>` o `/announce <mensaje>` | Muestra un título centrado a todos. Usa `|` para separar título, subtítulo y mensaje de chat. |
| `/lockchat` | Bloquea/desbloquea el chat para jugadores sin permisos de staff. |

### Ejemplos de `/broadcast`

```
/broadcast &c¡Evento PvP! | &7Empieza en 5 minutos
/broadcast &a¡Bienvenidos! | &7Disfruten del servidor | &e¡Gracias por jugar!
```

En el último ejemplo se muestra el título, luego el subtítulo, y 3 segundos después el mensaje de chat.

---

## Instalación

1. Compila el mod:

```bash
cd eazzy-server-utils-1.20.1
./gradlew :forge:build
```

2. El JAR se encuentra en:

```
forge/build/libs/eazzyserverutils-forge-1.0.0-1.20.1.jar
```

3. Copia el JAR a la carpeta `mods` de tu servidor Forge 1.20.1.

---

## Configuración

El mod genera dos archivos en `config/eazzy_server_utils/`:

### `config.properties`

| Opción | Descripción | Por defecto |
|---|---|---|
| `maxHomes` | Máximo de hogares por jugador. | `8` |
| `homeCooldown` | Cooldown de `/home` en segundos. | `5` |
| `maxWarps` | Máximo de warps globales. | `100` |
| `warpCooldown` | Cooldown de `/warp` en segundos. | `5` |
| `tpaCooldown` | Cooldown de TPA en segundos. | `3` |
| `tpaTimeout` | Tiempo de expiración de TPA en segundos. | `12` |
| `headCooldown` | Cooldown de `/head` en segundos. | `3600` |
| `spawnCooldown` | Cooldown de `/spawn` en segundos. | `5` |
| `backCooldown` | Cooldown de `/back` en segundos. | `5` |
| `nearRadius` | Radio de `/near` en bloques. | `50` |
| `homeWarpSharedCooldown` | Cooldown compartido entre `/home` y `/warp`. | `true` |
| `homeWarpCooldown` | Cooldown compartido en segundos. | `5` |
| `homeWarpBlockedDimensions` | Dimensiones donde `/home` y `/warp` están bloqueados, separadas por comas. | `""` |
| `combatCooldownSeconds` | Segundos de bloqueo por combate tras recibir daño. | `10` |

### `chat.properties`

| Opción | Descripción | Por defecto |
|---|---|---|
| `enabled` | Activa el chat personalizado. | `true` |
| `format` | Formato del chat. | `§b%name%§7: §f%message%` |
| `showHeadHover` | Muestra la cabeza del jugador al pasar el ratón por el nombre. | `true` |
| `showHeadIndicator` | Muestra un indicador junto al nombre. | `false` |
| `headIndicator` | Indicador a mostrar. | `☺` |
| `clickNameToMessage` | Permite clickear el nombre para enviar `/msg`. | `true` |
| `showHeadInline` | Renderiza cabezas en línea en el chat (cliente). | `true` |

---

## Permisos

El mod usa el sistema de permisos vanilla de Minecraft:

- **Permiso 0**: comandos de jugador.
- **Permiso 2**: comandos de staff.

Los jugadores con permiso 2 ignoran la mayoría de cooldowns y las protecciones anti-exploit.

---

## Notas importantes

- El chat personalizado requiere que los jugadores tengan el mod instalado en el cliente para ver los mensajes correctamente.
- `/lockchat` solo bloquea mensajes de chat; no bloquea comandos ni mensajes del sistema.
- Los baneos y advertencias se guardan en la carpeta del mundo (`<mundo>/data/eazzy_server_utils/`).
- El mod no tiene integración con plugins de permisos como LuckPerms; usa los niveles de permiso vanilla.

---

## Tecnología

- **Minecraft**: 1.20.1
- **Loader**: Forge 47.4.10
- **Lenguaje**: Java 17
- **Toolchain**: Unimined 1.4.1-SNAPSHOT
- **Mappings**: Mojang mappings

---

## Licencia

Consulta el archivo `gradle.properties` o la cabecera del proyecto para más información sobre la licencia.
