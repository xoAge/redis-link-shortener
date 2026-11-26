package io.github.xoage.shortener.ratelimit;

import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class SlidingWindowRateLimiter implements RateLimiter {

    // KEYS[1] - ключ клиента, ARGV[1] - окно в мс, ARGV[2] - лимит,
    // ARGV[3] - уникальный id запроса (в ZSET нельзя два одинаковых элемента).
    // Возвращает {1 или 0 (пропущен ли запрос), сколько осталось, retryAfter в мс}.
    private static final String SCRIPT = """
            local t = redis.call('TIME')
            local now = t[1] * 1000 + math.floor(t[2] / 1000)
            local window = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])

            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now - window)
            local count = redis.call('ZCARD', KEYS[1])

            if count < limit then
                redis.call('ZADD', KEYS[1], now, now .. ':' .. ARGV[3])
                redis.call('PEXPIRE', KEYS[1], window)
                return {1, limit - count - 1, 0}
            end

            local oldest = redis.call('ZRANGE', KEYS[1], 0, 0, 'WITHSCORES')
            return {0, 0, tonumber(oldest[2]) + window - now}
            """;

    private final JedisPool pool;
    private final long limit;
    private final Duration window;

    public SlidingWindowRateLimiter(JedisPool pool, long limit, Duration window) {
        if (limit < 1 || window.toMillis() < 1) {
            throw new IllegalArgumentException("Лимит и окно должны быть положительными");
        }
        this.pool = pool;
        this.limit = limit;
        this.window = window;
    }

    @Override
    public RateLimitResult tryAcquire(String clientId) {
        try (Jedis jedis = pool.getResource()) {
            List<?> reply = (List<?>) jedis.eval(
                    SCRIPT,
                    List.of(RedisKeys.slidingWindow(clientId)),
                    List.of(String.valueOf(window.toMillis()),
                            String.valueOf(limit),
                            UUID.randomUUID().toString()));

            boolean allowed = (Long) reply.get(0) == 1;
            long remaining = (Long) reply.get(1);
            long retryAfterMillis = (Long) reply.get(2);

            return new RateLimitResult(allowed, remaining, Duration.ofMillis(retryAfterMillis));
        }
    }
}

