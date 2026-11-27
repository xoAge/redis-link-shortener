package io.github.xoage.shortener.client;

import java.util.List;

public interface Command {

    String name();

    String usage();

    String description();

    void execute(List<String> args);
}

