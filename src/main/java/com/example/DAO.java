package com.example;


import com.example.dto.Answer;
import com.example.dto.Question;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import java.sql.SQLException;
import java.util.logging.*;

/**
 * This class is responsible for handling the database operations.
 */
public class DAO {
    private static final Logger LOGGER = LoggerConfig.getLogger();
    private final Connection CONNECTION;

    /**
     * Constructs an instance of DAO and establishes a database connection.
     *
     * @throws SQLException if a database access error occurs or the URL is incorrect.
     */
    public DAO() throws SQLException {
        // JDBC URL with INIT parameter to run the SQL script
        String jdbcUrl = "jdbc:h2:./data/databank;INIT=RUNSCRIPT FROM 'classpath:schema.sql'";
        try {
            CONNECTION = DriverManager.getConnection(jdbcUrl, "sa", "");
            LOGGER.log(Level.INFO, "Database connection established.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection.", e);
            throw e; // Rethrow the exception after logging
        }
    }

    /**
     * Constructs an instance of DAO with an existing connection.
     *
     * @param connection An existing database connection.
     */
    public DAO(Connection connection) {
        this.CONNECTION = connection;
    }

    /**
     * Retrieves a list of answers associated with a given question text from the database.
     *
     * @param questionText The text of the question for which the answers should be returned.
     * @return A {@link List} of {@link Answer} objects belonging to the question.
     * If no answers are found, an empty list is returned.
     */
    public List<Answer> getAnswersForQuestionText(String questionText) {
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT a.answer_text " +
                "FROM questions q " +
                "JOIN question_answers qa ON q.id = qa.question_id " +
                "JOIN answers a ON qa.answer_id = a.id " +
                "WHERE q.question_text = ?";

        try (var stmt = CONNECTION.prepareStatement(query)) {
            stmt.setString(1, questionText);
            try (var resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String answerText = resultSet.getString("answer_text");
                    answers.add(new Answer(answerText));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while fetching answers for question: " + questionText, e);
        }
        LOGGER.log(Level.INFO, "Fetched {0} answers for question: {1}", new Object[]{answers.size(), questionText});
        return answers;
    }

    /**
     * Adds a question with its answers to the database.
     *
     * @param question The question to be added to the database.
     * @return {@code true} if the question was successfully added, {@code false} otherwise.
     */
    public boolean addQuestion(Question question) throws SQLException {
        String insertQuestionSQL = "INSERT INTO questions (question_text) VALUES (?)";
        boolean isSuccessful = false;

        // Insert the question
        try (PreparedStatement pstmt = CONNECTION.prepareStatement(insertQuestionSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, question.getQuestionText());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int questionId = generatedKeys.getInt(1);

                // Insert each answer and link it to the question
                String insertAnswerSQL = "INSERT INTO answers (answer_text) VALUES (?)";
                try (PreparedStatement pstmtAnswer = CONNECTION.prepareStatement(insertAnswerSQL, Statement.RETURN_GENERATED_KEYS)) {
                    for (Answer answer : question.getAnswers()) {
                        pstmtAnswer.setString(1, answer.getAnswerText());
                        pstmtAnswer.executeUpdate();

                        ResultSet generatedAnswerKeys = pstmtAnswer.getGeneratedKeys();
                        if (generatedAnswerKeys.next()) {
                            int answerId = generatedAnswerKeys.getInt(1);

                            String insertQuestionAnswerSQL = "INSERT INTO question_answers (question_id, answer_id) VALUES (?, ?)";
                            try (PreparedStatement pstmtQuestionAnswer = CONNECTION.prepareStatement(insertQuestionAnswerSQL)) {
                                pstmtQuestionAnswer.setInt(1, questionId);
                                pstmtQuestionAnswer.setInt(2, answerId);
                                pstmtQuestionAnswer.executeUpdate();
                            }
                        }
                    }
                    isSuccessful = true;

                    //for logging
                    String answersString = question.getAnswers().stream()
                            .map(Answer::getAnswerText)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("No answers");

                    LOGGER.log(Level.INFO, "Question with answers added successfully: {0} {1}", new Object[]{question.getQuestionText(), answersString});
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error while inserting question and answers: " + question.getQuestionText(), e);
            isSuccessful = false;
        }
        return isSuccessful;
    }

    /**
     * Checks if a question is already stored in the database.
     *
     * @param questionText The text of the question to check.
     * @return {@code true} if the question is already stored, {@code false} otherwise.
     */
    public boolean isQuestionStored(String questionText) {
        String query = "SELECT * FROM questions WHERE question_text = ?";
        try (var stmt = CONNECTION.prepareStatement(query)) {
            stmt.setString(1, questionText);
            try (var resultSet = stmt.executeQuery()) {
                boolean exists = resultSet.next();
                LOGGER.log(Level.INFO, "Question {0} exists: {1}", new Object[]{questionText, exists});
                return exists;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if question is stored: " + questionText, e);
        }
        return false;
    }

}