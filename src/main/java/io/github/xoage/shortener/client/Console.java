package io.github.xoage.shortener.client;

import io.github.xoage.shortener.exception.ShortenerException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Console {

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final BufferedReader in;
    private final PrintStream out;

    public Console(List<Command> availableCommands, BufferedReader in, PrintStream out) {
        availableCommands.forEach(command -> this.commands.put(command.name(), command));
        this.in = in;
        this.out = out;
    }

    public void run() {
        out.println("Сервис коротких ссылок. Введите help для списка команд.");
        while (true) {
            out.print("> ");
            out.flush();

            String line = readLine();
            if (line == null) {
                break;
            }
            Optional<ParsedCommand> parsed = CommandParser.parse(line);
            if (parsed.isPresent() && !handle(parsed.get())) {
                break;
            }
        }
        out.println("До свидания!");
    }

    /** Возвращает false, если нужно выйти. */
    private boolean handle(ParsedCommand parsed) {
        switch (parsed.name()) {
            case "exit", "quit" -> {
                return false;
            }
            case "help" -> printHelp();
            default -> execute(parsed);
        }
        return true;
    }

    private void execute(ParsedCommand parsed) {
        Command command = commands.get(parsed.name());
        if (command == null) {
            out.println("Неизвестная команда: " + parsed.name() + ". Введите help.");
            return;
        }
        try {
            command.execute(parsed.args());
        } catch (ShortenerException e) {
            out.println("Ошибка: " + e.getMessage());
        } catch (JedisException e) {
            out.println("Ошибка Redis: " + e.getMessage());
        }
    }

    private void printHelp() {
        commands.values().forEach(c -> out.printf("  %-38s %s%n", c.usage(), c.description()));
        out.printf("  %-38s %s%n", "help", "список команд");
        out.printf("  %-38s %s%n", "exit", "выход");
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

