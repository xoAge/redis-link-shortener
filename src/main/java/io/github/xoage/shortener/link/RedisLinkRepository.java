package io.github.xoage.shortener.link;

import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.Optional;

public class RedisLinkRepository implements LinkRepository {

    private final JedisPool pool;

    public RedisLinkRepository(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public void save(String code, String url, Duration ttl) {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(RedisKeys.url(code), ttl.toSeconds(), url);
        }
    }

    @Override
    public Optional<String> findUrl(String code) {
        try (Jedis jedis = pool.getResource()) {
            return Optional.ofNullable(jedis.get(RedisKeys.url(code)));
        }
    }
}

