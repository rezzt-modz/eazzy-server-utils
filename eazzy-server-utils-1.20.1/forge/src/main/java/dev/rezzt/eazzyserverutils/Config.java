package dev.rezzt.eazzyserverutils;

import java.util.function.Supplier;

public class Config {
    public static final Supplier<Integer> MAX_HOMES = () -> FileConfig.getInt("maxHomes", 8);
    public static final Supplier<Integer> HOME_COOLDOWN = () -> FileConfig.getInt("homeCooldown", 5);
    public static final Supplier<Integer> MAX_WARPS = () -> FileConfig.getInt("maxWarps", 100);
    public static final Supplier<Integer> WARP_COOLDOWN = () -> FileConfig.getInt("warpCooldown", 5);
    public static final Supplier<Integer> TPA_COOLDOWN = () -> FileConfig.getInt("tpaCooldown", 3);
    public static final Supplier<Integer> TPA_TIMEOUT = () -> FileConfig.getInt("tpaTimeout", 12);
    public static final Supplier<Integer> HEAD_COOLDOWN = () -> FileConfig.getInt("headCooldown", 3600);
    public static final Supplier<Integer> SPAWN_COOLDOWN = () -> FileConfig.getInt("spawnCooldown", 5);
    public static final Supplier<Integer> BACK_COOLDOWN = () -> FileConfig.getInt("backCooldown", 5);
    public static final Supplier<Integer> NEAR_RADIUS = () -> FileConfig.getInt("nearRadius", 50);
    public static final Supplier<Boolean> HOME_WARP_SHARED_COOLDOWN = () -> FileConfig.getBoolean("homeWarpSharedCooldown", true);
    public static final Supplier<Integer> HOME_WARP_COOLDOWN = () -> FileConfig.getInt("homeWarpCooldown", 5);
    public static final Supplier<String> HOME_WARP_BLOCKED_DIMENSIONS = () -> FileConfig.getString("homeWarpBlockedDimensions", "");
    public static final Supplier<Integer> COMBAT_COOLDOWN_SECONDS = () -> FileConfig.getInt("combatCooldownSeconds", 10);
}
