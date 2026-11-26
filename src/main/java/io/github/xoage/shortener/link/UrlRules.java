package io.github.xoage.shortener.link;

import io.github.xoage.shortener.exception.InvalidUrlException;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlRules {
    private UrlRules() {
    }

    public static UrlRule notBlank() {
        return url -> {
            if (url == null || url.isBlank()) {
                throw new InvalidUrlException("Ссылка не должна быть пустой");
            }
        };
    }

    public static UrlRule maxLength(int max) {
        return url -> {
            if (url.length() > max) {
                throw new InvalidUrlException("Ссылка длиннее " + max + " символов");
            }
        };
    }

    public static UrlRule httpOrHttps() {
        return url -> {
            URI uri = parse(url);
            if (!isHttpScheme(uri.getScheme())) {
                throw new InvalidUrlException("Разрешены только http и https: " + url);
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new InvalidUrlException("В ссылке нет адреса сайта: " + url);
            }
        };
    }

    private static URI parse(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Некорректная ссылка: " + url, e);
        }
    }

    private static boolean isHttpScheme(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }
}
