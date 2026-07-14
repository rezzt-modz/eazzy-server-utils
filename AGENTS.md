# Eazzy Server Utils — Agent Guide

This guide is written for AI coding agents that need to work on the `eazzy-server-utils` project. It reflects the actual state of the repository. Do not assume standard Minecraft mod layouts; read this first.

---

## Project Overview

`eazzy-server-utils` is a Minecraft server-utility mod that adds teleportation, staff, and quality-of-life commands. The repository root contains **two separate version directories**:

| Directory | Contents | Status |
|---|---|---|
| `eazzy-server-utils-1.20.1/` | Full Gradle-based source project for Minecraft 1.20.1 | Buildable source of truth |
| `eazzy-server-utils-1.21.8/` | Extracted/built NeoForge mod JAR (`.class` files + resources) | Compiled artifact only — **not source** |

There is no shared source between the two directories in Git. The 1.21.8 artifact appears to have been built from a separate, currently absent source tree that mirrors the package layout of the 1.20.1 project.

### What the mod does

The mod registers Brigadier commands and Forge/NeoForge event handlers for:

* **Homes**: `/sethome [name]`, `/home [name]`, `/homes`, `/delhome <name>`, `/renamehome <old> <new>`, `/homepublic <name>`
* **Warps**: `/setwarp <name>`, `/warp <name>`, `/warps`
* **TPA**: `/tpa <player>`, `/tpahere <player>`, `/tpaccept`, `/tpacancel`
* **Movement**: `/back`, `/spawn`, `/near`
* **Messaging**: `/msg <player> <message>`, `/reply <message>`
* **Utility GUIs**: `/crafting`, `/anvil`, `/smithing`
* **Fun**: `/head <player>`
* **Staff**: `/tps`, `/freeze <player>`, `/unfreeze <player>`, `/iv <player>` (inventory see)
* **Server management**: `/broadcast <message>`, `/announce <message>`, `/lockchat`, `/lag`, `/mspt`
* **Dimension control**: `/eazzyserverutils close <dimension>`, `/eazzyserverutils open <dimension>`, `/eazzyserverutils list`
* **Chat formatting**: custom chat format with optional inline player heads (requires mod on client)

Features include per-command cooldowns, configurable limits, Gson-based JSON persistence for homes/warps and locked dimensions, a `/freeze` implementation that cancels interactions and snaps the player back to a frozen position every tick, basic anti-exploit checks for `/home` and `/warp` (shared cooldown, blocked dimensions, combat lock), and dimension locking that blocks players from entering closed dimensions via portals or mod teleports.

---

## Technology Stack

### 1.20.1 source project

