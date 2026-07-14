# Eazzy Server Utils 1.20.1 — Funcionalidades y comandos

Documento de referencia con todas las funcionalidades y comandos disponibles en la versión 1.20.1 (Forge) del mod.

---

## Hogares

Sistema personal de teletransporte para cada jugador.

- `/sethome [nombre]` — Establece un hogar en la posición actual; el nombre por defecto es `home`.
- `/home [nombre]` — Teletransporta al jugador al hogar indicado.
- `/homes` — Muestra la lista de hogares del jugador.
- `/delhome <nombre>` — Elimina el hogar especificado.
- `/renamehome <antiguo> <nuevo>` — Cambia el nombre de un hogar.
- `/homepublic <nombre>` — Alterna entre público y privado un hogar.

---

## Warps

Puntos de teletransporte globales gestionados por el staff.

- `/setwarp <nombre>` — Crea un warp global en la posición actual del staff.
- `/warp <nombre>` — Teletransporta al jugador al warp indicado.
- `/warps` — Lista todos los warps globales disponibles.

---

## Teletransporte entre jugadores (TPA)

Sistema de solicitudes de teletransporte entre jugadores.

- `/tpa <jugador>` — Envía una solicitud para teletransportarte hasta otro jugador.
- `/tpahere <jugador>` — Envía una solicitud para que otro jugador se teletransporte hasta ti.
- `/tpaccept` — Acepta la solicitud de TPA pendiente.
- `/tpacancel` — Cancela o deniega la solicitud de TPA pendiente.

---

## Movimiento

Comandos de teletransporte y utilidades de movimiento.

- `/back` — Devuelve al jugador a su última ubicación de muerte o teletransporte.
- `/spawn` — Teletransporta al jugador al spawn del overworld.
- `/near` — Muestra los jugadores que se encuentran dentro del radio configurado.

---

## Mensajería privada

Comunicación privada entre jugadores.

- `/msg <jugador> <mensaje>` — Envía un mensaje privado a un jugador; alias `/tell`.
- `/reply <mensaje>` — Responde al último mensaje privado recibido; alias `/r`.

---

## Utilidades de jugador

Herramientas y GUIs portátiles.

- `/head <jugador>` — Otorga la cabeza del jugador indicado.
- `/crafting` — Abre una mesa de crafteo portátil.
- `/anvil` — Abre un yunque portátil.
- `/smithing` — Abre una mesa de herrería portátil.

---

## Gestión del servidor

Comandos de administración y control del servidor.

- `/broadcast <mensaje>` — Muestra un título centrado a todos los jugadores; admite título, subtítulo y mensaje de chat separados por `|`.
- `/announce <mensaje>` — Alias de `/broadcast`.
- `/lockchat` — Bloquea o desbloquea el chat para todos los jugadores sin permisos de staff.
- `/tps` — Muestra los TPS actuales del servidor.
- `/lag` — Muestra un informe privado de rendimiento con TPS, MSPT, memoria, uptime, jugadores, chunks y entidades.
- `/mspt` — Alias de `/lag`.

---

## Staff y moderación

Comandos de supervisión y sanciones.

- `/freeze <jugador>` — Congela a un jugador impidiendo movimiento e interacciones.
- `/unfreeze <jugador>` — Descongela a un jugador.
- `/iv <jugador>` — Abre el inventario del jugador indicado; alias `/invsee`.
- `/vanish` — Activa o desactiva el modo invisible para el staff.
- `/warn <jugador> <razón>` — Registra una advertencia en el historial del jugador.
- `/warns <jugador>` — Muestra el historial de advertencias de un jugador.
- `/clearwarns <jugador>` — Elimina todas las advertencias de un jugador.
- `/kick <jugador> [razón]` — Expulsa a un jugador del servidor.
- `/ban <jugador> [razón]` — Banea permanentemente por UUID.
- `/tempban <jugador> <duración> [razón]` — Banea temporalmente por UUID; soporta `30s`, `5m`, `2h`, `7d`.
- `/unban <jugador>` — Elimina el baneo por UUID y cualquier IP asociada.
- `/ipban <jugador> [razón]` — Banea la UUID y la IP actual del jugador.

---

## Chat personalizado

El mod puede formatear el chat y mostrar cabezas de jugador.

- Formato configurable mediante `chat.properties`.
- Hover sobre el nombre para ver la cabeza del jugador.
- Click en el nombre para sugerir `/msg <jugador>`.
- Indicador de cabeza en línea opcional (requiere el mod en el cliente).

---

## Anti-exploit para `/home` y `/warp`

Protecciones configurables para evitar abusos en el teletransporte.

- Cooldown compartido entre `/home` y `/warp`.
- Lista de dimensiones bloqueadas donde no se puede usar el comando.
- Bloqueo temporal tras recibir daño (modo combate).
- El staff con permiso 2 ignora todas estas restricciones.

---

## Persistencia

El mod almacena datos en `<mundo>/data/eazzy_server_utils/`.

- `homes.json` — Hogares de los jugadores.
- `warps.json` — Warps globales.
- `warnings.json` — Historial de advertencias.
- `bans.json` — Baneos registrados.

---

## Configuración adicional

Además de `chat.properties`, el archivo `config/eazzy_server_utils/config.properties` permite ajustar:

- Límites y cooldowns de hogares, warps, TPA, spawn, back, near y head.
- Activar o desactivar el cooldown compartido de `/home`/`/warp`.
- Dimensiones bloqueadas para `/home` y `/warp`.
- Segundos de bloqueo por combate.
