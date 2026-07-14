package dev.rezzt.eazzyserverutils.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ChatHeadRenderer {
    private static final int SKIN_SIZE = 64;
    private static final int FACE_U = 8;
    private static final int FACE_V = 8;
    private static final int FACE_SIZE = 8;

    public static void render(GuiGraphics guiGraphics, UUID playerUuid, int x, int y, int size) {
        ResourceLocation texture = getPlayerSkin(playerUuid);
        if (texture == null) {
            return;
        }
        guiGraphics.blit(texture, x, y, size, size, FACE_U, FACE_V, FACE_SIZE, FACE_SIZE, SKIN_SIZE, SKIN_SIZE);
    }

    private static ResourceLocation getPlayerSkin(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return null;
        }
        PlayerInfo info = mc.getConnection().getPlayerInfo(uuid);
        if (info != null) {
            return info.getSkinLocation();
        }
        return null;
    }
}
