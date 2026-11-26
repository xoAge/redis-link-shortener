package io.github.xoage.shortener.link;

import java.util.HashMap;
import java.util.Map;

class InMemoryClickCounter implements ClickCounter {

    private final Map<String, Long> counters = new HashMap<>();

    @Override
    public long increment(String code) {
        return counters.merge(code, 1L, Long::sum);
    }

    @Override
    public long count(String code) {
        return counters.getOrDefault(code, 0L);
    }
}
