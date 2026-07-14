package dev.rezzt.eazzyserverutils.managers;

import dev.rezzt.eazzyserverutils.Config;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportRequestManager {
    private static final Map<UUID, Request> PENDING_REQUESTS = new HashMap<>();

    public static void addRequest(ServerPlayer sender, ServerPlayer receiver, RequestType type) {
        long expiration = System.currentTimeMillis() + (long) Config.TPA_TIMEOUT.get() * 1000L;
        PENDING_REQUESTS.put(receiver.getUUID(), new Request(sender.getUUID(), receiver.getUUID(), type, expiration));
    }

    public static Request getRequest(UUID receiverUUID) {
        Request req = PENDING_REQUESTS.get(receiverUUID);
        if (req != null && req.isExpired()) {
            PENDING_REQUESTS.remove(receiverUUID);
            return null;
        }
        return req;
    }

    public static void removeRequest(UUID receiverUUID) {
        PENDING_REQUESTS.remove(receiverUUID);
    }

    public static class Request {
        public final UUID sender;
        public final UUID receiver;
        public final RequestType type;
        public final long expiration;

        public Request(UUID sender, UUID receiver, RequestType type, long expiration) {
            this.sender = sender;
            this.receiver = receiver;
            this.type = type;
            this.expiration = expiration;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > this.expiration;
        }
    }

    public enum RequestType {
        TPA,
        TPA_HERE
    }
}
