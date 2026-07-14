package dev.rezzt.eazzyserverutils.managers;

import dev.rezzt.eazzyserverutils.storage.SavedLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackManager {
    private static final Map<UUID, SavedLocation> LAST_LOCATIONS = new HashMap<>();
    private static final Map<UUID, SavedLocation> DEATH_LOCATIONS = new HashMap<>();

    public static void setLastLocation(ServerPlayer player) {
        LAST_LOCATIONS.put(player.getUUID(), SavedLocation.fromPlayer("last", player));
    }

    public static void setDeathLocation(ServerPlayer player) {
        DEATH_LOCATIONS.put(player.getUUID(), SavedLocation.fromPlayer("death", player));
    }

    public static SavedLocation getBackLocation(ServerPlayer player) {
        UUID uuid = player.getUUID();
        SavedLocation death = DEATH_LOCATIONS.remove(uuid);
        if (death != null) {
            return death;
        }
        return LAST_LOCATIONS.get(uuid);
    }

    public static void clear(UUID uuid) {
        LAST_LOCATIONS.remove(uuid);
        DEATH_LOCATIONS.remove(uuid);
    }
}
