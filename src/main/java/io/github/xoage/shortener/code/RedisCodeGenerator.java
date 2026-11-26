package io.github.xoage.shortener.code;

import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisCodeGenerator implements CodeGenerator {

    private static final long OFFSET = 916_132_832L;

    private final JedisPool pool;

    public RedisCodeGenerator(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public String next() {
        try (Jedis jedis = pool.getResource()) {
            long id = jedis.incr(RedisKeys.ID_SEQUENCE);
            return Base62.encode(OFFSET + id);
        }
    }
}
