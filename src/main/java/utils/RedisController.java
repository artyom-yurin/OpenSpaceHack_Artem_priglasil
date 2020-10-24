package utils;


import com.google.gson.Gson;
import models.Context;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class RedisController {
    static final String DB_URL = "localhost";
    static final int DB_PORT = 6379;

    private final JedisPoolConfig poolConfig = buildPoolConfig();
    private JedisPool jedisPool = new JedisPool(poolConfig, DB_URL, DB_PORT);
    private static final Gson gson = new Gson();

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    public void setContextByChatId(String chatId, Context context) {
        Jedis jedis = jedisPool.getResource();

        jedis.set(chatId, gson.toJson(context));
    }

    public Context getContextByChatId(String chatId) {
        Jedis jedis = jedisPool.getResource();

        String maybe_context = jedis.get(chatId);

        if (maybe_context == null || maybe_context.isEmpty()) {
            Context context = new Context();
            jedis.set(chatId, gson.toJson(context));
            return context;
        }

        return gson.fromJson(maybe_context, Context.class);
    }
}
