package io.github.xoage.shortener.link;

import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.time.Duration;
import java.util.Optional;

public class RedisLinkRepository implements LinkRepository {

    private final JedisPool pool;

    public RedisLinkRepository(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public void save(String code, String url, Duration ttl) {
        long seconds = ttl.toSeconds();
        try (Jedis jedis = pool.getResource(); Transaction tx = jedis.multi()) {
            tx.setex(RedisKeys.url(code), seconds, url);
            tx.setex(RedisKeys.clicks(code), seconds, "0");
            tx.exec();
        }
    }

    @Override
    public Optional<Duration> remainingTtl(String code) {
        try (Jedis jedis = pool.getResource()) {
            long seconds = jedis.ttl(RedisKeys.url(code));
            // -2: ключа нет, -1: ключ без TTL
            return seconds < 0 ? Optional.empty() : Optional.of(Duration.ofSeconds(seconds));
        }
    }

    @Override
    public Optional<String> findUrl(String code) {
        try (Jedis jedis = pool.getResource()) {
            return Optional.ofNullable(jedis.get(RedisKeys.url(code)));
        }
    }
}

