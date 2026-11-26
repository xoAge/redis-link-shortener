package io.github.xoage.shortener.link;

import io.github.xoage.shortener.code.CodeGenerator;
import io.github.xoage.shortener.exception.InvalidTtlException;

import java.time.Duration;
import java.util.Optional;

public class LinkService {

    public static final Duration DEFAULT_TTL = Duration.ofHours(24);
    public static final Duration MIN_TTL = Duration.ofSeconds(1);
    public static final Duration MAX_TTL = Duration.ofDays(365);

    private final LinkRepository repository;
    private final CodeGenerator codeGenerator;
    private final UrlValidator urlValidator;

    public LinkService(LinkRepository repository, CodeGenerator codeGenerator, UrlValidator urlValidator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
        this.urlValidator = urlValidator;
    }

    public String shorten(String url) {
        return shorten(url, DEFAULT_TTL);
    }

    public String shorten(String url, Duration ttl) {
        urlValidator.validate(url);
        validateTtl(ttl);

        String code = codeGenerator.next();
        repository.save(code, url, ttl);
        return code;
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
}
