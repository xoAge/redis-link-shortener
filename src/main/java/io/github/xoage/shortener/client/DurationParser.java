package io.github.xoage.shortener.client;

import io.github.xoage.shortener.exception.InvalidTtlException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public final class DurationParser {

    private DurationParser() {
    }

    public static Duration parse(String text) {
        if (text == null || text.isBlank()) {
            throw new InvalidTtlException("TTL не указан");
        }
        String value = text.trim().toLowerCase();
        char last = value.charAt(value.length() - 1);

        ChronoUnit unit = ChronoUnit.SECONDS;
        String number = value;
        if (!Character.isDigit(last)) {
            unit = switch (last) {
                case 's' -> ChronoUnit.SECONDS;
                case 'm' -> ChronoUnit.MINUTES;
                case 'h' -> ChronoUnit.HOURS;
                case 'd' -> ChronoUnit.DAYS;
                default -> throw new InvalidTtlException(
                        "Неизвестная единица TTL '" + last + "', допустимо: s, m, h, d");
            };
            number = value.substring(0, value.length() - 1);
        }

        try {
            return Duration.of(Long.parseLong(number), unit);
        } catch (NumberFormatException | ArithmeticException e) {
            throw new InvalidTtlException("Некорректный TTL: " + text);
        }
    }
}

