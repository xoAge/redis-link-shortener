package io.github.xoage.shortener.redis;

public final class RedisKeys {

    public static final String ID_SEQUENCE = "id:seq";

    private RedisKeys() {
    }

    public static String url(String code) {
        return "url:" + code;
    }

    public static String clicks(String code) {
        return "clicks:" + code;
    }

    public static String fixedWindow(String clientId) {
        return "rate:fixed:" + clientId;
    }

    public static String slidingWindow(String clientId) {
        return "rate:sliding:" + clientId;
    }

}
