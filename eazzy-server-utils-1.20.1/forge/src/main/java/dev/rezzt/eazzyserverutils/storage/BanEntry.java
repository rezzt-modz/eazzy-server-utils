package dev.rezzt.eazzyserverutils.storage;

import java.util.UUID;

public class BanEntry {
    public static final String TYPE_BAN = "BAN";
    public static final String TYPE_TEMPBAN = "TEMPBAN";
    public static final String TYPE_IPBAN = "IPBAN";

    public UUID targetUUID;
    public String targetName;
    public UUID issuerUUID;
    public String issuerName;
    public String reason;
    public long issuedAt;
    public long expiresAt;
    public String ip;
    public String type;

    public BanEntry() {
    }

    public BanEntry(UUID targetUUID, String targetName, UUID issuerUUID, String issuerName, String reason, long issuedAt, long expiresAt, String ip, String type) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.issuerUUID = issuerUUID;
        this.issuerName = issuerName;
        this.reason = reason;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.ip = ip;
        this.type = type;
    }

    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }

    public boolean isPermanent() {
        return expiresAt <= 0;
    }
}
