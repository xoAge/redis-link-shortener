package io.github.xoage.shortener;

import io.github.xoage.shortener.config.AppConfig;

public class Main {

    public static void main(String[] args) {
        try (Application app = Application.start(AppConfig.fromEnv())) {
            app.run();
        }
    }
}
