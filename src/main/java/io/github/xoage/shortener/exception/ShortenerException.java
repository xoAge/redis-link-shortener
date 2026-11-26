package io.github.xoage.shortener.exception;

public class ShortenerException extends RuntimeException {
    public ShortenerException(String message) {
        super(message);
    }

    public ShortenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
