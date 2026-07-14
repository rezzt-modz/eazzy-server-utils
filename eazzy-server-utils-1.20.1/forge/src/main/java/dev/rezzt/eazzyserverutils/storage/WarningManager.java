package dev.rezzt.eazzyserverutils.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
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

public class WarningManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, List<Warning>> WARNINGS = new HashMap<>();
    private static Path storagePath;

    public static void load(MinecraftServer server) {
        storagePath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("eazzy_server_utils");
        File directory = storagePath.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = storagePath.resolve("warnings.json").toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Map<String, List<Warning>> loadedData = GSON.fromJson(reader, new TypeToken<Map<String, List<Warning>>>() {}.getType());
                if (loadedData != null) {
                    WARNINGS.clear();
                    loadedData.forEach((key, value) -> WARNINGS.put(UUID.fromString(key), value));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load warnings data", e);
            }
        }
    }

    public static void save() {
        if (storagePath == null) {
            return;
        }
        File file = storagePath.resolve("warnings.json").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            Map<String, List<Warning>> dataToSave = new HashMap<>();
            WARNINGS.forEach((key, value) -> dataToSave.put(key.toString(), value));
            GSON.toJson(dataToSave, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save warnings data", e);
        }
    }

    public static void warn(UUID targetUUID, String targetName, UUID issuerUUID, String issuerName, String reason) {
        List<Warning> list = WARNINGS.computeIfAbsent(targetUUID, k -> new ArrayList<>());
        list.add(new Warning(targetUUID, targetName, issuerUUID, issuerName, reason, System.currentTimeMillis()));
        save();
    }

    public static List<Warning> getWarnings(UUID targetUUID) {
        return WARNINGS.getOrDefault(targetUUID, new ArrayList<>());
    }

    public static int getWarningCount(UUID targetUUID) {
        return getWarnings(targetUUID).size();
    }

    public static boolean clearWarnings(UUID targetUUID) {
        List<Warning> removed = WARNINGS.remove(targetUUID);
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }
}
