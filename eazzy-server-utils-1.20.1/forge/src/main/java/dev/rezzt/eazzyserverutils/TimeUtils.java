package dev.rezzt.eazzyserverutils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)([smhd])$", Pattern.CASE_INSENSITIVE);

    public static long parseDuration(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Duration cannot be empty");
        }
        Matcher matcher = DURATION_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format: " + input);
        }
        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();
        if (value <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }
        return switch (unit) {
            case "s" -> TimeUnit.SECONDS.toMillis(value);
            case "m" -> TimeUnit.MINUTES.toMillis(value);
            case "h" -> TimeUnit.HOURS.toMillis(value);
            case "d" -> TimeUnit.DAYS.toMillis(value);
            default -> throw new IllegalArgumentException("Invalid duration unit: " + unit);
        };
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
}
