package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageCommands {
    private static final Map<UUID, UUID> LAST_REPLY = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("msg")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommands::sendMessage))));

        dispatcher.register(Commands.literal("tell")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommands::sendMessage))));

        dispatcher.register(Commands.literal("reply")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(MessageCommands::replyMessage)));

        dispatcher.register(Commands.literal("r")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(MessageCommands::replyMessage)));
    }

    private static int sendMessage(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer sender = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            String message = StringArgumentType.getString(context, "message");
            sendPrivateMessage(sender, target, message);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int replyMessage(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer sender = context.getSource().getPlayerOrException();
            UUID lastTarget = LAST_REPLY.get(sender.getUUID());
            if (lastTarget == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.msg.no_reply"));
                return 0;
            }
            ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayer(lastTarget);
            if (target == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.msg.player_offline"));
                return 0;
            }
            String message = StringArgumentType.getString(context, "message");
            sendPrivateMessage(sender, target, message);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static void sendPrivateMessage(ServerPlayer sender, ServerPlayer target, String message) {
        LAST_REPLY.put(sender.getUUID(), target.getUUID());
        LAST_REPLY.put(target.getUUID(), sender.getUUID());

        Component senderMsg = Component.translatable("command.eazzy_server_utils.msg.to", target.getDisplayName(), message);
        Component targetMsg = Component.translatable("command.eazzy_server_utils.msg.from", sender.getDisplayName(), message);

        sender.sendSystemMessage(senderMsg);
        target.sendSystemMessage(targetMsg);
    }
}
