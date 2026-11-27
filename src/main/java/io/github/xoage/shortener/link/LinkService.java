package io.github.xoage.shortener.link;

import io.github.xoage.shortener.code.CodeGenerator;
import io.github.xoage.shortener.exception.DuplicateCodeException;
import io.github.xoage.shortener.exception.InvalidTtlException;

import java.time.Duration;
import java.util.Optional;

public class LinkService {

    private static final int MAX_CODE_ATTEMPTS = 3;
    public static final Duration DEFAULT_TTL = Duration.ofHours(24);
    public static final Duration MIN_TTL = Duration.ofSeconds(1);
    public static final Duration MAX_TTL = Duration.ofDays(365);

    private final LinkRepository repository;
    private final CodeGenerator codeGenerator;
    private final UrlValidator urlValidator;
    private final ClickCounter clickCounter;

    public LinkService(LinkRepository repository, CodeGenerator codeGenerator, UrlValidator urlValidator, ClickCounter clickCounter) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
        this.urlValidator = urlValidator;
        this.clickCounter = clickCounter;
    }

    public String shorten(String url) {
        return shorten(url, DEFAULT_TTL);
    }

    public String shorten(String url, Duration ttl) {
        urlValidator.validate(url);
        validateTtl(ttl);
        return saveWithNewCode(url, ttl, MAX_CODE_ATTEMPTS);
    }

    private String saveWithNewCode(String url, Duration ttl, int attemptsLeft) {
        String code = codeGenerator.next();
        try {
            repository.save(code, url, ttl);
            return code;
        } catch (DuplicateCodeException e) {
            if (attemptsLeft <= 1) {
                throw e;
            }
            return saveWithNewCode(url, ttl, attemptsLeft - 1);
        }
    }

    public Optional<String> resolve(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return repository.findUrl(code);
    }

    private void validateTtl(Duration ttl) {
        if (ttl == null || ttl.compareTo(MIN_TTL) < 0) {
            throw new InvalidTtlException("TTL должен быть не меньше 1 секунды");
        }
        if (ttl.compareTo(MAX_TTL) > 0) {
            throw new InvalidTtlException("TTL не может быть больше " + MAX_TTL.toDays() + " дней");
        }
    }

    /** Возвращает ссылку и засчитывает переход. */
    public Optional<String> open(String code) {
        Optional<String> url = resolve(code);
        url.ifPresent(found -> clickCounter.increment(code));
        return url;
    }

    public Optional<LinkStats> stats(String code) {
        return resolve(code).map(url -> new LinkStats(
                code,
                url,
                clickCounter.count(code),
                repository.remainingTtl(code).orElse(Duration.ZERO)));
    }
}
