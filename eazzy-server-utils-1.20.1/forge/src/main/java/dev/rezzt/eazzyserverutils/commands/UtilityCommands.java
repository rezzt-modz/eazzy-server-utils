package dev.rezzt.eazzyserverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.BiFunction;

public class UtilityCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crafting")
                .requires(source -> source.hasPermission(0))
                .executes(context -> openTable(context.getSource(), "crafting")));

        dispatcher.register(Commands.literal("anvil")
                .requires(source -> source.hasPermission(0))
                .executes(context -> openTable(context.getSource(), "anvil")));

        dispatcher.register(Commands.literal("smithing")
                .requires(source -> source.hasPermission(0))
                .executes(context -> openTable(context.getSource(), "smithing")));
    }

    private static int openTable(CommandSourceStack source, String type) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ContainerLevelAccess portableAccess = new ContainerLevelAccess() {
                @Override
                public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> function) {
                    return Optional.of((T) Boolean.TRUE);
                }
            };

            SimpleMenuProvider menuProvider;
            switch (type) {
                case "crafting": {
                    MutableComponent title = Component.translatable("container.crafting");
                    menuProvider = new SimpleMenuProvider((id, inv, p) -> new CraftingMenu(id, inv, portableAccess) {
                        @Override
                        public boolean stillValid(Player player) {
                            return true;
                        }
                    }, title);
                    break;
                }
                case "anvil": {
                    MutableComponent title = Component.translatable("container.repair");
                    menuProvider = new SimpleMenuProvider((id, inv, p) -> new AnvilMenu(id, inv, portableAccess) {
                        @Override
                        public boolean stillValid(Player player) {
                            return true;
                        }
                    }, title);
                    break;
                }
                case "smithing": {
                    MutableComponent title = Component.translatable("container.upgrade");
                    menuProvider = new SimpleMenuProvider((id, inv, p) -> new SmithingMenu(id, inv, portableAccess) {
                        @Override
                        public boolean stillValid(Player player) {
                            return true;
                        }
                    }, title);
                    break;
                }
                default:
                    return 0;
            }
            player.openMenu(menuProvider);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
