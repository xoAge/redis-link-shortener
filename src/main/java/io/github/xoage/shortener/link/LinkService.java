package io.github.xoage.shortener.link;

import io.github.xoage.shortener.code.CodeGenerator;
import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.Optional;

public class LinkService {

    public static final Duration DEFAULT_TTL = Duration.ofHours(24);
    public static final Duration MAX_TTL = Duration.ofDays(365);

    private final JedisPool pool;
    private final CodeGenerator codeGenerator;

    public LinkService(JedisPool pool, CodeGenerator codeGenerator) {
        this.pool = pool;
        this.codeGenerator = codeGenerator;
    }

    public String shorten(String url) {
        return shorten(url, DEFAULT_TTL);
    }

    public String shorten(String url, Duration ttl) {
        UrlValidator.validate(url);
        validateTtl(ttl);

        String code = codeGenerator.next();
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(RedisKeys.url(code), ttl.toSeconds(), url);
        }
        return code;
    }

    public Optional<String> resolve(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        try (Jedis jedis = pool.getResource()) {
            return Optional.ofNullable(jedis.get(RedisKeys.url(code)));
        }
    }

    private void validateTtl(Duration ttl) {
        if (ttl == null || ttl.toSeconds() < 1) {
            throw new IllegalArgumentException("TTL должен быть не меньше 1 секунды");
        }
        if (ttl.compareTo(MAX_TTL) > 0) {
            throw new IllegalArgumentException("TTL не может быть больше " + MAX_TTL.toDays() + " дней");
        }
    }
}
