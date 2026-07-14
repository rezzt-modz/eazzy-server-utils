package dev.rezzt.eazzyserverutils.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import dev.rezzt.eazzyserverutils.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, List<SavedLocation>> HOMES = new HashMap<>();
    private static Path storagePath;

    public static void load(MinecraftServer server) {
        storagePath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("eazzy_server_utils");
        File directory = storagePath.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = storagePath.resolve("homes.json").toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Map<String, List<SavedLocation>> loadedData = GSON.fromJson(reader, new TypeToken<Map<String, List<SavedLocation>>>() {}.getType());
                if (loadedData != null) {
                    HOMES.clear();
                    loadedData.forEach((key, value) -> HOMES.put(UUID.fromString(key), value));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load homes data", e);
            }
        }
    }

    public static void save() {
        if (storagePath == null) {
            return;
        }
        File file = storagePath.resolve("homes.json").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            Map<String, List<SavedLocation>> dataToSave = new HashMap<>();
            HOMES.forEach((key, value) -> dataToSave.put(key.toString(), value));
            GSON.toJson(dataToSave, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save homes data", e);
        }
    }

    public static boolean addHome(UUID playerUUID, SavedLocation home) {
        List<SavedLocation> playerHomes = HOMES.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        if (playerHomes.size() >= Config.MAX_HOMES.get()) {
            return false;
        }
        playerHomes.removeIf(h -> h.name.equalsIgnoreCase(home.name));
        playerHomes.add(home);
        save();
        return true;
    }

    public static SavedLocation getHome(UUID playerUUID, String name) {
        List<SavedLocation> playerHomes = HOMES.get(playerUUID);
        if (playerHomes == null) {
            return null;
        }
        return playerHomes.stream().filter(h -> h.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static List<SavedLocation> getHomes(UUID playerUUID) {
        return HOMES.getOrDefault(playerUUID, new ArrayList<>());
    }

    public static boolean deleteHome(UUID playerUUID, String name) {
        List<SavedLocation> playerHomes = HOMES.get(playerUUID);
        if (playerHomes == null) {
            return false;
        }
        boolean removed = playerHomes.removeIf(h -> h.name.equalsIgnoreCase(name));
        if (removed) {
            save();
        }
        return removed;
    }

    public static boolean renameHome(UUID playerUUID, String oldName, String newName) {
        List<SavedLocation> playerHomes = HOMES.get(playerUUID);
        if (playerHomes == null) {
            return false;
        }
        SavedLocation home = playerHomes.stream().filter(h -> h.name.equalsIgnoreCase(oldName)).findFirst().orElse(null);
        if (home == null) {
            return false;
        }
        if (playerHomes.stream().anyMatch(h -> !h.name.equalsIgnoreCase(oldName) && h.name.equalsIgnoreCase(newName))) {
            return false;
        }
        home.name = newName;
        save();
        return true;
    }

    public static Boolean togglePublicHome(UUID playerUUID, String name) {
        List<SavedLocation> playerHomes = HOMES.get(playerUUID);
        if (playerHomes == null) {
            return null;
        }
        SavedLocation home = playerHomes.stream().filter(h -> h.name.equalsIgnoreCase(name)).findFirst().orElse(null);
        if (home == null) {
            return null;
        }
        home.isPublic = !home.isPublic;
        save();
        return home.isPublic;
    }

    public static boolean isPublicHome(UUID playerUUID, String name) {
        SavedLocation home = getHome(playerUUID, name);
        return home != null && home.isPublic;
    }

    public static Map<UUID, List<SavedLocation>> getAllPublicHomes() {
        Map<UUID, List<SavedLocation>> publicHomes = new HashMap<>();
        HOMES.forEach((uuid, homes) -> {
            List<SavedLocation> publicList = homes.stream().filter(h -> h.isPublic).toList();
            if (!publicList.isEmpty()) {
                publicHomes.put(uuid, publicList);
            }
        });
        return publicHomes;
    }
}
