package com.moko.support.pre.entity;

import java.util.UUID;

public enum OrderServices {
    SERVICE_CUSTOM(UUID.fromString("0000FFB0-0000-1000-8000-00805F9B34FB")),
    ;
    private UUID uuid;

    OrderServices(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
