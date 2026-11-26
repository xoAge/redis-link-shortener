package io.github.xoage.shortener.ratelimit;

public interface RateLimiter {

    /** Засчитывает попытку клиента и говорит, можно ли её пропустить. */
    RateLimitResult tryAcquire(String clientId);
}
