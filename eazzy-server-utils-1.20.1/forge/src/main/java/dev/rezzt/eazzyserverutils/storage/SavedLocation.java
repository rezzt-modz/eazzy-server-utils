package dev.rezzt.eazzyserverutils.storage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SavedLocation {
    public String name;
    public String dimension;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public boolean isPublic = false;

    public SavedLocation(String name, String dimension, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static SavedLocation fromPlayer(String name, ServerPlayer player) {
        return new SavedLocation(
                name,
                player.level().dimension().location().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );
    }

    public ResourceKey<Level> getDimensionKey() {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(this.dimension));
    }
}
