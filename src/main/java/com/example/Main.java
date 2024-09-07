package com.example;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    /**
     * Enum representing the different types of commands that can be entered by the user.
     */
    private enum CommandType {
        EXIT,
        HELP,
        ADD_QUESTION,
        ASK_QUESTION,
        UNKNOWN
    }

    public static void main(String[] args) {
        try {
            AppService appService = new AppService();
            Scanner scanner = new Scanner(System.in);
            String commandString;
            CommandType command;

            System.out.println("Database application running. Type 'exit' to stop.");

            while (true) {
                System.out.println("Add or ask a question:");
                commandString = scanner.nextLine().trim();
                command = getCommandType(commandString);

                if (command == CommandType.EXIT) {
                    appService.handleExit();
                    break;
                }

                if (command == CommandType.HELP) {
                    appService.displayHelp();
                }

                else if (command == CommandType.ADD_QUESTION) {
                    appService.handleAddQuestion(commandString);
                }

                else if (command == CommandType.ASK_QUESTION) {
                    appService.handleAskQuestion(commandString);
                }

                else if (command == CommandType.UNKNOWN){
                    appService.handleUnknownCommand();
                }
            }
            scanner.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines the type of command entered by the user.
     *
     * @param command The command entered by the user.
     * @return The type of command entered by the user.
     */
    private static CommandType getCommandType(String command) {
        if ("exit".equalsIgnoreCase(command)) {
            return CommandType.EXIT;
        } else if ("help".equalsIgnoreCase(command)) {
            return CommandType.HELP;
        } else if (command.endsWith("\"")) {
            return CommandType.ADD_QUESTION;
        } else if (command.endsWith("?")) {
            return CommandType.ASK_QUESTION;
        } else {
            return CommandType.UNKNOWN;
        }
    }
}
