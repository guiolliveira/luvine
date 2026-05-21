package com.javacore.spring_api_luvine.common.util;

public final class RequestInfo {

    private RequestInfo() {}

    public static String normalizeIp(String ipAddress) {
        if (ipAddress != null && ipAddress.contains(",")) {
            return ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    public static String truncateDeviceInfo(String deviceInfo) {
        if (deviceInfo == null) return null;
        return deviceInfo.substring(0, Math.min(deviceInfo.length(), 255));
    }
}