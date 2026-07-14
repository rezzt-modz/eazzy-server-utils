package dev.rezzt.eazzyserverutils;

import dev.rezzt.eazzyserverutils.managers.DimensionLockManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionEvents {
    private static final long MESSAGE_COOLDOWN_MS = 3000;
    private static final Map<UUID, Long> LAST_MESSAGE_TIME = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        // Dimension locks apply to everyone, including operators. If staff need to enter,
        // they can temporarily open the dimension with /eazzyserverutils open.
        if (!DimensionLockManager.isLocked(event.getDimension().location())) {
            return;
        }
        event.setCanceled(true);

        long now = System.currentTimeMillis();
        Long last = LAST_MESSAGE_TIME.get(player.getUUID());
        if (last == null || now - last > MESSAGE_COOLDOWN_MS) {
            LAST_MESSAGE_TIME.put(player.getUUID(), now);
            String dimensionName = event.getDimension().location().toString();
            player.displayClientMessage(Component.translatable("command.eazzy_server_utils.dimension.denied", dimensionName), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_MESSAGE_TIME.remove(event.getEntity().getUUID());
    }
}
