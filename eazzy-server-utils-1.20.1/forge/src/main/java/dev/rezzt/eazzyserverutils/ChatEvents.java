package dev.rezzt.eazzyserverutils;

import dev.rezzt.eazzyserverutils.managers.ChatLockManager;
import dev.rezzt.eazzyserverutils.network.ChatHeadPacket;
import dev.rezzt.eazzyserverutils.network.ESUPacketHandler;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class ChatEvents {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null || player.getServer() == null) {
            return;
        }
        if (ChatLockManager.isLocked() && !player.hasPermissions(2)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable("command.eazzy_server_utils.lockchat.deny"));
            return;
        }
        if (!ChatConfig.isEnabled()) {
            return;
        }
        MutableComponent formatted = formatChat(player, event.getMessage().getString());
        event.setCanceled(true);
        ESUPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ChatHeadPacket(formatted, player.getUUID()));
    }

    private static MutableComponent formatChat(ServerPlayer player, String rawMessage) {
        String format = ChatConfig.getFormat();
        Component nameComponent = buildNameComponent(player);
        Component messageComponent = Component.literal(rawMessage);

        MutableComponent result = Component.empty();
        int cursor = 0;
        while (cursor < format.length()) {
            int nameIndex = format.indexOf("%name%", cursor);
            int messageIndex = format.indexOf("%message%", cursor);

            if (nameIndex == -1 && messageIndex == -1) {
                if (cursor < format.length()) {
                    result.append(Component.literal(format.substring(cursor)));
                }
                break;
            }

            boolean isName = nameIndex != -1 && (messageIndex == -1 || nameIndex < messageIndex);
            int nextIndex = isName ? nameIndex : messageIndex;
            String literal = format.substring(cursor, nextIndex);
            if (!literal.isEmpty()) {
                result.append(Component.literal(literal));
            }
            result.append(isName ? nameComponent : messageComponent);
            cursor = nextIndex + (isName ? "%name%".length() : "%message%".length());
        }
        return result;
    }

    private static Component buildNameComponent(ServerPlayer player) {
        String name = player.getName().getString();
        String indicator = ChatConfig.showHeadIndicator() ? ChatConfig.getHeadIndicator() + " " : "";
        MutableComponent component = Component.literal(indicator + name);

        if (ChatConfig.showHeadHover()) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.getOrCreateTag().putString("SkullOwner", player.getGameProfile().getName());
            component.withStyle(style -> {
                HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(head));
                if (ChatConfig.clickNameToMessage()) {
                    return style.withHoverEvent(hover)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + name + " "));
                }
                return style.withHoverEvent(hover);
            });
        } else if (ChatConfig.clickNameToMessage()) {
            component.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + name + " ")));
        }
        return component;
    }
}
