package io.github.xoage.shortener.client;

import io.github.xoage.shortener.exception.InvalidTtlException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DurationParserTest {

    @ParameterizedTest
    @CsvSource({"30, 30", "30s, 30", "10m, 600", "2h, 7200", "7d, 604800", "5M, 300"})
    void parsesNumbersWithUnits(String text, long expectedSeconds) {
        assertThat(DurationParser.parse(text).toSeconds()).isEqualTo(expectedSeconds);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc", "10x", "s", "m10", "99999999999999999999"})
    void rejectsInvalidInput(String text) {
        assertThatThrownBy(() -> DurationParser.parse(text))
                .isInstanceOf(InvalidTtlException.class);
    }
}
