package io.github.xoage.shortener.link;

@FunctionalInterface
public interface UrlRule {

    void check(String url);
}
