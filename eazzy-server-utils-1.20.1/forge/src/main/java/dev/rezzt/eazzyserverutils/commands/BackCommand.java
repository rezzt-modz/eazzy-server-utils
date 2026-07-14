package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.rezzt.eazzyserverutils.Config;
import dev.rezzt.eazzyserverutils.managers.BackManager;
import dev.rezzt.eazzyserverutils.managers.CooldownManager;
import dev.rezzt.eazzyserverutils.managers.DimensionLockManager;
import dev.rezzt.eazzyserverutils.storage.SavedLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class BackCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("back")
                .requires(source -> source.hasPermission(0))
                .executes(BackCommand::back));
    }

    private static int back(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (CooldownManager.isOnCooldown(player.getUUID(), "eazzyserverutils.command.back") && !player.hasPermissions(2)) {
                long remaining = CooldownManager.getRemainingSeconds(player.getUUID(), "eazzyserverutils.command.back");
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.back.cooldown", remaining));
                return 0;
            }
            SavedLocation location = BackManager.getBackLocation(player);
            if (location == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.back.no_location"));
                return 0;
            }
            ServerLevel level = context.getSource().getServer().getLevel(location.getDimensionKey());
            if (level == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.back.dimension_not_found"));
                return 0;
            }
            if (!player.level().dimension().equals(level.dimension()) && DimensionLockManager.isLocked(level.dimension().location())) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.denied", level.dimension().location().toString()));
                return 0;
            }
            BackManager.setLastLocation(player);
            player.teleportTo(level, location.x, location.y, location.z, location.yaw, location.pitch);
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.back.teleport"), false);
            if (!player.hasPermissions(2)) {
                CooldownManager.setCooldown(player.getUUID(), "eazzyserverutils.command.back", Config.BACK_COOLDOWN.get());
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
