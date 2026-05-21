package com.javacore.spring_api_luvine.common.util;

public final class EmailMask {

    private EmailMask() {}

    public static String mask(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "null";
        }

        int indexAt = email.indexOf("@");

        if (indexAt <= 0) {
            return "***";
        }

        String local = email.substring(0, indexAt);
        String domain = email.substring(indexAt);

        int visibleCount = Math.max(1, local.length() / 3);
        String visible = local.substring(0, visibleCount);
        String masked = "*".repeat(local.length() - visibleCount);

        return visible + masked + domain;
    }
}