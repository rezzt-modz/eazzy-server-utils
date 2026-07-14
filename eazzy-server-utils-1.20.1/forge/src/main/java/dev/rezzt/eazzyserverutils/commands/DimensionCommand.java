package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.rezzt.eazzyserverutils.managers.DimensionLockManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.Set;
import java.util.stream.Collectors;

public class DimensionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("eazzyserverutils")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("close")
                        .then(Commands.argument("dimension", StringArgumentType.greedyString())
                                .suggests(dimensionSuggestionProvider())
                                .executes(DimensionCommand::closeDimension)))
                .then(Commands.literal("open")
                        .then(Commands.argument("dimension", StringArgumentType.greedyString())
                                .suggests(dimensionSuggestionProvider())
                                .executes(DimensionCommand::openDimension)))
                .then(Commands.literal("list")
                        .executes(DimensionCommand::listLocked)));
    }

    private static SuggestionProvider<CommandSourceStack> dimensionSuggestionProvider() {
        return (context, builder) -> {
            MinecraftServer server = context.getSource().getServer();
            if (server != null) {
                DimensionLockManager.getAvailableDimensions(server).forEach(builder::suggest);
            }
            return builder.buildFuture();
        };
    }

    private static int closeDimension(CommandContext<CommandSourceStack> context) {
        return setDimensionState(context, true);
    }

    private static int openDimension(CommandContext<CommandSourceStack> context) {
        return setDimensionState(context, false);
    }

    private static int setDimensionState(CommandContext<CommandSourceStack> context, boolean lock) {
        try {
            String input = StringArgumentType.getString(context, "dimension").trim();
            MinecraftServer server = context.getSource().getServer();

            String resolved = DimensionLockManager.resolveAlias(input);
            if (resolved == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.invalid", input));
                return 0;
            }

            if (!DimensionLockManager.isValidDimension(input, server)) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.not_found", input));
                return 0;
            }

            if (lock) {
                if (DimensionLockManager.isLocked(input)) {
                    context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.already_closed", resolved));
                    return 0;
                }
                DimensionLockManager.lock(input);
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.dimension.closed", resolved), true);
                return 1;
            } else {
                if (!DimensionLockManager.isLocked(input)) {
                    context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.dimension.already_open", resolved));
                    return 0;
                }
                DimensionLockManager.unlock(input);
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.dimension.opened", resolved), true);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int listLocked(CommandContext<CommandSourceStack> context) {
        Set<String> locked = DimensionLockManager.getLockedDimensions();
        if (locked.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.dimension.list.empty"), false);
            return 1;
        }
        String list = locked.stream().sorted().collect(Collectors.joining("§8, §c"));
        context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.dimension.list", list), false);
        return 1;
    }
}
