package io.github.xoage.shortener.ratelimit;

import java.time.Duration;

/**
 * @param allowed    пропущен ли запрос
 * @param remaining  сколько запросов ещё можно сделать в текущем окне
 * @param retryAfter через сколько можно повторить (ZERO, если запрос пропущен)
 */

public record RateLimitResult(boolean allowed, long remaining, Duration retryAfter) {
}
