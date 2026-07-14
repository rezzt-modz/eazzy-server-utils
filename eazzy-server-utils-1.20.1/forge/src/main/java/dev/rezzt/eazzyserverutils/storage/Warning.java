package dev.rezzt.eazzyserverutils.storage;

import java.util.UUID;

public class Warning {
    public UUID targetUUID;
    public String targetName;
    public UUID issuerUUID;
    public String issuerName;
    public String reason;
    public long issuedAt;

    public Warning() {
    }

    public Warning(UUID targetUUID, String targetName, UUID issuerUUID, String issuerName, String reason, long issuedAt) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.issuerUUID = issuerUUID;
        this.issuerName = issuerName;
        this.reason = reason;
        this.issuedAt = issuedAt;
    }
}
