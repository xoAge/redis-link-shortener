package io.github.xoage.shortener.code;

public final class Base62 {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int BASE = ALPHABET.length();

    private Base62() {}

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Значение должно быть неотрицательным: " + value);
        }
        if (value == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(ALPHABET.charAt((int) (value % BASE)));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    public static long decode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Ключ не должен быть пустым");
        }
        long result = 0;
        for (char c : code.toCharArray()) {
            int digit = ALPHABET.indexOf(c);
            if (digit < 0) {
                throw new IllegalArgumentException("Неправильный символ: " + c);
            }
            result = result * BASE + digit;
        }
        return result;
    }
}
