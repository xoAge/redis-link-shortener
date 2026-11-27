package io.github.xoage.shortener.link;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CachedLinkRepositoryTest {

    private static final String URL = "https://example.com";

    private final InMemoryLinkRepository cache = new InMemoryLinkRepository();
    private final InMemoryLinkRepository source = new InMemoryLinkRepository();
    private final CachedLinkRepository repository = new CachedLinkRepository(cache, source);

    @Test
    void saveWritesToSourceAndCache() {
        repository.save("a", URL, Duration.ofMinutes(5));

        assertThat(source.findUrl("a")).contains(URL);
        assertThat(cache.findUrl("a")).contains(URL);
    }

    @Test
    void missLoadsFromSourceAndFillsCache() {
        source.save("a", URL, Duration.ofMinutes(5));

        assertThat(repository.findUrl("a")).contains(URL);

        assertThat(cache.findUrl("a")).contains(URL);
        assertThat(repository.misses()).isEqualTo(1);
        assertThat(repository.hits()).isZero();
    }

    @Test
    void secondReadIsCacheHit() {
        source.save("a", URL, Duration.ofMinutes(5));

        repository.findUrl("a");
        repository.findUrl("a");

        assertThat(repository.misses()).isEqualTo(1);
        assertThat(repository.hits()).isEqualTo(1);
    }

    @Test
    void unknownCodeIsNotCached() {
        assertThat(repository.findUrl("nope")).isEmpty();

        assertThat(cache.size()).isZero();
        assertThat(repository.misses()).isEqualTo(1);
    }

    @Test
    void linkAboutToExpireIsNotCached() {
        source.save("a", URL, Duration.ofMillis(500));

        assertThat(repository.findUrl("a")).contains(URL);

        assertThat(cache.size()).isZero();
    }

    @Test
    void remainingTtlFallsBackToSource() {
        source.save("a", URL, Duration.ofMinutes(5));

        assertThat(repository.remainingTtl("a")).contains(Duration.ofMinutes(5));
    }
}

