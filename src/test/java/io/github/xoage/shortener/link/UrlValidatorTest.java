package io.github.xoage.shortener.link;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com",
            "http://example.com/a?b=c#d",
            "HTTPS://example.com/very/long/url",
            "https://github.com/xoAge"
    })
    void acceptsValidUrls(String url) {
        assertThatCode(() -> UrlValidator.validate(url)).doesNotThrowAnyException();
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
        assertThatThrownBy(() -> UrlValidator.validate(url))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTooLongUrl() {
        String url = "https://example.com/" + "a".repeat(2100);
        assertThatThrownBy(() -> UrlValidator.validate(url))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
