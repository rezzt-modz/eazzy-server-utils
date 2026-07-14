package dev.rezzt.eazzyserverutils.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BanManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, BanEntry> BANS = new HashMap<>();
    private static final Map<String, BanEntry> IP_BANS = new HashMap<>();
    private static Path storagePath;

    private static class BanData {
        Map<String, BanEntry> bans = new HashMap<>();
        Map<String, BanEntry> ipBans = new HashMap<>();
    }

    public static void load(MinecraftServer server) {
        storagePath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("eazzy_server_utils");
        File directory = storagePath.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = storagePath.resolve("bans.json").toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                BanData loadedData = GSON.fromJson(reader, BanData.class);
                if (loadedData != null) {
                    BANS.clear();
                    IP_BANS.clear();
                    if (loadedData.bans != null) {
                        loadedData.bans.forEach((key, value) -> {
                            if (!value.isExpired()) {
                                BANS.put(UUID.fromString(key), value);
                            }
                        });
                    }
                    if (loadedData.ipBans != null) {
                        loadedData.ipBans.forEach((key, value) -> {
                            if (!value.isExpired()) {
                                IP_BANS.put(key, value);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load bans data", e);
            }
        }
    }

    public static void save() {
        if (storagePath == null) {
            return;
        }
        File file = storagePath.resolve("bans.json").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            BanData data = new BanData();
            BANS.forEach((key, value) -> data.bans.put(key.toString(), value));
            IP_BANS.forEach((key, value) -> data.ipBans.put(key, value));
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save bans data", e);
        }
    }

    public static void ban(UUID uuid, BanEntry entry) {
        BANS.put(uuid, entry);
        save();
    }

    public static boolean unban(UUID uuid) {
        BanEntry removed = BANS.remove(uuid);
        if (removed != null) {
            if (removed.ip != null && !removed.ip.isEmpty()) {
                IP_BANS.remove(removed.ip);
            }
            save();
            return true;
        }
        return false;
    }

    public static boolean unbanByName(String name, MinecraftServer server) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) {
            return unban(online.getUUID());
        }
        Optional<GameProfile> profile = server.getProfileCache().get(name);
        return profile.map(gameProfile -> unban(gameProfile.getId())).orElse(false);
    }

    public static BanEntry getBan(UUID uuid) {
        BanEntry entry = BANS.get(uuid);
        if (entry != null && entry.isExpired()) {
            BANS.remove(uuid);
            save();
            return null;
        }
        return entry;
    }

    public static boolean isBanned(UUID uuid) {
        return getBan(uuid) != null;
    }

    public static void ipBan(String ip, BanEntry entry) {
        IP_BANS.put(ip, entry);
        if (entry.targetUUID != null) {
            BANS.put(entry.targetUUID, entry);
        }
        save();
    }

    public static boolean unbanIP(String ip) {
        BanEntry removed = IP_BANS.remove(ip);
        if (removed != null) {
            if (removed.targetUUID != null) {
                BANS.remove(removed.targetUUID);
            }
            save();
            return true;
        }
        return false;
    }

    public static BanEntry getIPBan(String ip) {
        BanEntry entry = IP_BANS.get(ip);
        if (entry != null && entry.isExpired()) {
            IP_BANS.remove(ip);
            save();
            return null;
        }
        return entry;
    }

    public static boolean isIPBanned(String ip) {
        return getIPBan(ip) != null;
    }

    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "Permanent";
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long days = TimeUnit.MILLISECONDS.toDays(millis);

        if (seconds < 60) {
            return seconds + "s";
        }
        if (minutes < 60) {
            return minutes + "m";
        }
        if (hours < 24) {
            return hours + "h";
        }
        return days + "d";
    }

    public static String formatExpiry(BanEntry entry) {
        if (entry.isPermanent()) {
            return "Permanent";
        }
        long remaining = entry.expiresAt - System.currentTimeMillis();
        if (remaining <= 0) {
            return "Expired";
        }
        return formatDuration(remaining);
    }

    public static Component getBanMessage(BanEntry entry) {
        String reason = entry.reason != null ? entry.reason : "No reason provided";
        return switch (entry.type) {
            case BanEntry.TYPE_TEMPBAN ->
                    Component.translatable("command.eazzy_server_utils.tempban.screen", reason, formatExpiry(entry));
            case BanEntry.TYPE_IPBAN ->
                    Component.translatable("command.eazzy_server_utils.ipban.screen", reason);
            default ->
                    Component.translatable("command.eazzy_server_utils.ban.screen", reason);
        };
    }
}
