package io.github.xoage.shortener.code;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Base62Test {

    @Test
    void encodesZero() {
        assertThat(Base62.encode(0)).isEqualTo("0");
    }

    @Test
    void encodesKnownValues() {
        assertThat(Base62.encode(61)).isEqualTo("z");
        assertThat(Base62.encode(62)).isEqualTo("10");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 61, 62, 3843, 916_132_832L, Long.MAX_VALUE})
    void decodeInverseOfEncode(long value) {
        assertThat(Base62.decode(Base62.encode(value))).isEqualTo(value);
    }

    @Test
    void rejectsNegativeValues() {
        assertThatThrownBy(() -> Base62.encode(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsIllegalCharacterOnDecode() {
        assertThatThrownBy(() -> Base62.decode("ab-c")).isInstanceOf(IllegalArgumentException.class);
    }
}
