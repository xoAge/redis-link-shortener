package io.github.xoage.shortener.client;

import io.github.xoage.shortener.exception.InvalidCommandException;
import io.github.xoage.shortener.exception.RateLimitExceededException;
import io.github.xoage.shortener.link.LinkService;
import io.github.xoage.shortener.ratelimit.RateLimitResult;
import io.github.xoage.shortener.ratelimit.RateLimiter;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;

public class ShortenCommand implements Command {

    private static final String TTL_FLAG = "--ttl";

    private final LinkService service;
    private final RateLimiter rateLimiter;
    private final String clientId;
    private final PrintStream out;

    public ShortenCommand(LinkService service, RateLimiter rateLimiter,
                          String clientId, PrintStream out) {
        this.service = service;
        this.rateLimiter = rateLimiter;
        this.clientId = clientId;
        this.out = out;
    }

    @Override
    public String name() {
        return "shorten";
    }

    @Override
    public String usage() {
        return "shorten <url> [--ttl 30s|10m|2h|7d]";
    }

    @Override
    public String description() {
        return "сократить ссылку";
    }

    @Override
    public void execute(List<String> args) {
        if (args.isEmpty()) {
            throw usageError();
        }
        Duration ttl = parseTtl(args.subList(1, args.size()));

        RateLimitResult limit = rateLimiter.tryAcquire(clientId);
        if (!limit.allowed()) {
            throw new RateLimitExceededException(limit.retryAfter());
        }

        String code = service.shorten(args.get(0), ttl);
        out.println("Короткий код: " + code);
        out.println("Осталось запросов в этом окне: " + limit.remaining());
    }

    private Duration parseTtl(List<String> options) {
        if (options.isEmpty()) {
            return LinkService.DEFAULT_TTL;
        }
        if (options.size() == 2 && TTL_FLAG.equals(options.get(0))) {
            return DurationParser.parse(options.get(1));
        }
        throw usageError();
    }

    private InvalidCommandException usageError() {
        return new InvalidCommandException("Неверные аргументы. Использование: " + usage());
    }
}
