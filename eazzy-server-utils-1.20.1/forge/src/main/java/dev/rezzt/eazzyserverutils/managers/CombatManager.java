package dev.rezzt.eazzyserverutils.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private static final Map<UUID, Long> LAST_DAMAGE = new HashMap<>();

    public static void recordDamage(UUID playerUUID) {
        LAST_DAMAGE.put(playerUUID, System.currentTimeMillis());
    }

    public static boolean isInCombat(UUID playerUUID, int combatCooldownSeconds) {
        Long lastDamage = LAST_DAMAGE.get(playerUUID);
        if (lastDamage == null) {
            return false;
        }
        if (System.currentTimeMillis() - lastDamage >= (long) combatCooldownSeconds * 1000L) {
            LAST_DAMAGE.remove(playerUUID);
            return false;
        }
        return true;
    }

    public static long getRemainingSeconds(UUID playerUUID, int combatCooldownSeconds) {
        Long lastDamage = LAST_DAMAGE.get(playerUUID);
        if (lastDamage == null) {
            return 0L;
        }
        long remaining = combatCooldownSeconds - (System.currentTimeMillis() - lastDamage) / 1000L;
        if (remaining <= 0) {
            LAST_DAMAGE.remove(playerUUID);
            return 0L;
        }
        return remaining;
    }

    public static void clear(UUID playerUUID) {
        LAST_DAMAGE.remove(playerUUID);
    }
}
