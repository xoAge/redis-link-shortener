package io.github.xoage.shortener.client;

import io.github.xoage.shortener.exception.InvalidCommandException;
import io.github.xoage.shortener.link.LinkService;

import java.io.PrintStream;
import java.util.List;

public class OpenCommand implements Command {

    private final LinkService service;
    private final PrintStream out;

    public OpenCommand(LinkService service, PrintStream out) {
        this.service = service;
        this.out = out;
    }

    @Override
    public String name() {
        return "open";
    }

    @Override
    public String usage() {
        return "open <код>";
    }

    @Override
    public String description() {
        return "открыть ссылку и засчитать переход";
    }

    @Override
    public void execute(List<String> args) {
        if (args.size() != 1) {
            throw new InvalidCommandException("Неверные аргументы. Использование: " + usage());
        }
        out.println(service.open(args.get(0)).orElse("Ссылка не найдена или истекла"));
    }
}