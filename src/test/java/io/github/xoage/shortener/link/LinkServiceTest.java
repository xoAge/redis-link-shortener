package io.github.xoage.shortener.link;

import io.github.xoage.shortener.code.CodeGenerator;
import io.github.xoage.shortener.exception.InvalidTtlException;
import io.github.xoage.shortener.exception.InvalidUrlException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinkServiceTest {

    private static final String URL = "https://example.com";

    private final InMemoryLinkRepository repository = new InMemoryLinkRepository();
    private final AtomicInteger generated = new AtomicInteger();
    private final CodeGenerator generator = () -> "code" + generated.incrementAndGet();
    private final LinkService service =
            new LinkService(repository, generator, UrlValidator.withDefaultRules());

    @Test
    void shortenSavesLinkAndReturnsCode() {
        String code = service.shorten(URL);

        assertThat(code).isEqualTo("code1");
        assertThat(service.resolve(code)).contains(URL);
    }

    @Test
    void shortenUsesDefaultTtl() {
        service.shorten(URL);

        assertThat(repository.lastTtl()).isEqualTo(LinkService.DEFAULT_TTL);
    }

    @Test
    void shortenPassesCustomTtlToRepository() {
        service.shorten(URL, Duration.ofMinutes(5));

        assertThat(repository.lastTtl()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void invalidUrlIsNotSavedAndCodeIsNotConsumed() {
        assertThatThrownBy(() -> service.shorten("не ссылка"))
                .isInstanceOf(InvalidUrlException.class);

        assertThat(repository.size()).isZero();
        assertThat(generated.get()).isZero();
    }

    @Test
    void rejectsTtlOutOfRange() {
        assertThatThrownBy(() -> service.shorten(URL, Duration.ZERO))
                .isInstanceOf(InvalidTtlException.class);
        assertThatThrownBy(() -> service.shorten(URL, Duration.ofMillis(500)))
                .isInstanceOf(InvalidTtlException.class);
        assertThatThrownBy(() -> service.shorten(URL, LinkService.MAX_TTL.plusSeconds(1)))
                .isInstanceOf(InvalidTtlException.class);
        assertThatThrownBy(() -> service.shorten(URL, null))
                .isInstanceOf(InvalidTtlException.class);

        assertThat(generated.get()).isZero();
    }

    @Test
    void resolveReturnsEmptyForUnknownBlankAndNullCode() {
        assertThat(service.resolve("unknown")).isEmpty();
        assertThat(service.resolve("  ")).isEmpty();
        assertThat(service.resolve(null)).isEmpty();
    }
}
