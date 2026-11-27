package io.github.xoage.shortener.cli;

import io.github.xoage.shortener.client.ParsedCommand;

import java.util.Arrays;
import java.util.Optional;

public final class CommandParser {

    private CommandParser() {
    }

    public static Optional<ParsedCommand> parse(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }
        String[] parts = line.trim().split("\\s+");
        return Optional.of(new ParsedCommand(
                parts[0].toLowerCase(),
                Arrays.asList(parts).subList(1, parts.length)));
    }
}

