package io.github.xoage.shortener.link;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryLinkRepository implements LinkRepository {

    private final Map<String, String> links = new HashMap<>();
    private Duration lastTtl;

    @Override
    public void save(String code, String url, Duration ttl) {
        links.put(code, url);
        lastTtl = ttl;
    }

    @Override
    public Optional<String> findUrl(String code) {
        return Optional.ofNullable(links.get(code));
    }

    Duration lastTtl() {
        return lastTtl;
    }

    int size() {
        return links.size();
    }

    @Override
    public Optional<Duration> remainingTtl(String code) {
        return links.containsKey(code) ? Optional.of(lastTtl) : Optional.empty();
    }

}

