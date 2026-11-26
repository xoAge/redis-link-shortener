package io.github.xoage.shortener.link;

import java.time.Duration;

public record LinkStats(String code, String url, long clicks, Duration remainingTtl) {
}
