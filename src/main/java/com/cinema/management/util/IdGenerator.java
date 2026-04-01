package com.cinema.management.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IdGenerator {

    /**
     * Tự động sinh mã dựa trên timestamp: prefix-ddMMyyyyHHmmss
     * VD: NV-01042026092145
     */
    public static String generateId(String prefix, Class<?> entityClass, String idFieldName) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        return prefix + "-" + dtf.format(now);
    }
}
