package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class LagCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lag")
                .requires(source -> source.hasPermission(2))
                .executes(LagCommand::execute));

        dispatcher.register(Commands.literal("mspt")
                .requires(source -> source.hasPermission(2))
                .executes(LagCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            MinecraftServer server = context.getSource().getServer();
            long[] tickTimes = server.tickTimes;
            long sum = 0L;
            for (long tickTime : tickTimes) {
                sum += tickTime;
            }
            double mean = (double) sum / (double) tickTimes.length;
            double mspt = mean * 1.0E-6;
            double tps = Math.min(20.0, 1000.0 / mspt);

            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / 1024L / 1024L;
            long totalMemory = runtime.totalMemory() / 1024L / 1024L;
            long freeMemory = runtime.freeMemory() / 1024L / 1024L;
            long usedMemory = totalMemory - freeMemory;

            long tickCount = server.getTickCount();
            String uptime = formatUptime(tickCount);

            int loadedChunks = 0;
            int entityCount = 0;
            AABB all = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
            for (ServerLevel level : server.getAllLevels()) {
                loadedChunks += level.getChunkSource().getLoadedChunksCount();
                entityCount += level.getEntities((Entity) null, all, e -> true).size();
            }

            int online = server.getPlayerCount();
            int max = server.getMaxPlayers();

            MutableComponent message = Component.empty()
                    .append(Component.translatable("command.eazzy_server_utils.lag.header"))
                    .append("\n")
                    .append(Component.translatable("command.eazzy_server_utils.lag.tps", String.format("%.2f", tps)))
                    .append("\n")
                    .append(Component.translatable("command.eazzy_server_utils.lag.mspt", String.format("%.2f", mspt)))
                    .append("\n")
                    .append(Component.translatable("command.eazzy_server_utils.lag.memory", usedMemory, totalMemory, maxMemory))
                    .append("\n")
                    .append(Component.translatable("command.eazzy_server_utils.lag.uptime", uptime))
                    .append("\n")
                    .append(Component.translatable("command.eazzy_server_utils.lag.players", online, max))
                    .append("\n")
                    .append(Component.translatable("command.eazzy_server_utils.lag.chunks", loadedChunks))
                    .append("\n")
                    .append(Component.translatable("command.eazzy_server_utils.lag.entities", entityCount));

            context.getSource().sendSuccess(() -> message, false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static String formatUptime(long ticks) {
        long seconds = ticks / 20L;
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        if (days > 0) {
            return String.format("%dd %02dh %02dm %02ds", days, hours, minutes, secs);
        }
        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, secs);
        }
        if (minutes > 0) {
            return String.format("%dm %02ds", minutes, secs);
        }
        return String.format("%ds", secs);
    }
}
