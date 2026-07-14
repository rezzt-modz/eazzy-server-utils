package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.rezzt.eazzyserverutils.managers.ChatLockManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class LockChatCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lockchat")
                .requires(source -> source.hasPermission(2))
                .executes(LockChatCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            boolean locked = ChatLockManager.toggle();
            MinecraftServer server = context.getSource().getServer();
            Component message = Component.translatable(locked
                    ? "command.eazzy_server_utils.lockchat.locked"
                    : "command.eazzy_server_utils.lockchat.unlocked",
                    context.getSource().getDisplayName());
            server.getPlayerList().broadcastSystemMessage(message, false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
