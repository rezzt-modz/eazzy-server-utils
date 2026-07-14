package dev.rezzt.eazzyserverutils.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DimensionLockManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> LOCKED_DIMENSIONS = new HashSet<>();
    private static final Map<String, String> ALIASES = new LinkedHashMap<>();

    static {
        ALIASES.put("nether", Level.NETHER.location().toString());
        ALIASES.put("the_nether", Level.NETHER.location().toString());
        ALIASES.put("end", Level.END.location().toString());
        ALIASES.put("the_end", Level.END.location().toString());
        ALIASES.put("overworld", Level.OVERWORLD.location().toString());
    }

    private static Path storagePath;

    public static void load(MinecraftServer server) {
        storagePath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("eazzy_server_utils");
        File directory = storagePath.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = storagePath.resolve("locked_dimensions.json").toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Set<String> loadedData = GSON.fromJson(reader, new TypeToken<Set<String>>() {}.getType());
                if (loadedData != null) {
                    LOCKED_DIMENSIONS.clear();
                    LOCKED_DIMENSIONS.addAll(loadedData);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load locked dimensions data", e);
            }
        }
    }

    public static void save() {
        if (storagePath == null) {
            return;
        }
        File file = storagePath.resolve("locked_dimensions.json").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(LOCKED_DIMENSIONS, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save locked dimensions data", e);
        }
    }

    public static String resolveAlias(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (ALIASES.containsKey(normalized)) {
            return ALIASES.get(normalized);
        }
        ResourceLocation location = ResourceLocation.tryParse(input.trim());
        return location != null ? location.toString() : null;
    }

    public static boolean isValidDimension(String dimension, MinecraftServer server) {
        String resolved = resolveAlias(dimension);
        if (resolved == null) {
            return false;
        }
        return server.levelKeys().stream().anyMatch(key -> key.location().toString().equalsIgnoreCase(resolved));
    }

    public static boolean lock(String dimension) {
        String resolved = resolveAlias(dimension);
        if (resolved == null) {
            return false;
        }
        boolean added = LOCKED_DIMENSIONS.add(resolved.toLowerCase(Locale.ROOT));
        if (added) {
            save();
        }
        return added;
    }

    public static boolean unlock(String dimension) {
        String resolved = resolveAlias(dimension);
        if (resolved == null) {
            return false;
        }
        boolean removed = LOCKED_DIMENSIONS.remove(resolved.toLowerCase(Locale.ROOT));
        if (removed) {
            save();
        }
        return removed;
    }

    public static boolean isLocked(String dimension) {
        String resolved = resolveAlias(dimension);
        if (resolved == null) {
            return false;
        }
        return LOCKED_DIMENSIONS.contains(resolved.toLowerCase(Locale.ROOT));
    }

    public static boolean isLocked(ResourceLocation dimension) {
        return LOCKED_DIMENSIONS.contains(dimension.toString().toLowerCase(Locale.ROOT));
    }

    public static Set<String> getLockedDimensions() {
        return new HashSet<>(LOCKED_DIMENSIONS);
    }

    public static Set<String> getAvailableDimensions(MinecraftServer server) {
        Set<String> dimensions = new LinkedHashSet<>();
        server.levelKeys().forEach(key -> dimensions.add(key.location().toString()));
        dimensions.addAll(ALIASES.keySet());
        return dimensions;
    }
}
