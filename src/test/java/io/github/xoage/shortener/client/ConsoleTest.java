package io.github.xoage.shortener.client;

import io.github.xoage.shortener.link.InMemoryClickCounter;
import io.github.xoage.shortener.link.InMemoryLinkRepository;
import io.github.xoage.shortener.link.LinkService;
import io.github.xoage.shortener.link.UrlValidator;
import io.github.xoage.shortener.ratelimit.RateLimitResult;
import io.github.xoage.shortener.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleTest {

    private static final RateLimiter UNLIMITED =
            clientId -> new RateLimitResult(true, 99, Duration.ZERO);
    private static final RateLimiter BLOCKED =
            clientId -> new RateLimitResult(false, 0, Duration.ofSeconds(30));

    private final AtomicInteger generated = new AtomicInteger();
    private final LinkService service = new LinkService(
            new InMemoryLinkRepository(),
            () -> "code" + generated.incrementAndGet(),
            UrlValidator.withDefaultRules(),
            new InMemoryClickCounter());
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    private String run(RateLimiter limiter, String... lines) {
        BufferedReader in = new BufferedReader(new StringReader(String.join("\n", lines) + "\n"));
        new Console(List.of(
                new ShortenCommand(service, limiter, "test", out),
                new OpenCommand(service, out),
                new StatsCommand(service, out)),
                in, out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void shortenThenOpenReturnsOriginalUrl() {
        String output = run(UNLIMITED, "shorten https://example.com/page", "open code1", "exit");

        assertThat(output)
                .contains("Короткий код: code1")
                .contains("https://example.com/page");
    }

    @Test
    void statsShowsClicksAndRemainingTtl() {
        String output = run(UNLIMITED,
                "shorten https://example.com --ttl 10m", "open code1", "stats code1", "exit");

        assertThat(output)
                .contains("Переходов: 1")
                .contains("Осталось жить: 10 мин");
    }

    @Test
    void ttlOptionIsPassedToService() {
        run(UNLIMITED, "shorten https://example.com --ttl 2h", "exit");

        assertThat(service.stats("code1").orElseThrow().remainingTtl())
                .isEqualTo(Duration.ofHours(2));
    }

    @Test
    void blockedClientGetsMessageAndNoCodeIsConsumed() {
        String output = run(BLOCKED, "shorten https://example.com", "exit");

        assertThat(output).contains("Слишком много запросов");
        assertThat(generated.get()).isZero();
    }

    @Test
    void domainErrorsArePrintedWithoutCrashing() {
        String output = run(UNLIMITED,
                "shorten не-ссылка", "shorten", "open", "open nope", "exit");

        assertThat(output)
                .contains("Ошибка:")
                .contains("Использование: shorten")
                .contains("Использование: open")
                .contains("Ссылка не найдена или истекла");
    }

    @Test
    void unknownCommandAndHelp() {
        String output = run(UNLIMITED, "foo", "help", "exit");

        assertThat(output)
                .contains("Неизвестная команда: foo")
                .contains("shorten <url>");
    }

    @Test
    void endOfInputStopsConsole() {
        assertThat(run(UNLIMITED, "help")).contains("До свидания!");
    }
}
