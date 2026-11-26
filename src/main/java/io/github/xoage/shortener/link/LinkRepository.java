package io.github.xoage.shortener.link;

import java.time.Duration;
import java.util.Optional;

public interface LinkRepository {

    void save(String code, String url, Duration ttl);

    Optional<String> findUrl(String code);
}
