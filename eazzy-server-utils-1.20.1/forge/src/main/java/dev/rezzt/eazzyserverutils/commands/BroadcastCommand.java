package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BroadcastCommand {

    private static final List<PendingBroadcast> PENDING = new ArrayList<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("broadcast")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(BroadcastCommand::execute)));

        dispatcher.register(Commands.literal("announce")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(BroadcastCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            String raw = StringArgumentType.getString(context, "message").trim();
            if (raw.isEmpty()) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.broadcast.empty"));
                return 0;
            }

            String[] parts = raw.split("\\|", 3);
            String title = colorize(parts[0].trim());
            String subtitle = parts.length >= 2 ? colorize(parts[1].trim()) : "";
            String chatMessage = parts.length >= 3 ? colorize(parts[2].trim()) : "";

            MinecraftServer server = context.getSource().getServer();
            Component titleComponent = Component.literal(title);
            Component subtitleComponent = Component.literal(subtitle);

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitleComponent));
                player.connection.send(new ClientboundSetTitleTextPacket(titleComponent));
            }

            if (!chatMessage.isEmpty()) {
                PENDING.add(new PendingBroadcast(server, Component.literal(chatMessage), 60));
            }

            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.broadcast.success"), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static String colorize(String input) {
        return input.replace('&', '§');
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Iterator<PendingBroadcast> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            PendingBroadcast pending = iterator.next();
            pending.ticksRemaining--;
            if (pending.ticksRemaining <= 0) {
                MinecraftServer server = pending.server;
                if (server != null) {
                    server.getPlayerList().broadcastSystemMessage(pending.message, false);
                }
                iterator.remove();
            }
        }
    }

    private static class PendingBroadcast {
        final MinecraftServer server;
        final Component message;
        int ticksRemaining;

        PendingBroadcast(MinecraftServer server, Component message, int ticksRemaining) {
            this.server = server;
            this.message = message;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
