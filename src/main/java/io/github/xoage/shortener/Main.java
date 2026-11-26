package io.github.xoage.shortener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Main {
    public static void main(String[] args) {
        try (JedisPool pool = new JedisPool("localhost", 6379);
             Jedis jedis = pool.getResource()) {
            System.out.println("Redis ответил: " + jedis.ping());
        }
    }
}