# **MODS PROJECTS  |  EAZZY SERVER UTILS**
> pagina de informacion sobre el proyecto de "eazzy server utils". informacion general y tecnica sobre el desarrollo del mod, sus comandos y su stack tecnologico.

---

## **EAZZY SERVER UTILS  |  INFORMACION GENERAL**

<div align="justify">

mod de forge para minecraft 1.20.1 disenado para servidores multijugador, tanto personales como profesionales. agrega utilidades para jugadores normales y para el staff o administracion del servidor. el mod no tiene una unica funcionalidad principal, sino que se compone de varios comandos organizados en categorias: homes y warps, heads, player utils y teleportation, staff y administration.

</div>

---

## **EAZZY SERVER UTILS  |  CATEGORIAS DE COMANDOS**

<div align="justify">

a continuacion se detallan las categorias en las que se organizan los comandos del mod. cada categoria agrupa funcionalidades similares y define el nivel de acceso de los usuarios.

</div>

### **HOMES Y WARPS**
<div align="justify">

<b>homes:</b> /sethome [nombre], /home [nombre], /homes, /delhome <nombre>, /renamehome <viejo> <nuevo>, /homepublic <nombre>. los jugadores pueden crear hasta 8 homes por defecto. solo el propietario puede gestionar sus homes, aunque los homes publicos pueden ser visitados por otros jugadores.

<b>warps:</b> /setwarp <nombre> (solo administradores), /warp <nombre>, /warps. los warps son puntos globales del servidor accesibles para todos los usuarios.

los comandos de home y warp comparten un cooldown configurable de 5 segundos por defecto. estan bloqueados en combate y pueden restringirse por dimension.

</div>

### **HEADS**
<div align="justify">

<b>/head <jugador>:</b> obtiene la cabeza del jugador indicado, este dentro o fuera del servidor. los administradores pueden usarlo sin cooldown. los jugadores normales tienen un cooldown de 1 hora por ejecucion para evitar saturar el servidor con peticiones.

</div>

### **PLAYER UTILS Y TELEPORTATION**
<div align="justify">

<b>utilidades:</b> /crafting, /anvil y /smithing abren las interfaces de las mesas de trabajo vanilla en cualquier momento.

<b>teletransporte:</b> /tpa <jugador>, /tpahere <jugador>, /tpaccept y /tpacancel gestionan peticiones de teletransporte entre jugadores con un cooldown de 3 segundos por peticion.

<b>movimiento:</b> /back regresa al ultimo punto de muerte o teletransporte, /spawn lleva al spawn del overworld y /near lista los jugadores cercanos dentro del radio configurado.

<b>mensajeria:</b> /msg <jugador> <mensaje> y /reply <mensaje> permiten enviar mensajes privados.

</div>

### **STAFF Y ADMINISTRATION**
<div align="justify">

<b>herramientas de staff:</b> /tps muestra los ticks por segundo del servidor, /freeze <jugador> y /unfreeze <jugador> congelan y descongelan a un jugador, /iv <jugador> (alias /invsee) abre el inventario vanilla del jugador seleccionado.

<b>gestion del servidor:</b> /broadcast <mensaje> y /announce <mensaje> envian anuncios centrados, /lockchat bloquea el chat global, /lag y /mspt muestran un informe detallado de rendimiento.

<b>sanciones:</b> /warn, /warns, /clearwarns, /kick, /ban, /tempban, /unban e /ipban gestionan advertencias, expulsiones y bloqueos de jugadores.

<b>vanish:</b> /vanish otorga invisibilidad al miembro del staff que lo ejecuta.

</div>

---

## **EAZZY SERVER UTILS  |  LISTA COMPLETA DE COMANDOS**

<div align="justify">

tabla de referencia con todos los comandos, su nivel de permiso, cooldown y notas principales.

</div>

| comando | permiso | cooldown | notas |
|---|---|---|---|
| /sethome [nombre] | 0 | no | maximo de homes configurable |
| /home [nombre] | 0 | home / homewarp | bloqueado en combate y dimensiones restringidas |
| /homes | 0 | no | lista las homes del jugador |
| /delhome <nombre> | 0 | no | elimina una home |
| /renamehome <viejo> <nuevo> | 0 | no | renombra una home |
| /homepublic <nombre> | 0 | no | alterna entre publica y privada |
| /setwarp <nombre> | 2 | no | maximo de warps configurable |
| /warp <nombre> | 0 | warp / homewarp | bloqueado en combate y dimensiones restringidas |
| /warps | 0 | no | lista los warps del servidor |
| /tpa <jugador> | 0 | tpa | envia una peticion de teletransporte |
| /tpahere <jugador> | 0 | tpa | pide al objetivo que se teletransporte al emisor |
| /tpaccept | 0 | no | acepta la peticion pendiente |
| /tpacancel | 0 | no | deniega la peticion pendiente |
| /back | 0 | back | regresa a la ultima ubicacion de muerte o teletransporte |
| /spawn | 0 | spawn | teletransporta al spawn del overworld |
| /near | 0 | no | lista jugadores dentro del radio configurado |
| /msg <jugador> <mensaje> | 0 | no | mensaje privado |
| /reply <mensaje> | 0 | no | responde al ultimo mensaje privado |
| /head <jugador> | 0 | head | obtiene la cabeza del jugador indicado |
| /broadcast <mensaje> | 2 | no | anuncio con titulo, subtitulo y mensaje de chat |
| /announce <mensaje> | 2 | no | alias de /broadcast |
| /lockchat | 2 | no | bloquea o desbloquea el chat global |
| /lag | 2 | no | informe detallado de rendimiento |
| /mspt | 2 | no | alias de /lag |
| /crafting | 0 | no | mesa de crafteo portable |
| /anvil | 0 | no | yunque portable |
| /smithing | 0 | no | mesa de herreria portable |
| /tps | 2 | no | muestra los tps del servidor |
| /freeze <jugador> | 2 | no | congela al jugador indicado |
| /unfreeze <jugador> | 2 | no | descongela al jugador indicado |
| /iv <jugador> | 2 | no | abre el inventario del jugador |
| /vanish | 2 | vanish | activa o desactiva la invisibilidad del staff |
| /warn <jugador> <razon> | 2 | warn | registra una advertencia |
| /warns <jugador> | 2 | warns | muestra el historial de advertencias |
| /clearwarns <jugador> | 2 | clearwarns | limpia las advertencias |
| /kick <jugador> [razon] | 2 | kick | expulsa a un jugador online |
| /ban <jugador> [razon] | 2 | ban | baneo permanente por uuid |
| /tempban <jugador> <duracion> [razon] | 2 | tempban | baneo temporal (formatos: 30s, 5m, 2h, 7d) |
| /unban <jugador> | 2 | unban | elimina el baneo por uuid y la ip asociada |
| /ipban <jugador> [razon] | 2 | ipban | banea uuid e ip actual del jugador |

