package io.github.xoage.shortener.link;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlValidator {

    private static final int MAX_LENGTH = 2048;

    private UrlValidator() {}

    public static void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Ссылка не должна быть пустой");
        }
        if (url.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Ссылка длинее " + MAX_LENGTH + " символов");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Некорректная ссылка: " + url, e);
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Разрешены только http и https: " + url);
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("В ссылке нет адреса сайта: " + url);
        }
    }
}
