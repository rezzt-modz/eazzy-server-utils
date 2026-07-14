package dev.rezzt.eazzyserverutils;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FileConfig {
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("eazzy_server_utils");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");
    private static final Properties PROPERTIES = new Properties();

    public static void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            if (!Files.exists(CONFIG_FILE)) {
                createDefaultConfig();
            }
            try (InputStream stream = Files.newInputStream(CONFIG_FILE)) {
                PROPERTIES.load(stream);
            }
        } catch (IOException e) {
            EazzyServerUtils.LOGGER.error("Failed to load Eazzy Server Utils config, using defaults", e);
        }
    }

    private static void createDefaultConfig() throws IOException {
        Properties defaults = new Properties();
        defaults.setProperty("maxHomes", "8");
        defaults.setProperty("homeCooldown", "5");
        defaults.setProperty("maxWarps", "100");
        defaults.setProperty("warpCooldown", "5");
        defaults.setProperty("tpaCooldown", "3");
        defaults.setProperty("tpaTimeout", "12");
        defaults.setProperty("headCooldown", "3600");
        defaults.setProperty("spawnCooldown", "5");
        defaults.setProperty("backCooldown", "5");
        defaults.setProperty("nearRadius", "50");
        defaults.setProperty("homeWarpSharedCooldown", "true");
        defaults.setProperty("homeWarpCooldown", "5");
        defaults.setProperty("homeWarpBlockedDimensions", "");
        defaults.setProperty("combatCooldownSeconds", "10");

        try (OutputStream stream = Files.newOutputStream(CONFIG_FILE)) {
            defaults.store(stream,
                    "Eazzy Server Utils Configuration\n" +
                    "maxHomes: Maximum number of homes per player\n" +
                    "homeCooldown: Cooldown in seconds for home commands\n" +
                    "maxWarps: Maximum number of global warps\n" +
                    "warpCooldown: Cooldown in seconds for warp commands\n" +
                    "tpaCooldown: Cooldown in seconds for TPA requests\n" +
                    "tpaTimeout: Time in seconds before a TPA request expires\n" +
                    "headCooldown: Cooldown in seconds for the head command\n" +
                    "spawnCooldown: Cooldown in seconds for the spawn command\n" +
                    "backCooldown: Cooldown in seconds for the back command\n" +
                    "nearRadius: Radius in blocks for the near command\n" +
                    "homeWarpSharedCooldown: Share cooldown between /home and /warp\n" +
                    "homeWarpCooldown: Cooldown in seconds for shared /home and /warp\n" +
                    "homeWarpBlockedDimensions: Comma-separated list of dimensions where /home and /warp are blocked (e.g. minecraft:the_nether,minecraft:the_end)\n" +
                    "combatCooldownSeconds: Seconds after taking damage before /home and /warp can be used");
        }
    }

    public static int getInt(String key, int defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            EazzyServerUtils.LOGGER.warn("Invalid integer value for config key '{}': {}, using default {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public static String getString(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value.trim();
    }
}
