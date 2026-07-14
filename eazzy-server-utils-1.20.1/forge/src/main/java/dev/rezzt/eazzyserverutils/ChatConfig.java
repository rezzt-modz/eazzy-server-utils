package dev.rezzt.eazzyserverutils;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ChatConfig {
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("eazzy_server_utils");
    private static final Path CHAT_CONFIG_FILE = CONFIG_DIR.resolve("chat.properties");
    private static final Properties PROPERTIES = new Properties();

    public static void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            if (!Files.exists(CHAT_CONFIG_FILE)) {
                createDefaultConfig();
            }
            try (Reader reader = new InputStreamReader(Files.newInputStream(CHAT_CONFIG_FILE), StandardCharsets.UTF_8)) {
                PROPERTIES.load(reader);
            }
        } catch (IOException e) {
            EazzyServerUtils.LOGGER.error("Failed to load Eazzy Server Utils chat config, using defaults", e);
        }
    }

    private static void createDefaultConfig() throws IOException {
        Properties defaults = new Properties();
        defaults.setProperty("enabled", "true");
        defaults.setProperty("format", "§b%name%§7: §f%message%");
        defaults.setProperty("showHeadHover", "true");
        defaults.setProperty("showHeadIndicator", "false");
        defaults.setProperty("headIndicator", "☺");
        defaults.setProperty("clickNameToMessage", "true");
        defaults.setProperty("showHeadInline", "true");

        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(CHAT_CONFIG_FILE), StandardCharsets.UTF_8)) {
            defaults.store(writer,
                    "Eazzy Server Utils - Chat Configuration\n" +
                    "Placeholders: %name% = player name, %message% = chat message\n" +
                    "\n" +
                    "Color codes:\n" +
                    "  §0 Black     §1 Dark Blue   §2 Dark Green  §3 Dark Aqua\n" +
                    "  §4 Dark Red  §5 Dark Purple §6 Gold        §7 Gray\n" +
                    "  §8 Dark Gray §9 Blue        §a Green       §b Aqua\n" +
                    "  §c Red       §d Light Purple§e Yellow      §f White\n" +
                    "\n" +
                    "Formatting codes:\n" +
                    "  §k Obfuscated  §l Bold        §m Strikethrough\n" +
                    "  §n Underline    §o Italic      §r Reset\n" +
                    "\n" +
                    "Options:\n" +
                    "  enabled: Enable custom chat formatting\n" +
                    "  format: Chat format string\n" +
                    "  showHeadHover: Show player head on name hover\n" +
                    "  showHeadIndicator: Show indicator before name (not actual skin head)\n" +
                    "  headIndicator: Character to use as indicator\n" +
                    "  clickNameToMessage: Click name to suggest /msg <player>\n" +
                    "  showHeadInline: Show player head to the left of the chat message (requires mod on client)\n" +
                    "\n" +
                    "NOTE: Inline heads need this mod installed on the client");
        }
    }

    public static boolean isEnabled() {
        return getBool("enabled", true);
    }

    public static String getFormat() {
        return getString("format", "§b%name%§7: §f%message%");
    }

    public static boolean showHeadHover() {
        return getBool("showHeadHover", true);
    }

    public static boolean showHeadIndicator() {
        return getBool("showHeadIndicator", false);
    }

    public static String getHeadIndicator() {
        return getString("headIndicator", "☺");
    }

    public static boolean clickNameToMessage() {
        return getBool("clickNameToMessage", true);
    }

    public static boolean showHeadInline() {
        return getBool("showHeadInline", true);
    }

    private static boolean getBool(String key, boolean defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static String getString(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        return value != null ? value : defaultValue;
    }
}
