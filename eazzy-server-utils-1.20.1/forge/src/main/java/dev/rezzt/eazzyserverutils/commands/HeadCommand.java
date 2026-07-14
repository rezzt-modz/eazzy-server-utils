package dev.rezzt.eazzyserverutils.commands;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.rezzt.eazzyserverutils.Config;
import dev.rezzt.eazzyserverutils.managers.CooldownManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HeadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("head")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("player", StringArgumentType.string()).executes(HeadCommand::giveHead)));
    }

    private static int giveHead(final CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        try {
            final ServerPlayer player = context.getSource().getPlayerOrException();
            if (!player.hasPermissions(2) && CooldownManager.isOnCooldown(player.getUUID(), "eazzyserverutils.command.head")) {
                long remaining = CooldownManager.getRemainingSeconds(player.getUUID(), "eazzyserverutils.command.head") / 60L;
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.head.cooldown", remaining));
                return 0;
            }
            context.getSource().sendSuccess(() -> Component.literal("Fetching head for " + playerName + "..."), true);
            final MinecraftServer server = context.getSource().getServer();
            server.getProfileRepository().findProfilesByNames(new String[]{playerName}, Agent.MINECRAFT, new ProfileLookupCallback() {
                @Override
                public void onProfileLookupSucceeded(GameProfile profile) {
                    try {
                        GameProfile populatedProfile = server.getSessionService().fillProfileProperties(profile, true);
                        if (populatedProfile == null) {
                            populatedProfile = profile;
                        }
                        final GameProfile finalProfile = populatedProfile;
                        server.execute(() -> {
                            try {
                                ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                                CompoundTag skullOwner = NbtUtils.writeGameProfile(new CompoundTag(), finalProfile);
                                head.addTagElement("SkullOwner", skullOwner);
                                if (!player.getInventory().add(head)) {
                                    player.drop(head, false);
                                }
                                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.head.received", finalProfile.getName()), false);
                                if (!player.hasPermissions(2)) {
                                    CooldownManager.setCooldown(player.getUUID(), "eazzyserverutils.command.head", Config.HEAD_COOLDOWN.get());
                                }
                            } catch (Exception e) {
                                context.getSource().sendFailure(Component.literal("Error creating head: " + e.getMessage()));
                            }
                        });
                    } catch (Exception e) {
                        server.execute(() -> context.getSource().sendFailure(Component.literal("Error fetching skin: " + e.getMessage())));
                    }
                }

                @Override
                public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                    server.execute(() -> context.getSource().sendFailure(Component.literal("Error fetching profile for " + profile.getName() + ": " + exception.getMessage())));
                }
            });
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
