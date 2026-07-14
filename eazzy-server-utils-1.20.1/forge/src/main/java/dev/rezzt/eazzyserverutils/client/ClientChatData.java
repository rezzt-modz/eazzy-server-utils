package dev.rezzt.eazzyserverutils.client;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClientChatData {
    public static final Map<Integer, UUID> TIME_TO_UUID = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, UUID> eldest) {
            return size() > 100;
        }
    };
    public static final Set<Integer> RENDERED_ADDED_TIMES = new HashSet<>();
}
