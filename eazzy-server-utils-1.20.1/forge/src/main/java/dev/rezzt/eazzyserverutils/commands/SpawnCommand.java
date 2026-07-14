package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.rezzt.eazzyserverutils.Config;
import dev.rezzt.eazzyserverutils.managers.BackManager;
import dev.rezzt.eazzyserverutils.managers.CooldownManager;
import dev.rezzt.eazzyserverutils.managers.DimensionLockManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SpawnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn")
                .requires(source -> source.hasPermission(0))
                .executes(SpawnCommand::spawn));
    }

    private static int spawn(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (CooldownManager.isOnCooldown(player.getUUID(), "eazzyserverutils.command.spawn") && !player.hasPermissions(2)) {
                long remaining = CooldownManager.getRemainingSeconds(player.getUUID(), "eazzyserverutils.command.spawn");
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.spawn.cooldown", remaining));
                return 0;
            }
            ServerLevel overworld = context.getSource().getServer().getLevel(Level.OVERWORLD);
            if (overworld == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.spawn.not_found"));
                return 0;
            }
            if (!player.level().dimension().equals(overworld.dimension()) && DimensionLockManager.isLocked(overworld.dimension().location())) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.denied", overworld.dimension().location().toString()));
                return 0;
            }
            BlockPos spawnPos = overworld.getSharedSpawnPos();
            BackManager.setLastLocation(player);
            player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), player.getXRot());
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.spawn.teleport"), false);
            if (!player.hasPermissions(2)) {
                CooldownManager.setCooldown(player.getUUID(), "eazzyserverutils.command.spawn", Config.SPAWN_COOLDOWN.get());
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
