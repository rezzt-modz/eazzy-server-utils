package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.rezzt.eazzyserverutils.managers.FreezeManager;
import dev.rezzt.eazzyserverutils.managers.VanishManager;
import dev.rezzt.eazzyserverutils.menus.InvSeeContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class StaffCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tps")
                .requires(source -> source.hasPermission(2))
                .executes(StaffCommands::tps));

        dispatcher.register(Commands.literal("freeze")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player()).executes(StaffCommands::freeze)));

        dispatcher.register(Commands.literal("unfreeze")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player()).executes(StaffCommands::unfreeze)));

        dispatcher.register(Commands.literal("iv")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player()).executes(StaffCommands::invsee)));

        dispatcher.register(Commands.literal("vanish")
                .requires(source -> source.hasPermission(2))
                .executes(StaffCommands::vanish));
    }

    private static int tps(CommandContext<CommandSourceStack> context) {
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
            String formatted = String.format("%.2f", tps);
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.tps", formatted), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int freeze(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            if (FreezeManager.isFrozen(target.getUUID())) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.freeze.already_frozen", target.getDisplayName()));
                return 0;
            }
            long expiration = 0L;
            FreezeManager.freeze(target.getUUID(), new FreezeManager.FreezeData(
                    target.level().dimension(),
                    target.position(),
                    target.getYRot(),
                    target.getXRot(),
                    expiration));
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.freeze.enabled", target.getDisplayName()), true);
            target.sendSystemMessage(Component.translatable("command.eazzy_server_utils.freeze.frozen_message"));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int unfreeze(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            if (!FreezeManager.isFrozen(target.getUUID())) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.freeze.not_frozen", target.getDisplayName()));
                return 0;
            }
            FreezeManager.unfreeze(target.getUUID());
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.freeze.disabled", target.getDisplayName()), true);
            target.sendSystemMessage(Component.translatable("command.eazzy_server_utils.freeze.disabled", target.getDisplayName()));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int invsee(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            if (player.getUUID().equals(target.getUUID())) {
                context.getSource().sendFailure(Component.literal("Cannot view own inventory."));
                return 0;
            }
            InvSeeContainer container = new InvSeeContainer(target.getInventory());
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x5, id, inv, container, 5), target.getDisplayName()));
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.invsee", target.getDisplayName()), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int vanish(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            boolean enabled = VanishManager.toggleVanish(player);
            if (enabled) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.vanish.enabled"), true);
            } else {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.vanish.disabled"), true);
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
