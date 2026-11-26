package io.github.xoage.shortener.link;

import java.util.List;

public final class UrlValidator {

    private static final int MAX_LENGTH = 2048;

    private final List<UrlRule> rules;

    public UrlValidator(List<UrlRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static UrlValidator withDefaultRules() {
        return new UrlValidator(List.of(
                UrlRules.notBlank(),
                UrlRules.maxLength(MAX_LENGTH),
                UrlRules.httpOrHttps()
        ));
    }

    public void validate(String url) {
        rules.forEach(rule -> rule.check(url));
    }
}
