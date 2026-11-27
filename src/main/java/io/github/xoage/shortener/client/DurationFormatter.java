package io.github.xoage.shortener.client;

import java.time.Duration;

public final class DurationFormatter {

    private DurationFormatter() {
    }

    public static String format(Duration duration) {
        long total = duration.toSeconds();
        long days = total / 86_400;
        long hours = (total % 86_400) / 3_600;
        long minutes = (total % 3_600) / 60;
        long seconds = total % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" д ");
        }
        if (hours > 0) {
            sb.append(hours).append(" ч ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" мин ");
        }
        if (seconds > 0 || sb.isEmpty()) {
            sb.append(seconds).append(" сек");
        }
        return sb.toString().trim();
    }
}