* **Language**: Java 17
* **Build tool**: Gradle with Groovy convention plugins in `buildSrc/`
* **Minecraft toolchain**: [Unimined](https://github.com/Wagyourtail/Unimined) `1.4.1-SNAPSHOT`
* **Mappings**: Mojang mappings (`mojmap()`)
* **Loader**: Forge `47.4.10`
* **Mod loader framework**: Forge API (`net.minecraftforge.*`)
* **Additional Gradle plugins**:
  * `xyz.wagyourtail.jvmdowngrader` `1.3.0` — downgrades compiled bytecode to the target Java version
  * `xyz.wagyourtail.manifold` `1.0.0-SNAPSHOT` — preprocessor and string interpolation
  * `io.github.pacifistmc.forgix` `2.+` — JAR merging for multi-loader output (`autoRun = false`)
* **Serialization**: Gson
* **Logging**: SLF4J via `com.mojang.logging.LogUtils`
* **Networking**: Forge `SimpleChannel` for client-server packets
* **Mixins**: MixinExtras for client-side chat rendering

### 1.21.8 artifact

* **Loader**: NeoForge (extracted `META-INF/neoforge.mods.toml`)
* **Java**: 21 (`compatibilityLevel` in `eazzy_server_utils.mixins.json`)
* **Mod ID**: `eazzy_server_utils` (note the underscore — different from 1.20.1's `eazzyserverutils`)
* **No build files, no `.java` sources** — do not try to compile this directory.

---

## Repository Layout

```
eazzy-server-utils/
├── .vscode/settings.json
├── agent-docs/                  # empty
├── eazzy-server-utils-1.20.1/   # buildable source project
│   ├── buildSrc/src/main/groovy/ # convention plugins
│   ├── common/src/main/resources/ # shared resources (mixins, pack.mcmeta)
│   ├── forge/src/main/java/dev/rezzt/eazzyserverutils/ # all Java source
│   ├── forge/src/main/resources/ # loader-specific resources + lang files
│   ├── versionProperties/1.20.1.properties
│   ├── gradle.properties
│   ├── settings.gradle
│   └── build.gradle
└── eazzy-server-utils-1.21.8/   # extracted NeoForge JAR (compiled classes only)
    ├── dev/rezzt/eazzyserverutils/
    ├── assets/eazzy_server_utils/lang/
    ├── META-INF/neoforge.mods.toml
    └── eazzy_server_utils.mixins.json
```

### Key files in `eazzy-server-utils-1.20.1`

| File | Purpose |
|---|---|
| `gradle.properties` | `mc_ver`, mod metadata (`mod_name`, `mod_id`, `mod_version`, `group`, `license`), Manifold version, Gradle JVM args |
| `versionProperties/1.20.1.properties` | Java version, Minecraft version, Forge/Fabric/NeoForge loader versions, selected loaders (`builds_for=forge`) |
| `build.properties` | Auto-generated preprocessor definitions (`MC_1_20_1=0`, `MC_VER=0`) produced by `buildSrc/root.gradle` |
| `settings.gradle` | Loads per-version properties, includes `common` and loader subprojects from `builds_for` |
| `build.gradle` | Root plugins (Unimined, JVMDowngrader, Manifold, Forgix) |
| `buildSrc/src/main/groovy/common.gradle` | Java/Maven/JVMDowngrader/Manifold setup shared by all modules |
| `buildSrc/src/main/groovy/minecraft.gradle` | Unimined Minecraft/mappings setup; wires `common` sources/resources into loader subprojects |
| `buildSrc/src/main/groovy/unimined-forge.gradle` | Forge-specific Unimined configuration, mixin config, run config |
| `common/build.gradle` | `unimined-common` plugin; no extra deps |
| `forge/build.gradle` | `unimined-forge` plugin; **disables tests**; also patches the mixin refmap for `ChatComponentMixin` so `GuiGraphics#drawString` is remapped correctly in production |

---

## Code Organization

All source code for 1.20.1 lives under:

```
forge/src/main/java/dev/rezzt/eazzyserverutils/
```

### Packages

| Package | Responsibility |
|---|---|
| `dev.rezzt.eazzyserverutils` | Mod entry point, config, Forge event bus subscribers, permissions |
| `dev.rezzt.eazzyserverutils.commands` | Brigadier command registration and execution |
| `dev.rezzt.eazzyserverutils.managers` | Runtime state: cooldowns, frozen players, vanished players, pending TPA requests, back locations |
| `dev.rezzt.eazzyserverutils.menus` | Container implementations for command GUIs (`InvSeeContainer`) |
| `dev.rezzt.eazzyserverutils.storage` | Gson-backed persistence: `SavedLocation`, `HomeManager`, `WarpManager`, `WarningManager`, `BanManager` |
| `dev.rezzt.eazzyserverutils.network` | Forge networking: packet definitions and handler registration |
| `dev.rezzt.eazzyserverutils.client` | Client-side code: chat packet handling and head rendering |
| `dev.rezzt.eazzyserverutils.mixins` | Client-side Mixin classes for chat rendering |

### Important classes

| Class | Role |
|---|---|
| `EazzyServerUtilsForge` | `@Mod` entry point; loads config and registers `EazzyServerUtils`, `CommonEvents`, `ChatEvents`, `StaffEvents`, `BroadcastCommand` and `DimensionEvents` on the Forge event bus |
| `EazzyServerUtils` | Static mod constants (`MODID`, `LOGGER`) and `ServerStartingEvent` handler that loads `HomeManager`, `WarpManager`, `WarningManager`, `BanManager` and `DimensionLockManager` |
| `FileConfig` | Reads/writes `config/eazzy_server_utils/config.properties` with default values |
| `Config` | `Supplier<Integer>` wrappers around `FileConfig.getInt(...)` for `MAX_HOMES`, `HOME_COOLDOWN`, etc. |
| `CommonEvents` | `PlayerTickEvent` and `PlayerInteractEvent` handlers that enforce `/freeze` |
| `ModCommands` | `RegisterCommandsEvent` subscriber that delegates to each command class |
| `CooldownManager` | In-memory per-player, per-command cooldown store |
| `CombatManager` | Records last damage time per player for the `/home`/`/warp` combat lock |
| `ChatLockManager` | Tracks whether `/lockchat` is currently active |
| `DimensionLockManager` | Gson JSON persistence of locked dimensions; resolves aliases (`nether`, `end`, etc.) and validates dimensions against the server's level keys |
| `BackManager` | Stores last teleport/death locations for `/back` |
| `HomeManager` / `WarpManager` | Gson JSON persistence in `<world>/data/eazzy_server_utils/` |
| `WarningManager` / `BanManager` | Gson JSON persistence for player warnings and bans in `<world>/data/eazzy_server_utils/` |
| `VanishManager` | In-memory set of staff players currently in vanish |
| `StaffEvents` | Forge event subscriber that enforces bans on `PlayerLoggedInEvent` and cleans up vanish on logout |
| `PunishmentCommands` | Brigadier registration for `/warn`, `/warns`, `/clearwarns`, `/kick`, `/ban`, `/tempban`, `/unban`, `/ipban` |
| `DimensionCommand` | Brigadier registration for `/eazzyserverutils close <dimension>`, `/eazzyserverutils open <dimension>` and `/eazzyserverutils list` |
| `DimensionEvents` | `EntityTravelToDimensionEvent` handler that cancels travel into locked dimensions and shows a colored action-bar message |
| `TimeUtils` | Parses duration strings like `30s`, `5m`, `2h`, `7d` |
| `ESUPacketHandler` / `ChatHeadPacket` | Forge networking for chat-with-heads feature |
| `ChatEvents` | `ServerChatEvent` subscriber that formats chat and sends custom packets |
| `ChatConfig` | Separate `chat.properties` config for chat formatting |
| `Permissions` | String constants for permission nodes; used as hard-coded permission checks (not integrated with a permissions mod) |

### Commands reference

| Command | Permission level | Cooldown key | Notes |
|---|---|---|---|
| `/sethome [name]` | 0 | — | default name `"home"`; respects `MAX_HOMES` |
| `/home [name]` | 0 | `eazzyserverutils.command.home` (or shared `eazzyserverutils.command.homewarp`) | suggests player homes; blocked in combat and in blocked dimensions |
| `/homes` | 0 | — | lists homes |
| `/setwarp <name>` | 2 | — | respects `MAX_WARPS` |
| `/warp <name>` | 0 | `warp` (or shared `eazzyserverutils.command.homewarp`) | suggests warps; blocked in combat and in blocked dimensions |
| `/warps` | 0 | — | lists warps |
| `/tpa <player>` | 0 | `eazzyserverutils.command.tpa` | sends a request; respects `TPA_TIMEOUT` |
| `/tpahere <player>` | 0 | `eazzyserverutils.command.tpa` | asks target to teleport to sender |
| `/tpaccept` | 0 | — | teleports sender to receiver |
| `/tpacancel` | 0 | — | denies pending request |
| `/back` | 0 | `eazzyserverutils.command.back` | returns to last death/teleport location |
| `/spawn` | 0 | `eazzyserverutils.command.spawn` | teleports to overworld spawn |
| `/near` | 0 | — | lists players within `nearRadius` blocks |
| `/msg <player> <message>` | 0 | — | private message; aliases `/tell` |
| `/reply <message>` | 0 | — | reply to last private message; alias `/r` |
| `/delhome <name>` | 0 | — | deletes a home |
| `/renamehome <old> <new>` | 0 | — | renames a home |
| `/homepublic <name>` | 0 | — | toggles home public/private |
| `/head <player>` | 0 | `eazzyserverutils.command.head` | fetches GameProfile async; cooldown in minutes display |
| `/broadcast <message>` | 2 | — | centered title/subtitle; `|` separates title, subtitle and chat message; `&` is treated as `§`; chat part is sent 3 seconds later |
| `/announce <message>` | 2 | — | alias of `/broadcast` |
| `/lockchat` | 2 | — | toggles global chat lock; non-staff cannot chat while locked |
| `/eazzyserverutils close <dimension>` | 2 | — | locks a dimension; accepts aliases (`nether`, `end`) and full dimension IDs; persists in world data |
| `/eazzyserverutils open <dimension>` | 2 | — | unlocks a dimension; accepts aliases and full dimension IDs |
| `/eazzyserverutils list` | 2 | — | lists currently locked dimensions |
| `/lag` | 2 | — | private detailed performance report (TPS, MSPT, memory, uptime, players, chunks, entities) |
| `/mspt` | 2 | — | alias of `/lag` |
| `/crafting` | 0 | — | portable crafting table |
| `/anvil` | 0 | — | portable anvil |
| `/smithing` | 0 | — | portable smithing table |
| `/tps` | 2 | — | computes TPS from `MinecraftServer.tickTimes` |
| `/freeze <player>` | 2 | — | freezes until `/unfreeze` |
| `/unfreeze <player>` | 2 | — | unfreezes player |
| `/iv <player>` | 2 | — | opens target inventory (`/invsee` alias) |
| `/vanish` | 2 | `eazzyserverutils.command.vanish` | toggles invisibility for the executing staff member |
| `/warn <player> <reason>` | 2 | `eazzyserverutils.command.warn` | records a warning in JSON history; notifies target if online |
| `/warns <player>` | 2 | `eazzyserverutils.command.warns` | lists warning history for a player |
| `/clearwarns <player>` | 2 | `eazzyserverutils.command.clearwarns` | clears all warnings for a player |
| `/kick <player> [reason]` | 2 | `eazzyserverutils.command.kick` | disconnects an online player |
| `/ban <player> [reason]` | 2 | `eazzyserverutils.command.ban` | permanent UUID ban; disconnects if online |
| `/tempban <player> <duration> [reason]` | 2 | `eazzyserverutils.command.tempban` | temporary UUID ban; duration format `30s`, `5m`, `2h`, `7d` |
| `/unban <player>` | 2 | `eazzyserverutils.command.unban` | removes UUID and any associated IP ban |
| `/ipban <player> [reason]` | 2 | `eazzyserverutils.command.ipban` | bans the player's UUID and current IP |

Note: Players with permission level `>= 2` bypass most cooldowns.

---

## Build and Run Commands

All build commands run from `eazzy-server-utils-1.20.1/`.

```bash
cd eazzy-server-utils-1.20.1

# Build the Forge JAR
./gradlew :forge:build

# Run a client or server for testing
./gradlew :forge:runClient
./gradlew :forge:runServer

# Build everything (including any configured loaders)
./gradlew build
```

### How versioning works

* `gradle.properties` sets `mc_ver=1.20.1`.
* `settings.gradle` reads `versionProperties/${mc_ver}.properties` and exposes values as Gradle extra properties.
* Mod version is `${mod_version}-${minecraft_version}`, e.g. `1.0.0-1.20.1`.
* To build for a different supported version, add a new `.properties` file under `versionProperties/` and change `mc_ver`.

### Build outputs

After a successful build, relevant JARs are:

* `forge/build/libs/eazzyserverutils-forge-1.0.0-1.20.1.jar`
* `build/libs/eazzyserverutils--1.0.0-1.20.1.jar` (root/Forgix merged output when run)

---

## Testing

* **There are no unit tests.** The project disables the test source set and task in `forge/build.gradle`:

```groovy
tasks.compileTestJava { enabled = false }
tasks.test { enabled = false }
```

* The only testing workflow is running `./gradlew :forge:runClient` or `:forge:runServer` and exercising commands manually.
* When adding features, prefer manual in-game verification because there is no automated test harness.

---

## Development Conventions

### Code style

* Indentation: 4 spaces (observed in all Java files).
* Package naming: `dev.rezzt.eazzyserverutils.*`.
* Static command registration: each command class exposes `public static void register(CommandDispatcher<CommandSourceStack>)`.
* Static manager classes: `HomeManager`, `WarpManager`, `CooldownManager`, `FreezeManager`, `TeleportRequestManager` expose static methods.
* Translatable messages: all user-facing strings use `Component.translatable("command.eazzy_server_utils...")`; translations live in `assets/eazzyserverutils/lang/en_us.json` and `es_es.json`.
* Error handling: commands wrap execution in `try/catch` and send `Component.literal(e.getMessage())` on failure.

### Configuration

* Config lives in `config/eazzy_server_utils/config.properties` at runtime.
* Default values are duplicated in both `FileConfig.createDefaultConfig()` and `Config.java` fallback defaults.
* Available keys in `config.properties`: `maxHomes`, `homeCooldown`, `maxWarps`, `warpCooldown`, `tpaCooldown`, `tpaTimeout`, `headCooldown`, `spawnCooldown`, `backCooldown`, `nearRadius`, `homeWarpSharedCooldown`, `homeWarpCooldown`, `homeWarpBlockedDimensions`, `combatCooldownSeconds`.
* Chat formatting has a separate config file: `config/eazzy_server_utils/chat.properties`.
* Available keys in `chat.properties`: `enabled`, `format`, `showHeadHover`, `showHeadIndicator`, `headIndicator`, `clickNameToMessage`, `showHeadInline`.

### Persistence

* Homes and warps are stored as pretty-printed Gson JSON in `<world>/data/eazzy_server_utils/homes.json` and `warps.json`.
* Warnings and bans are stored as pretty-printed Gson JSON in `<world>/data/eazzy_server_utils/warnings.json` and `bans.json`.
* Locked dimensions are stored as pretty-printed Gson JSON in `<world>/data/eazzy_server_utils/locked_dimensions.json`.
* `SavedLocation` stores name, dimension ResourceLocation string, coordinates, and rotation.
* `BanEntry` stores target UUID/name, issuer UUID/name, reason, issue time, expiry time, optional IP, and ban type (`BAN`, `TEMPBAN`, `IPBAN`).

### Mixins

* The mixin config (`eazzyserverutils.mixins.json`) now ships client-only mixins for chat head rendering.
* Client mixins: `ChatComponentMixin` (renders heads next to chat lines) and `ChatComponentAccessor` (exposes `allMessages`).
* Mixin configuration is wired in `unimined-forge.gradle`; MixinExtras is enabled and declared as a dependency in `forge/build.gradle`.
* **Refmap workaround:** Unimined 1.4.1-SNAPSHOT does not remap `@At` target descriptors for MixinExtras injectors. `forge/build.gradle` patches the generated refmap after `remapJar` so `GuiGraphics#drawString` maps to its SRG name `m_280648_`. If you add more `@At` targets that hit obfuscated methods, you must extend that patch.

### Preprocessor

* Manifold preprocessor definitions are auto-generated into `build.properties` by `buildSrc/root.gradle`.
* Definitions look like `MC_1_20_1=0`, `MC_VER=0` and can be used for version-conditional code when the project expands to multiple versions.

---

## Deployment / Publishing

* There is **no publishing configuration** (no Maven repo, no CurseForge, no Modrinth, no GitHub releases) in the visible Gradle scripts.
* Deployment is currently manual: build the JAR and copy it to a server mods folder.
* The 1.21.8 directory is a pre-built artifact; its deployment process is not captured in this repository.

---

## Security Considerations

* Permission checks use vanilla `hasPermission(0)` / `hasPermission(2)`. There is no integration with LuckPerms, ForgePermissions, or similar.
* `/invsee` (`/iv`) opens the target player's inventory through a custom `InvSeeContainer` whose `stillValid(Player)` returns `true`, so it does not re-close when players move. Ensure only trusted staff receive permission level 2.
* `/freeze` teleports the target back to a saved position every server tick and cancels all tracked interaction events. It does not currently persist across restarts or dimension changes (it intentionally ignores dimension mismatches to avoid unsafe teleports).
* `/head` performs an async GameProfile lookup via `server.getProfileRepository()` and runs the resulting item give on the main server thread.
* The TPA system stores pending requests in memory only and removes them on expiration, logout, accept, or cancel.
* `/vanish` applies a long-duration invisibility effect and tracks vanished staff in memory; it does not currently hide players from the tab list or packet-level tracking.
* `/warn`, `/ban`, `/tempban`, `/unban`, and `/ipban` rely on the server profile cache to resolve offline player names. If the cache has expired or the name has never joined, the command reports "player not found".
* `/broadcast` schedules chat follow-up messages in a static queue processed by a `ServerTickEvent` handler. The queue is cleared when the server process restarts.
* `/lockchat` only blocks chat sent through the vanilla `ServerChatEvent`; it does not block command output or system messages.
* `/home` and `/warp` share a cooldown when `homeWarpSharedCooldown=true`, can be restricted by dimension via `homeWarpBlockedDimensions`, and are blocked for `combatCooldownSeconds` after the player takes damage. Staff (permission level `>= 2`) bypass these checks.
* `/eazzyserverutils close <dimension>` blocks all players from entering the locked dimension through portals, vanilla dimension travel, or mod teleports that fire `EntityTravelToDimensionEvent`. The mod's own teleport commands (`/home`, `/warp`, `/back`, `/spawn`, `/tpaccept`) also check the lock before attempting cross-dimensional travel. Staff and singleplayer operators are no longer exempt; to enter a closed dimension they must open it first.
* `/ipban` captures the player's current IP at execution time; offline players cannot be IP-banned by this command.
* Ban and warning data is written to the world folder; copying or backing up the world folder also backs up sanctions.
* No input sanitization beyond Brigadier argument types; command names, player names, and reasons are used directly in JSON and translation keys.
* Chat messages are now sent via a custom Forge packet for formatted output and inline heads. Players must have the mod on the client to see chat messages; the mod is intended to be installed on both client and server.

---

## Important Notes for Agents

1. **Do not edit `eazzy-server-utils-1.21.8/` expecting compilation.** It is an extracted JAR. If you need to change 1.21.8 behavior, there is no source here to modify.
2. **The real source is in `eazzy-server-utils-1.20.1/forge/src/main/java/`.** The `common/` module currently holds only resources, not Java code.
3. **Always run Gradle from the `eazzy-server-utils-1.20.1` directory**, not the repository root.
4. **There are no tests.** Verify changes by building and running the Forge client/server.
5. The mod ID differs between versions (`eazzyserverutils` for 1.20.1 vs `eazzy_server_utils` for 1.21.8). If you ever create a multi-version source tree, reconcile this inconsistency carefully.
