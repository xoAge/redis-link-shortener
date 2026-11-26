package io.github.xoage.shortener.exception;

import java.time.Duration;

public class RateLimitExceededException extends ShortenerException {

    private final Duration retryAfter;

    public RateLimitExceededException(Duration retryAfter) {
        super("Слишком много запросов. Повторите через " + (retryAfter.toSeconds() + 1) + " сек.");
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
