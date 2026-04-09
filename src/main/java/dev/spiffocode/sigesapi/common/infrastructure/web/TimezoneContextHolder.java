package dev.spiffocode.sigesapi.common.infrastructure.web;

import java.time.ZoneId;

public class TimezoneContextHolder {

    private static final ThreadLocal<ZoneId> CONTEXT = new ThreadLocal<>();
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Mexico_City");

    public static void setZoneId(ZoneId zoneId) {
        CONTEXT.set(zoneId);
    }

    public static ZoneId getZoneId() {
        ZoneId zoneId = CONTEXT.get();
        return zoneId != null ? zoneId : DEFAULT_ZONE;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
