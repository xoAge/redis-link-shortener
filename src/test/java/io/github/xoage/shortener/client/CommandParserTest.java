package io.github.xoage.shortener.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandParserTest {

    @Test
    void parsesNameAndArgsIgnoringExtraSpacesAndCase() {
        ParsedCommand parsed = CommandParser.parse("  SHORTEN   https://a.com  --ttl 5 ").orElseThrow();

        assertThat(parsed.name()).isEqualTo("shorten");
        assertThat(parsed.args()).isEqualTo(List.of("https://a.com", "--ttl", "5"));
    }

    @Test
    void commandWithoutArgsHasEmptyArgs() {
        assertThat(CommandParser.parse("help").orElseThrow().args()).isEmpty();
    }

    @Test
    void blankAndNullLinesAreEmpty() {
        assertThat(CommandParser.parse("   ")).isEmpty();
        assertThat(CommandParser.parse(null)).isEmpty();
    }
}
