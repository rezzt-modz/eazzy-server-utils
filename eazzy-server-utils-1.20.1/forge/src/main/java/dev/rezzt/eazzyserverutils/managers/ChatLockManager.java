package dev.rezzt.eazzyserverutils.managers;

public class ChatLockManager {
    private static boolean locked = false;

    public static boolean isLocked() {
        return locked;
    }

    public static boolean toggle() {
        locked = !locked;
        return locked;
    }

    public static void setLocked(boolean value) {
        locked = value;
    }
}
