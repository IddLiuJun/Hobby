package com.example.dianciguanli.utils;

import java.util.UUID;

public class IDUtils {
    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateShortId() {
        String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, 8) + uuid.substring(24);
    }
}