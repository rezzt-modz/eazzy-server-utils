package dev.rezzt.eazzyserverutils.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private static final Map<String, Map<UUID, Long>> COOLDOWNS = new HashMap<>();

    public static boolean isOnCooldown(UUID playerUUID, String type) {
        Map<UUID, Long> typeCooldowns = COOLDOWNS.get(type);
        if (typeCooldowns == null) {
            return false;
        }
        Long expiration = typeCooldowns.get(playerUUID);
        if (expiration == null) {
            return false;
        }
        if (System.currentTimeMillis() >= expiration) {
            typeCooldowns.remove(playerUUID);
            return false;
        }
        return true;
    }

    public static void setCooldown(UUID playerUUID, String type, int seconds) {
        long expiration = System.currentTimeMillis() + (long) seconds * 1000L;
        COOLDOWNS.computeIfAbsent(type, k -> new HashMap<>()).put(playerUUID, expiration);
    }

    public static long getRemainingSeconds(UUID playerUUID, String type) {
        if (!isOnCooldown(playerUUID, type)) {
            return 0L;
        }
        long expiration = COOLDOWNS.get(type).get(playerUUID);
        return (expiration - System.currentTimeMillis()) / 1000L;
    }
}
