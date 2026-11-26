package io.github.xoage.shortener.exception;

public class InvalidUrlException extends ShortenerException {
    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
