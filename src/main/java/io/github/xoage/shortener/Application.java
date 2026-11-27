package io.github.xoage.shortener;

import com.zaxxer.hikari.HikariDataSource;
import io.github.xoage.shortener.client.Console;
import io.github.xoage.shortener.client.OpenCommand;
import io.github.xoage.shortener.client.ShortenCommand;
import io.github.xoage.shortener.client.StatsCommand;
import io.github.xoage.shortener.code.RedisCodeGenerator;
import io.github.xoage.shortener.config.AppConfig;
import io.github.xoage.shortener.db.Database;
import io.github.xoage.shortener.link.CachedLinkRepository;
import io.github.xoage.shortener.link.LinkService;
import io.github.xoage.shortener.link.PostgresLinkRepository;
import io.github.xoage.shortener.link.RedisClickCounter;
import io.github.xoage.shortener.link.RedisLinkRepository;
import io.github.xoage.shortener.link.UrlValidator;
import io.github.xoage.shortener.ratelimit.RateLimiter;
import io.github.xoage.shortener.ratelimit.SlidingWindowRateLimiter;
import redis.clients.jedis.JedisPool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/** Собирает все объекты приложения и владеет подключениями к Redis и PostgreSQL. */
public final class Application implements AutoCloseable {

    private static final int SHORTEN_LIMIT = 5;
    private static final Duration SHORTEN_WINDOW = Duration.ofMinutes(1);

    private final JedisPool pool;
    private final HikariDataSource dataSource;
    private final Console console;

    private Application(JedisPool pool, HikariDataSource dataSource, Console console) {
        this.pool = pool;
        this.dataSource = dataSource;
        this.console = console;
    }

    public static Application start(AppConfig config) {
        JedisPool pool = new JedisPool(config.redisHost(), config.redisPort());
        HikariDataSource dataSource;
        try {
            dataSource = Database.createMigrated(
                    config.dbUrl(), config.dbUser(), config.dbPassword());
        } catch (RuntimeException e) {
            pool.close();
            throw e;
        }
        return new Application(pool, dataSource, createConsole(pool, dataSource));
    }

    public void run() {
        console.run();
    }

    @Override
    public void close() {
        dataSource.close();
        pool.close();
    }

    private static Console createConsole(JedisPool pool, HikariDataSource dataSource) {
        LinkService service = new LinkService(
                new CachedLinkRepository(
                        new RedisLinkRepository(pool),
                        new PostgresLinkRepository(dataSource)),
                new RedisCodeGenerator(pool),
                UrlValidator.withDefaultRules(),
                new RedisClickCounter(pool));

        RateLimiter shortenLimiter =
                new SlidingWindowRateLimiter(pool, SHORTEN_LIMIT, SHORTEN_WINDOW);
        String clientId = "shorten:" + System.getProperty("user.name", "anonymous");

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));

        return new Console(List.of(
                new ShortenCommand(service, shortenLimiter, clientId, out),
                new OpenCommand(service, out),
                new StatsCommand(service, out)),
                in, out);
    }
}

