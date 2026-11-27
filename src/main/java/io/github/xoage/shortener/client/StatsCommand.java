package io.github.xoage.shortener.client;

import io.github.xoage.shortener.exception.InvalidCommandException;
import io.github.xoage.shortener.link.LinkService;

import java.io.PrintStream;
import java.util.List;

public class StatsCommand implements Command {

    private final LinkService service;
    private final PrintStream out;

    public StatsCommand(LinkService service, PrintStream out) {
        this.service = service;
        this.out = out;
    }

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public String usage() {
        return "stats <код>";
    }

    @Override
    public String description() {
        return "ссылка, число переходов, остаток жизни";
    }

    @Override
    public void execute(List<String> args) {
        if (args.size() != 1) {
            throw new InvalidCommandException("Неверные аргументы. Использование: " + usage());
        }
        service.stats(args.get(0)).ifPresentOrElse(
                stats -> {
                    out.println("Ссылка: " + stats.url());
                    out.println("Переходов: " + stats.clicks());
                    out.println("Осталось жить: " + DurationFormatter.format(stats.remainingTtl()));
                },
                () -> out.println("Ссылка не найдена или истекла"));
    }
}

