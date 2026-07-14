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
import java.util.List;

public class WarpManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<SavedLocation> WARPS = new ArrayList<>();
    private static Path storagePath;

    public static void load(MinecraftServer server) {
        storagePath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("eazzy_server_utils");
        File directory = storagePath.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = storagePath.resolve("warps.json").toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                List<SavedLocation> loadedData = GSON.fromJson(reader, new TypeToken<List<SavedLocation>>() {}.getType());
                if (loadedData != null) {
                    WARPS.clear();
                    WARPS.addAll(loadedData);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load warps data", e);
            }
        }
    }

    public static void save() {
        if (storagePath == null) {
            return;
        }
        File file = storagePath.resolve("warps.json").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(WARPS, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save warps data", e);
        }
    }

    public static boolean setWarp(SavedLocation warp) {
        boolean exists = WARPS.stream().anyMatch(w -> w.name.equalsIgnoreCase(warp.name));
        if (!exists && WARPS.size() >= Config.MAX_WARPS.get()) {
            return false;
        }
        WARPS.removeIf(w -> w.name.equalsIgnoreCase(warp.name));
        WARPS.add(warp);
        save();
        return true;
    }

    public static SavedLocation getWarp(String name) {
        return WARPS.stream().filter(w -> w.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static List<SavedLocation> getWarps() {
        return new ArrayList<>(WARPS);
    }

    public static boolean deleteWarp(String name) {
        boolean removed = WARPS.removeIf(w -> w.name.equalsIgnoreCase(name));
        if (removed) {
            save();
        }
        return removed;
    }
}
