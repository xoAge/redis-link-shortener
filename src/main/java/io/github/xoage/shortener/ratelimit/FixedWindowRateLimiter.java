package io.github.xoage.shortener.ratelimit;

import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;

public class FixedWindowRateLimiter implements RateLimiter {

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
        String key = RedisKeys.fixedWindow(clientId);
        try (Jedis jedis = pool.getResource()) {
            long count = jedis.incr(key);
            if (count == 1) {
                jedis.pexpire(key, window.toMillis());
            }
            long ttlMillis = jedis.pttl(key);

            boolean allowed = count <= limit;
            return new RateLimitResult(
                    allowed,
                    Math.max(0, limit - count),
                    allowed ? Duration.ZERO : Duration.ofMillis(Math.max(0, ttlMillis)));
        }
    }
}
