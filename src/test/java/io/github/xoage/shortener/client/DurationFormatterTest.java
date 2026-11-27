package io.github.xoage.shortener.client;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DurationFormatterTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0 сек",
            "59, 59 сек",
            "60, 1 мин",
            "3661, 1 ч 1 мин 1 сек",
            "86400, 1 д",
            "90061, 1 д 1 ч 1 мин 1 сек"
    })
    void formatsDuration(long seconds, String expected) {
        assertThat(DurationFormatter.format(Duration.ofSeconds(seconds))).isEqualTo(expected);
    }
}
