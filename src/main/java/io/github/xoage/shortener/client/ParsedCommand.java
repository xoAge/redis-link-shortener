package io.github.xoage.shortener.client;

import java.util.List;

public record ParsedCommand(String name, List<String> args) {
}
