package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.rezzt.eazzyserverutils.Config;
import dev.rezzt.eazzyserverutils.managers.BackManager;
import dev.rezzt.eazzyserverutils.managers.CombatManager;
import dev.rezzt.eazzyserverutils.managers.CooldownManager;
import dev.rezzt.eazzyserverutils.managers.DimensionLockManager;
import dev.rezzt.eazzyserverutils.storage.HomeManager;
import dev.rezzt.eazzyserverutils.storage.SavedLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HomeCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(((LiteralArgumentBuilder<CommandSourceStack>) ((LiteralArgumentBuilder<CommandSourceStack>) Commands.literal("sethome")
                .requires(source -> source.hasPermission(0)))
                .then(Commands.argument("name", StringArgumentType.word()).executes(HomeCommands::setHome)))
                .executes(context -> HomeCommands.setHome(context, "home")));

        dispatcher.register(((LiteralArgumentBuilder<CommandSourceStack>) ((LiteralArgumentBuilder<CommandSourceStack>) Commands.literal("home")
                .requires(source -> source.hasPermission(0)))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player != null) {
                                HomeManager.getHomes(player.getUUID()).forEach(h -> builder.suggest(h.name));
                            }
                            return builder.buildFuture();
                        })
                        .executes(HomeCommands::home)))
                .executes(context -> HomeCommands.home(context, "home")));

        dispatcher.register(Commands.literal("homes")
                .requires(source -> source.hasPermission(0))
                .executes(HomeCommands::listHomes));

        dispatcher.register(Commands.literal("delhome")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(homeSuggestionProvider())
                        .executes(HomeCommands::deleteHome)));

        dispatcher.register(Commands.literal("renamehome")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("oldName", StringArgumentType.word())
                        .suggests(homeSuggestionProvider())
                        .then(Commands.argument("newName", StringArgumentType.word())
                                .executes(HomeCommands::renameHome))));

        dispatcher.register(Commands.literal("homepublic")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(homeSuggestionProvider())
                        .executes(HomeCommands::togglePublicHome)));
    }

    private static com.mojang.brigadier.suggestion.SuggestionProvider<CommandSourceStack> homeSuggestionProvider() {
        return (context, builder) -> {
            ServerPlayer player = context.getSource().getPlayer();
            if (player != null) {
                HomeManager.getHomes(player.getUUID()).forEach(h -> builder.suggest(h.name));
            }
            return builder.buildFuture();
        };
    }

    private static int setHome(CommandContext<CommandSourceStack> context) {
        return setHome(context, StringArgumentType.getString(context, "name"));
    }

    private static int setHome(CommandContext<CommandSourceStack> context, String name) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            SavedLocation newHome = SavedLocation.fromPlayer(name, player);
            if (HomeManager.addHome(player.getUUID(), newHome)) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.home.set", name), false);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home.limit", Config.MAX_HOMES.get()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int home(CommandContext<CommandSourceStack> context) {
        return home(context, StringArgumentType.getString(context, "name"));
    }

    private static int home(CommandContext<CommandSourceStack> context, String name) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!canUseHomeWarp(player, context)) {
                return 0;
            }
            SavedLocation home = HomeManager.getHome(player.getUUID(), name);
            if (home == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home.not_found", name));
                return 0;
            }
            ServerLevel level = context.getSource().getServer().getLevel(home.getDimensionKey());
            if (level == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home.dimension_not_found"));
                return 0;
            }
            if (isDimensionBlocked(level.dimension().location())) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home_warp.blocked_dimension"));
                return 0;
            }
            if (!player.level().dimension().equals(level.dimension()) && DimensionLockManager.isLocked(level.dimension().location())) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.denied", level.dimension().location().toString()));
                return 0;
            }
            BackManager.setLastLocation(player);
            player.teleportTo(level, home.x, home.y, home.z, home.yaw, home.pitch);
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.home.teleport", name), false);
            applyHomeWarpCooldown(player);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static boolean canUseHomeWarp(ServerPlayer player, CommandContext<CommandSourceStack> context) {
        if (player.hasPermissions(2)) {
            return true;
        }
        if (CooldownManager.isOnCooldown(player.getUUID(), getHomeWarpCooldownKey())) {
            long remaining = CooldownManager.getRemainingSeconds(player.getUUID(), getHomeWarpCooldownKey());
            context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home_warp.cooldown", remaining));
            return false;
        }
        if (CombatManager.isInCombat(player.getUUID(), Config.COMBAT_COOLDOWN_SECONDS.get())) {
            long remaining = CombatManager.getRemainingSeconds(player.getUUID(), Config.COMBAT_COOLDOWN_SECONDS.get());
            context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home_warp.in_combat", remaining));
            return false;
        }
        return true;
    }

    private static void applyHomeWarpCooldown(ServerPlayer player) {
        if (player.hasPermissions(2)) {
            return;
        }
        CooldownManager.setCooldown(player.getUUID(), getHomeWarpCooldownKey(), Config.HOME_WARP_COOLDOWN.get());
    }

    private static String getHomeWarpCooldownKey() {
        return Config.HOME_WARP_SHARED_COOLDOWN.get() ? "eazzyserverutils.command.homewarp" : "eazzyserverutils.command.home";
    }

    private static boolean isDimensionBlocked(ResourceLocation dimension) {
        String raw = Config.HOME_WARP_BLOCKED_DIMENSIONS.get();
        if (raw.isEmpty()) {
            return false;
        }
        String dimString = dimension.toString();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(dimString::equalsIgnoreCase);
    }

    private static int listHomes(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            List<SavedLocation> homes = HomeManager.getHomes(player.getUUID());
            if (homes.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.home.list.empty"), false);
                return 1;
            }
            String list = homes.stream().map(h -> h.name + (h.isPublic ? " §7[§aP§7]" : "")).collect(Collectors.joining("§7, §b"));
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.home.list", list), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int deleteHome(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(context, "name");
            if (HomeManager.deleteHome(player.getUUID(), name)) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.home.deleted", name), false);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home.not_found", name));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int renameHome(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String oldName = StringArgumentType.getString(context, "oldName");
            String newName = StringArgumentType.getString(context, "newName");
            if (HomeManager.renameHome(player.getUUID(), oldName, newName)) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.home.renamed", oldName, newName), false);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home.rename_failed", oldName, newName));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int togglePublicHome(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(context, "name");
            Boolean isPublic = HomeManager.togglePublicHome(player.getUUID(), name);
            if (isPublic == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.home.not_found", name));
                return 0;
            }
            context.getSource().sendSuccess(() -> Component.translatable(isPublic ? "command.eazzy_server_utils.home.public_enabled" : "command.eazzy_server_utils.home.public_disabled", name), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