<div align="justify">

<b>nota:</b> los jugadores con nivel de permiso mayor o igual a 2 suelen saltarse la mayoria de cooldowns.

</div>

---

## **EAZZY SERVER UTILS  |  INFORMACION TECNICA**

<div align="justify">

a continuacion se presenta la informacion tecnica del mod necesaria para agregar el proyecto en ficheros json de paginas de publicacion de mods.

</div>

- **icon:** dns **|** **title:** eazzy server utils **|** **loader:** forge **|** **version:** 1.0.0
- **description:** eazzy server utils is a forge mod for minecraft 1.20.1 that adds essential utilities for multiplayer servers, serving both regular players and staff with categorized commands.
- **problem:** multiplayer servers often lack quick-access tools for homes, teleports and admin functions, which slows down gameplay and server management.
- **solution:** the mod offers organized command categories with cooldowns, permission levels and features like homes, warps, heads, player utils and staff tools for smooth operations.
- **specs:** forge 47.4.10 (minecraft 1.20.1) **|** **dependency:** forge **|** **environment:** client + server

---

## **EAZZY SERVER UTILS  |  STACK TECNOLOGICO**

<div align="justify">

el proyecto de desarrollo principal se encuentra en el directorio `eazzy-server-utils-1.20.1/` y utiliza las siguientes tecnologias:

</div>

- **lenguaje:** java 17
- **herramienta de construccion:** gradle con plugins groovy en `buildsrc/`
- **toolchain de minecraft:** unimined 1.4.1-snapshot
- **mapeos:** mojang mappings (`mojmap()`)
- **loader:** forge 47.4.10
- **serializacion:** gson
- **logging:** slf4j via `com.mojang.logging.logutils`
- **networking:** forge `simplechannel`
- **mixins:** mixinextras para renderizado de chat en cliente

<div align="justify">

el directorio `eazzy-server-utils-1.21.8/` contiene unicamente un artefacto extraido de neoforge (clases compiladas y recursos), no codigo fuente. por lo tanto, la version compilable y fuente de verdad del proyecto es la de minecraft 1.20.1 con forge.

</div>

---

## **EAZZY SERVER UTILS  |  ESTRUCTURA DEL REPOSITORIO**

<div align="justify">

el repositorio raiz contiene dos directorios separados:

</div>

- `eazzy-server-utils-1.20.1/`: proyecto gradle completo con el codigo fuente para minecraft 1.20.1.
- `eazzy-server-utils-1.21.8/`: artefacto extraido de neoforge sin codigo fuente ni archivos de compilacion.

<div align="justify">

todo el codigo fuente de la version 1.20.1 se ubica en `forge/src/main/java/dev/rezzt/eazzyserverutils/` y se organiza en los siguientes paquetes:

</div>

- `dev.rezzt.eazzyserverutils`: punto de entrada del mod, configuracion y permisos.
- `dev.rezzt.eazzyserverutils.commands`: registro y ejecucion de comandos con brigadier.
- `dev.rezzt.eazzyserverutils.managers`: estado en tiempo de ejecucion (cooldowns, jugadores congelados, peticiones tpa, etc.).
- `dev.rezzt.eazzyserverutils.menus`: implementaciones de contenedores para interfaces (invsee).
- `dev.rezzt.eazzyserverutils.storage`: persistencia json con gson (homes, warps, advertencias, baneos).
- `dev.rezzt.eazzyserverutils.network`: definicion y registro de paquetes de red de forge.
- `dev.rezzt.eazzyserverutils.client`: codigo del lado del cliente para el chat formateado.
- `dev.rezzt.eazzyserverutils.mixins`: clases mixin del lado del cliente para renderizado de chat.

---

## **EAZZY SERVER UTILS  |  COMANDOS DE COMPILACION**

<div align="justify">

para compilar y probar el mod se deben ejecutar los siguientes comandos desde el directorio `eazzy-server-utils-1.20.1/`:

</div>

```bash
cd eazzy-server-utils-1.20.1

# compilar el jar de forge
./gradlew :forge:build

# ejecutar cliente o servidor de prueba
./gradlew :forge:runclient
./gradlew :forge:runserver

# compilar todo
./gradlew build
```

<div align="justify">

tras una compilacion exitosa, el jar resultante se encuentra en `forge/build/libs/eazzyserverutils-forge-1.0.0-1.20.1.jar`.

</div>
