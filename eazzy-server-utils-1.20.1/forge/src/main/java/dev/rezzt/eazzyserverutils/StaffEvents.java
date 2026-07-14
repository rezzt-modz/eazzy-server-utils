package dev.rezzt.eazzyserverutils;

import dev.rezzt.eazzyserverutils.managers.VanishManager;
import dev.rezzt.eazzyserverutils.storage.BanEntry;
import dev.rezzt.eazzyserverutils.storage.BanManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class StaffEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String ip = extractIp(player);
        if (ip != null && !ip.isEmpty()) {
            BanEntry ipBan = BanManager.getIPBan(ip);
            if (ipBan != null) {
                player.connection.disconnect(BanManager.getBanMessage(ipBan));
                return;
            }
        }

        BanEntry ban = BanManager.getBan(player.getUUID());
        if (ban != null) {
            player.connection.disconnect(BanManager.getBanMessage(ban));
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VanishManager.remove(player.getUUID());
        }
    }

    private static String extractIp(ServerPlayer player) {
        java.net.SocketAddress address = player.connection.connection.getRemoteAddress();
        if (address == null) {
            return null;
        }
        String raw = address.toString();
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }
        int portIndex = raw.indexOf(':');
        return portIndex >= 0 ? raw.substring(0, portIndex) : raw;
    }
}
