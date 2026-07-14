package dev.rezzt.eazzyserverutils.client;

import dev.rezzt.eazzyserverutils.ChatConfig;
import dev.rezzt.eazzyserverutils.mixins.ChatComponentAccessor;
import dev.rezzt.eazzyserverutils.network.ChatHeadPacket;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;

import java.util.List;

public class ClientPacketHandler {
    public static void handle(ChatHeadPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null) {
            return;
        }
        ChatComponent chat = mc.gui.getChat();
        if (chat == null) {
            return;
        }

        chat.addMessage(packet.getMessage());

        if (ChatConfig.showHeadInline()) {
            List<GuiMessage> allMessages = ((ChatComponentAccessor) chat).getAllMessages();
            if (!allMessages.isEmpty()) {
                GuiMessage newest = allMessages.get(0);
                ClientChatData.TIME_TO_UUID.put(newest.addedTime(), packet.getSender());
            }
        }
    }
}
