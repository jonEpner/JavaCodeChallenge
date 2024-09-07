package com.example;

import java.io.IOException;
import java.util.logging.*;

/**
 * This class is responsible for configuring the logger.
 * It sets up a FileHandler to log everything (FINE and above) to app.log
 * and a ConsoleHandler to only log SEVERE messages to the console.
 */
public class LoggerConfig {

    private static final Logger LOGGER = Logger.getLogger(LoggerConfig.class.getName());

    static {
        try {
            // FileHandler to log everything (FINE and above)
            FileHandler fileHandler = new FileHandler("app.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            // ConsoleHandler to only log SEVERE messages
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.SEVERE);

            // Remove default handlers
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }

            LOGGER.addHandler(fileHandler);
            LOGGER.addHandler(consoleHandler);

            LOGGER.setLevel(Level.ALL);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize logger", e);
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
