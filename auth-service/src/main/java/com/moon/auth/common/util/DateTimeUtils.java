package com.moon.auth.common.util;

import java.time.LocalDateTime;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDateTime plusSeconds(long seconds) {
        return now().plusSeconds(seconds);
    }

    public static LocalDateTime plusMinutes(long minutes) {
        return now().plusMinutes(minutes);
    }
}
