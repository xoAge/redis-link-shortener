package io.github.xoage.shortener.code;

import io.github.xoage.shortener.redis.RedisKeys;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CodeGenerator {

    private static final long OFFSET = 916_132_832L;

    private final JedisPool pool;

    public CodeGenerator(JedisPool pool) {
        this.pool = pool;
    }

    public String next() {
        try (Jedis jedis = pool.getResource()) {
            long id = jedis.incr(RedisKeys.ID_SEQUENCE);
            return Base62.encode(OFFSET + id);
        }
    }
}
