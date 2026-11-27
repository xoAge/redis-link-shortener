package io.github.xoage.shortener.link;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class CachedLinkRepository implements LinkRepository{

    private static final Duration MIN_CACHE_TTL = Duration.ofSeconds(1);

    private final LinkRepository cache;
    private final LinkRepository source;
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public CachedLinkRepository(LinkRepository cache, LinkRepository source) {
        this.cache = cache;
        this.source = source;
    }

    @Override
    public void save(String code, String url, Duration ttl) {
        source.save(code, url, ttl);   // сначала источник: при коллизии кеш не тронем
        cache.save(code, url, ttl);
    }

    @Override
    public Optional<String> findUrl(String code) {
        Optional<String> cached = cache.findUrl(code);
        if (cached.isPresent()) {
            hits.incrementAndGet();
            return cached;
        }

        misses.incrementAndGet();
        Optional<String> found = source.findUrl(code);
        found.ifPresent(url -> refillCache(code, url));
        return found;
    }

    @Override
    public Optional<Duration> remainingTtl(String code) {
        return cache.remainingTtl(code).or(() -> source.remainingTtl(code));
    }

    public long hits() {
        return hits.get();
    }

    public long misses() {
        return misses.get();
    }

    private void refillCache(String code, String url) {
        // TTL кеша = остаток жизни ссылки, чтобы кеш не пережил саму ссылку
        source.remainingTtl(code)
                .filter(ttl -> ttl.compareTo(MIN_CACHE_TTL) >= 0)
                .ifPresent(ttl -> cache.save(code, url, ttl));
    }
}
