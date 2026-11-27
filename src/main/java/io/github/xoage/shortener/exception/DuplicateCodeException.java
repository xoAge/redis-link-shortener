package io.github.xoage.shortener.exception;

public class DuplicateCodeException extends ShortenerException {

    public DuplicateCodeException(String code) {
        super("Код уже занят: " + code);
    }

    public DuplicateCodeException(String code, Throwable cause) {
        super("Код уже занят: " + code, cause);
    }
}
