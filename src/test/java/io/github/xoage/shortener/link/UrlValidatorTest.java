package io.github.xoage.shortener.link;

import io.github.xoage.shortener.exception.InvalidUrlException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlValidatorTest {

    private final UrlValidator validator = UrlValidator.withDefaultRules();

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com",
            "http://example.com/a?b=c#d",
            "HTTPS://example.com/very/long/url",
            "https://github.com/xoAge"
    })
    void acceptsValidUrls(String url) {
        assertThatCode(() -> validator.validate(url)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "example.com",
            "ftp://example.com",
            "javascript:alert(1)",
            "https://",
            "http://exa mple.com"
    })
    void rejectsInvalidUrls(String url) {
        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsTooLongUrl() {
        String url = "https://example.com/" + "a".repeat(2100);
        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(InvalidUrlException.class);
    }
}

