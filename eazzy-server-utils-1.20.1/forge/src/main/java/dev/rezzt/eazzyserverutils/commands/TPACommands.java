package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.rezzt.eazzyserverutils.Config;
import dev.rezzt.eazzyserverutils.managers.BackManager;
import dev.rezzt.eazzyserverutils.managers.CooldownManager;
import dev.rezzt.eazzyserverutils.managers.DimensionLockManager;
import dev.rezzt.eazzyserverutils.managers.TeleportRequestManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TPACommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("target", EntityArgument.player()).executes(TPACommands::sendTpaRequest)));

        dispatcher.register(Commands.literal("tpahere")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("target", EntityArgument.player()).executes(TPACommands::sendTpaHereRequest)));

        dispatcher.register(Commands.literal("tpaccept")
                .requires(source -> source.hasPermission(0))
                .executes(TPACommands::acceptRequest));

        dispatcher.register(Commands.literal("tpacancel")
                .requires(source -> source.hasPermission(0))
                .executes(TPACommands::cancelRequest));
    }

    private static int sendTpaRequest(CommandContext<CommandSourceStack> context) {
        return sendRequest(context, TeleportRequestManager.RequestType.TPA,
                "command.eazzy_server_utils.tpa.sent",
                "command.eazzy_server_utils.tpa.received");
    }

    private static int sendTpaHereRequest(CommandContext<CommandSourceStack> context) {
        return sendRequest(context, TeleportRequestManager.RequestType.TPA_HERE,
                "command.eazzy_server_utils.tpahere.sent",
                "command.eazzy_server_utils.tpahere.received");
    }

    private static int sendRequest(CommandContext<CommandSourceStack> context, TeleportRequestManager.RequestType type,
                                   String sentKey, String receivedKey) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            if (player.getUUID().equals(target.getUUID())) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.tpa.self"));
                return 0;
            }
            if (CooldownManager.isOnCooldown(player.getUUID(), "eazzyserverutils.command.tpa") && !player.hasPermissions(2)) {
                long remaining = CooldownManager.getRemainingSeconds(player.getUUID(), "eazzyserverutils.command.tpa");
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.tpa.cooldown", remaining));
                return 0;
            }
            TeleportRequestManager.addRequest(player, target, type);
            context.getSource().sendSuccess(() -> Component.translatable(sentKey, target.getDisplayName(), Config.TPA_TIMEOUT.get()), false);
            target.sendSystemMessage(Component.translatable(receivedKey, player.getDisplayName()));
            if (!player.hasPermissions(2)) {
                CooldownManager.setCooldown(player.getUUID(), "eazzyserverutils.command.tpa", Config.TPA_COOLDOWN.get());
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int acceptRequest(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            TeleportRequestManager.Request request = TeleportRequestManager.getRequest(player.getUUID());
            if (request == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.tpa.no_pending"));
                return 0;
            }
            ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayer(request.sender);
            if (sender == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.error.player_not_found"));
                TeleportRequestManager.removeRequest(player.getUUID());
                return 0;
            }
            if (request.type == TeleportRequestManager.RequestType.TPA) {
                if (!sender.level().dimension().equals(player.level().dimension()) && DimensionLockManager.isLocked(player.level().dimension().location())) {
                    context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.denied", player.level().dimension().location().toString()));
                    return 0;
                }
                BackManager.setLastLocation(sender);
                sender.teleportTo((ServerLevel) player.level(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            } else if (request.type == TeleportRequestManager.RequestType.TPA_HERE) {
                if (!player.level().dimension().equals(sender.level().dimension()) && DimensionLockManager.isLocked(sender.level().dimension().location())) {
                    context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.denied", sender.level().dimension().location().toString()));
                    return 0;
                }
                BackManager.setLastLocation(player);
                player.teleportTo((ServerLevel) sender.level(), sender.getX(), sender.getY(), sender.getZ(), sender.getYRot(), sender.getXRot());
            }
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.tpa.accepted"), false);
            sender.sendSystemMessage(Component.translatable("command.eazzy_server_utils.tpa.accepted_target", player.getDisplayName()));
            TeleportRequestManager.removeRequest(player.getUUID());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int cancelRequest(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            TeleportRequestManager.Request request = TeleportRequestManager.getRequest(player.getUUID());
            if (request == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.tpa.no_pending"));
                return 0;
            }
            ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayer(request.sender);
            if (sender != null) {
                sender.sendSystemMessage(Component.translatable("command.eazzy_server_utils.tpa.denied_target", player.getDisplayName()));
            }
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.tpa.denied"), false);
            TeleportRequestManager.removeRequest(player.getUUID());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
