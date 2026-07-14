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
import dev.rezzt.eazzyserverutils.storage.SavedLocation;
import dev.rezzt.eazzyserverutils.storage.WarpManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WarpCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setwarp")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("name", StringArgumentType.word()).executes(WarpCommands::setWarp)));

        dispatcher.register(Commands.literal("warp")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            WarpManager.getWarps().forEach(w -> builder.suggest(w.name));
                            return builder.buildFuture();
                        })
                        .executes(WarpCommands::warp)));

        dispatcher.register(Commands.literal("warps")
                .requires(source -> source.hasPermission(0))
                .executes(WarpCommands::listWarps));
    }

    private static int setWarp(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            SavedLocation newWarp = SavedLocation.fromPlayer(name, player);
            if (WarpManager.setWarp(newWarp)) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.warp.set", name), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.warp.limit", Config.MAX_WARPS.get()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int warp(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!canUseHomeWarp(player, context)) {
                return 0;
            }
            SavedLocation warp = WarpManager.getWarp(name);
            if (warp == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.warp.not_found", name));
                return 0;
            }
            ServerLevel level = context.getSource().getServer().getLevel(warp.getDimensionKey());
            if (level == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.warp.dimension_not_found"));
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
            player.teleportTo(level, warp.x, warp.y, warp.z, warp.yaw, warp.pitch);
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.warp.teleport", name), false);
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
        return Config.HOME_WARP_SHARED_COOLDOWN.get() ? "eazzyserverutils.command.homewarp" : "warp";
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

    private static int listWarps(CommandContext<CommandSourceStack> context) {
        List<SavedLocation> warps = WarpManager.getWarps();
        if (warps.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.warp.list.empty"), false);
            return 1;
        }
        String list = warps.stream().map(w -> w.name).collect(Collectors.joining("§7, §b"));
        context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.warp.list", list), false);
        return 1;
    }
}
