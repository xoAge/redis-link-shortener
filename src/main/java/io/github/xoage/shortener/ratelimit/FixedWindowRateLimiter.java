package io.github.xoage.shortener.ratelimit;

import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.List;

public class FixedWindowRateLimiter implements RateLimiter {

    // KEYS[1] - ключ клиента, ARGV[1] - длина окна в миллисекундах.
    // Возвращает {текущее значение счётчика, оставшийся TTL в мс}.
    private static final String SCRIPT = """
            local count = redis.call('INCR', KEYS[1])
            if count == 1 or redis.call('PTTL', KEYS[1]) < 0 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return {count, redis.call('PTTL', KEYS[1])}
            """;

    private final JedisPool pool;
    private final long limit;
    private final Duration window;

    public FixedWindowRateLimiter(JedisPool pool, long limit, Duration window) {
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
                    List.of(RedisKeys.fixedWindow(clientId)),
                    List.of(String.valueOf(window.toMillis())));

            long count = (Long) reply.get(0);
            long ttlMillis = (Long) reply.get(1);

            boolean allowed = count <= limit;
            return new RateLimitResult(
                    allowed,
                    Math.max(0, limit - count),
                    allowed ? Duration.ZERO : Duration.ofMillis(Math.max(0, ttlMillis)));
        }
    }
}