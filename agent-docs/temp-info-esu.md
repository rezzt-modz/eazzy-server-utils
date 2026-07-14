#  **MODS PROJECTS  |  EAZZY SERVER UTILS**
> pagina de informacion sobre el proyecto de "eazzy server utils", informacion general y tecnica sobre el desarrollo del proyecto.
---
##   **EAZZY SERVER UTILS  |  INFORMACION GENERAL**
<div align="justify">  
mod de neoforge para minecraft 1.21.8 que esta diseñado para servidores multijugador tanto personales como profesionales, agrega utilidades tanto para los jugadores normales como para los administradores o staff del servidor. el mod no tiene una funcionalidad principal como tal, sino que se compone de varios comandos que se pueden usar por parte de los jugadores para staff o jugar de manera normal.  
<br><br>
se pueden diferenciar los comandos en diferentes categorias: homes y warps, heads, player utils y teleportation, staff y administration. las categorias se van a utilizar para poder ir organizando mejor los comandos en el entorno de desarrollo. hay diferenciaciones entre el acceso a los comandos que van a tener los jugadores normales y el acceso que va a tener el staff.  
</div>
---
##   **EAZZY SERVER UTILS  |  INFORMACION ADICIONAL**
<div align="justify">  
dentro del mod se tienen que diferenciar entre el acceso a los comandos de los usuarios normales y el acceso a los comandos que van a tener los administradores y el staff del servidor. las categorias dentro de las que se diferencian todos los comandos son los siguientes:  
<br><br>
<b>home y warp</b> (/sethome "name", /homes /home "name" - todos los usuarios tendran acceso completo a los comandos - maximo de 8 homes por usuario) (/setwarp "name" - acceso solo para administradores | /warps /warp "name" - acceso completo a los usuarios, los warps son accesibles para todo los usuarios). crean puntos a los que los usuarios se pueden teletransportar en cualquier momento, teniendo un cooldown de 5 segundos.  
<br><br>
<b>heads</b> (/head "player" - obtiene la cabeza del jugador seleccionado - acceso unico para administrador sin cooldown). obtiene la cabeza del jugador que queramos, ya este dentro del servidor como fuera, mientras que sea un jugador de minecraft. los usuarios podran hacer uso del comando con un cooldown de 1h por ejecucion, de esta manera no se podra saturar el servidor con tantas peticiones.  
<br><br>
<b>player utils y teleportation</b> (/crafting /anvil /smithing - acceso a las interfaces de las mesas de trabajo vanilla) (/tpa /tpaccept /tpacancel - peticiones de teletransporte entre jugadores - cooldown de 3 segundos por peticion). todos los usuarios tendran acceso a estos comandos, tanto de utilidad como de teletransporte. se tendra acceso a las interfaces de las mesas de trabajo para poder hacer uso en cualquier momento, tambien se habilitara la funcionalidad de teletransporte entre jugadores con peticiones.  
<br><br>
<b>staff y administration</b> (/tps /freeze /iv "player" - solo la administracion tendra acceso a esta lista de comandos). los comando que componen esta categoria con utilizados para poder acceder a los tps del servidor, congelar a un jugador por un periodo de 10 segundos y poder abrir el inventario de cualquier jugador - este inventario sera unicamente vanilla, en el caso de que haya mods que amplifiquen este inventario se tendra que buscar otra manera.  
</div>
---
##   **EAZZY SERVER UTILS  |  INFORMACION TECNICA**
<div align="justify">  
a continuacion esta la informacion tecnica del mod, la que se va a usar para poder agregar el proyecto dentro del fichero json de la pagina de publicacion de mods:  
</div>
- **icon:** dns **|** **titulo:** eazzy server utils **|** **launcher:** neoforge **|** **version:** 1.0.0
- **descripcion:** eazzy server utils is a neoforge mod for minecraft 1.21.8 that adds essential utilities for multiplayer servers, serving both regular players and staff with categorized commands.
- **problema:** multiplayer servers often lack quick-access tools for homes, teleports, and admin functions, which slows down gameplay and server management.
- **solucion:** the mod offers organized command categories with cooldowns, permission levels, and features like homes, warps, heads, player utils, and staff tools for smooth operations.
- **specs:** neoforge 1.21.1+ (minecraft 1.21.8) **|** **dependencia:** neoforge **|** **entorno:** client + server
---
