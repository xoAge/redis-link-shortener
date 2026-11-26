package io.github.xoage.shortener.link;

import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisClickCounter implements ClickCounter {

    private final JedisPool pool;

    public RedisClickCounter(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public long increment(String code) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.incr(RedisKeys.clicks(code));
        }
    }

    @Override
    public long count(String code) {
        try (Jedis jedis = pool.getResource()) {
            String value = jedis.get(RedisKeys.clicks(code));
            return value == null ? 0 : Long.parseLong(value);
        }
    }
}
