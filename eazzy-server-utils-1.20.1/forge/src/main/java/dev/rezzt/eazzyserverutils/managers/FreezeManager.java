package dev.rezzt.eazzyserverutils.managers;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeManager {
    private static final Map<UUID, FreezeData> FROZEN_PLAYERS = new HashMap<>();

    public static void freeze(UUID uuid, FreezeData data) {
        FROZEN_PLAYERS.put(uuid, data);
    }

    public static void unfreeze(UUID uuid) {
        FROZEN_PLAYERS.remove(uuid);
    }

    public static boolean isFrozen(UUID uuid) {
        FreezeData data = FROZEN_PLAYERS.get(uuid);
        if (data == null) {
            return false;
        }
        if (data.isExpired()) {
            unfreeze(uuid);
            return false;
        }
        return true;
    }

    public static FreezeData getFreezeData(UUID uuid) {
        return FROZEN_PLAYERS.get(uuid);
    }

    public static class FreezeData {
        public final ResourceKey<Level> dimension;
        public final Vec3 position;
        public final float yRot;
        public final float xRot;
        public final long expirationTime;

        public FreezeData(ResourceKey<Level> dimension, Vec3 position, float yRot, float xRot, long expirationTime) {
            this.dimension = dimension;
            this.position = position;
            this.yRot = yRot;
            this.xRot = xRot;
            this.expirationTime = expirationTime;
        }

        public boolean isExpired() {
            return this.expirationTime > 0L && System.currentTimeMillis() > this.expirationTime;
        }
    }
}
