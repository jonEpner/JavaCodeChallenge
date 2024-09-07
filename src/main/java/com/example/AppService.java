package com.example;

import com.example.dto.Answer;
import com.example.dto.Question;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for handling the application logic.
 */
public class AppService {
    private DAO dao;

    /**
     * Constructs an instance of the AppService class.
     *
     * @throws SQLException if connection to the database cannot be established.
     */
    public AppService() throws SQLException {
        this.dao = new DAO();
    }

    /**
     * Constructs an instance of the AppService class with a provided DAO.
     *
     * @param dao The Data Access Object (DAO) to be used for database operations.
     */
    public AppService(DAO dao) {
        this.dao = dao;
    }

    /**
     * handles the exit command
     */
    public void handleExit() {
        System.out.println("Exiting...");
    }

    /**
     * Displays the help message
     */
    public void displayHelp() {
        System.out.println("To add a question, type the question followed by the answers in quotes.");
        System.out.println("Example: What is the meaning of life? \"42\" \"To live\" \"To love\"");
        System.out.println("To ask a question, type the question followed by a question mark.");
        System.out.println("Example: What is the meaning of life?");
    }

    /**
     * Handles the add question command
     * Validates the command and adds the question to the database
     *
     * @param command The command string containing the question and answers
     * @throws SQLException if an error occurs while adding the question to the database
     */
    public void handleAddQuestion(String command) throws SQLException {
        Question questionToAdd = validateAndSplitCmd(command, dao);
        if (questionToAdd != null) {
            boolean addSuccess = dao.addQuestion(questionToAdd);
            if (addSuccess) {
                System.out.println("Question added successfully.");
            } else {
                System.out.println("Failed to add question.");
            }
        }
    }

    /**
     * Handles the ask question command
     * Retrieves the answers for the question from the database
     *
     * @param command The command string containing the question
     */
    public void handleAskQuestion(String command) {
        String questionText = command.trim();
        List<Answer> answers = dao.getAnswersForQuestionText(questionText);
        if (answers.isEmpty()) {
            System.out.println("The answer to life, universe, and everything is 42.");
        } else {
            for (Answer answer : answers) {
                System.out.println(" - " + answer.getAnswerText());
            }
        }
    }

    /**
     * Handles unknown commands
     */
    public void handleUnknownCommand() {
        System.out.println("Unknown command. If you are having problems try help.");
    }

    /**
     * Validates the command and splits it into a Question object
     *
     * @param command The string containing the question and the answers.
     * @param dao The Data Access Object (DAO) used to interact with the database.
     * @return Question object if the command is valid, null otherwise
     */
    private static Question validateAndSplitCmd(String command, DAO dao) {
        // Check if the command has a question part
        if (command.contains("?")) {
            List<Answer> answers = new ArrayList<>();
            Question questionToAdd;

            String[] cmdParts = command.split("\\?");
            String question = cmdParts[0].trim() + "?";

            //Check if the question already exists in the database
            if (dao.isQuestionStored(question)) {
                System.out.println("Question already exists in the database.");
                return null;
            }

            // Check if the command has an answer part
            if (cmdParts.length > 1) {
                String partAfterQuestionMark = cmdParts[1].trim();

                String patternString = "(\"[^\"]*\"\\s*)+";
                Pattern fullPattern = Pattern.compile(patternString);
                Matcher fullMatcher = fullPattern.matcher(partAfterQuestionMark);

                // If the whole string matches the pattern, it is a valid command
                if (fullMatcher.matches()) {
                    // Extract all the strings between quotes -> answers
                    Pattern quotePattern = Pattern.compile("\"([^\"]*)\"");
                    Matcher matcher = quotePattern.matcher(partAfterQuestionMark);

                    // Add all the extracted strings to the answers list
                    while (matcher.find()) {
                        String extractedString = matcher.group(1);
                        answers.add(new Answer(extractedString));
                    }

                } else {
                    System.out.println("Invalid format. Strings must be properly quoted.");
                    return null;
                }

            } else {
                System.out.println("A question needs at least one answer.");
                return null;
            }

            // Check if the question is within the length limits
            if (question.length() > 255) {
                System.out.println("Question is too long. Maximum length is 255 characters.");
                return null;
            }

            // Check if the answers are within the length limits
            for (Answer answer : answers) {
                if (answer.getAnswerText().length() > 255) {
                    System.out.println("Answer is too long. Maximum length is 255 characters.");
                    return null;
                }
            }

            // Create a new Question object with the validated question and answers
            questionToAdd = new Question(question, answers);
            return questionToAdd;

        } else {
            System.out.println("Invalid command. Please enter a valid command.");
            return null;
        }
    }
}
