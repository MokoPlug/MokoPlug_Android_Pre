package com.moko.support.pre.entity;

import java.io.Serializable;
import java.util.UUID;

public enum OrderCHAR implements Serializable {
    // FFB0
    CHAR_PARAMS_READ(UUID.fromString("0000FFB0-0000-1000-8000-00805F9B34FB")),
    CHAR_PARAMS_WRITE(UUID.fromString("0000FFB1-0000-1000-8000-00805F9B34FB")),
    CHAR_PARAMS_NOTIFY(UUID.fromString("0000FFB2-0000-1000-8000-00805F9B34FB")),
    ;

    private UUID uuid;

    OrderCHAR(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
