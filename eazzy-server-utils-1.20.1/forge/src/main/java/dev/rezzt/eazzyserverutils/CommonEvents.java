package dev.rezzt.eazzyserverutils;

import dev.rezzt.eazzyserverutils.managers.BackManager;
import dev.rezzt.eazzyserverutils.managers.CombatManager;
import dev.rezzt.eazzyserverutils.managers.FreezeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BackManager.setDeathLocation(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BackManager.clear(event.getEntity().getUUID());
        CombatManager.clear(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CombatManager.recordDamage(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player instanceof ServerPlayer serverPlayer && FreezeManager.isFrozen(serverPlayer.getUUID())) {
            FreezeManager.FreezeData data = FreezeManager.getFreezeData(serverPlayer.getUUID());
            if (data != null && !serverPlayer.level().dimension().equals(data.dimension)) {
                return;
            }
            if (data != null && serverPlayer.distanceToSqr(data.position) > 0.01) {
                serverPlayer.teleportTo(data.position.x, data.position.y, data.position.z);
                serverPlayer.setYRot(data.yRot);
                serverPlayer.setXRot(data.xRot);
                serverPlayer.setDeltaMovement(0.0, 0.0, 0.0);
                serverPlayer.hurtMarked = true;
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleInteraction(event);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        handleInteraction(event);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        handleInteraction(event);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        handleInteraction(event);
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleInteraction(event);
    }

    private static void handleInteraction(PlayerInteractEvent event) {
        Player player = event.getEntity();
        if (FreezeManager.isFrozen(player.getUUID())) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("command.eazzy_server_utils.freeze.frozen_message"), true);
        }
    }
}
