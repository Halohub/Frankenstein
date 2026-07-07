package com.halohub.frankenstein.common.util;

public final class PermCodeUtils {

    private PermCodeUtils() {
    }

    /**
     * Converts {@code admin:system:role} to {@code AdminSystemRole} for Vue route names.
     */
    public static String toRouteName(String permCode) {
        if (permCode == null || permCode.isBlank()) {
            return "Route";
        }
        String[] parts = permCode.split(":");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
