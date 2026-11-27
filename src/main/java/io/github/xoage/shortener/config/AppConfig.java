package io.github.xoage.shortener.config;

public record AppConfig(String redisHost,
                        int redisPort,
                        String dbUrl,
                        String dbUser,
                        String dbPassword) {

    public static AppConfig fromEnv() {
        return new AppConfig(
                env("REDIS_HOST", "localhost"),
                Integer.parseInt(env("REDIS_PORT", "6379")),
                env("DB_URL", "jdbc:postgresql://localhost:55432/shortener"),
                env("DB_USER", "shortener"),
                env("DB_PASSWORD", "shortener"));
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

