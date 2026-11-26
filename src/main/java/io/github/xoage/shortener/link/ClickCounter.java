package io.github.xoage.shortener.link;

public interface ClickCounter {

    long increment(String code);

    long count(String code);
}
