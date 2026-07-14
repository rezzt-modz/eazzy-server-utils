package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.rezzt.eazzyserverutils.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.stream.Collectors;

public class NearCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("near")
                .requires(source -> source.hasPermission(0))
                .executes(NearCommand::near));
    }

    private static int near(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            double radius = Config.NEAR_RADIUS.get();
            double radiusSq = radius * radius;
            List<ServerPlayer> nearby = context.getSource().getServer().getPlayerList().getPlayers().stream()
                    .filter(p -> !p.getUUID().equals(player.getUUID()))
                    .filter(p -> p.level().dimension().equals(player.level().dimension()))
                    .filter(p -> p.distanceToSqr(player) <= radiusSq)
                    .sorted((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)))
                    .toList();
            if (nearby.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.near.empty", (int) radius), false);
                return 1;
            }
            String list = nearby.stream()
                    .map(p -> p.getDisplayName().getString() + " §8(§7" + (int) Math.sqrt(p.distanceToSqr(player)) + "m§8)")
                    .collect(Collectors.joining("§7, §b"));
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.near.list", nearby.size(), (int) radius, list), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
