package dev.rezzt.eazzyserverutils.managers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private static final Set<UUID> VANISHED = new HashSet<>();

    public static boolean isVanished(UUID uuid) {
        return VANISHED.contains(uuid);
    }

    public static boolean toggleVanish(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (VANISHED.contains(uuid)) {
            VANISHED.remove(uuid);
            player.removeEffect(MobEffects.INVISIBILITY);
            return false;
        }
        VANISHED.add(uuid);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        return true;
    }

    public static void setVanished(ServerPlayer player, boolean vanished) {
        UUID uuid = player.getUUID();
        if (vanished) {
            VANISHED.add(uuid);
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        } else {
            VANISHED.remove(uuid);
            player.removeEffect(MobEffects.INVISIBILITY);
        }
    }

    public static void remove(UUID uuid) {
        VANISHED.remove(uuid);
    }
}
