import com.example.DAO;
import com.example.dto.Answer;
import com.example.AppService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class AppServiceTest {
    private Connection connection;
    private DAO dao;
    private AppService appService;

    @BeforeEach
    public void setUp() throws SQLException {
        // Set up in-memory H2 database with schema from schema.sql
        connection = DriverManager.getConnection("jdbc:h2:mem:testDatabank;INIT=RUNSCRIPT FROM 'classpath:schema.sql';DB_CLOSE_DELAY=-1");
        dao = new DAO(connection);
        appService = new AppService(dao);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up database state
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS question_answers");
            stmt.execute("DROP TABLE IF EXISTS answers");
            stmt.execute("DROP TABLE IF EXISTS questions");
        }
        connection.close();
    }

    @Test
    public void test_handleExit() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleExit();

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "Exiting...";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }

    @Test
    public void test_displayHelp() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.displayHelp();

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String[] lines = output.split("\n");

        assertEquals(4, lines.length, "The output should have exactly four lines.");

        assertEquals("To add a question, type the question followed by the answers in quotes.", lines[0].trim(),
                "The output is not as expected.");
        assertEquals("Example: What is the meaning of life? \"42\" \"To live\" \"To love\"", lines[1].trim(),
                "The output is not as expected.");
        assertEquals("To ask a question, type the question followed by a question mark.", lines[2].trim(),
                "The output is not as expected.");
        assertEquals("Example: What is the meaning of life?", lines[3].trim(),
                "The output is not as expected.");
    }

    @Test
    public void test_handleAddQuestion_Success() throws SQLException {
        String command = "What is the meaning of life? \"test1\" \"test2\" \"test3\"";
        appService.handleAddQuestion(command);

        List<Answer> answers = dao.getAnswersForQuestionText("What is the meaning of life?");
        assertEquals(3, answers.size());
        assertTrue(answers.stream().anyMatch(a -> a.getAnswerText().equals("test1")));
        assertTrue(answers.stream().anyMatch(a -> a.getAnswerText().equals("test2")));
        assertTrue(answers.stream().anyMatch(a -> a.getAnswerText().equals("test3")));
    }

    @Test
    public void test_handleAddQuestion_alreadyStored() throws SQLException {
        String command = "What is the capital of France? \"test1\" \"test2\" \"test3\"";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleAddQuestion(command);

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "Question already exists in the database.";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }

    @Test
    public void test_handleAddQuestion_WrongAnswerQuotes() throws SQLException {
        String command = "What is the capital of England? test1\" \"test2\" \"test3\"";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleAddQuestion(command);

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "Invalid format. Strings must be properly quoted.";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }

    @Test
    public void test_handleAddQuestion_NoAnswer() throws SQLException {
        String command = "What is the capital of England?";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleAddQuestion(command);

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "A question needs at least one answer.";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }

    @Test
    public void test_handleAddQuestion_NoQuestionMark() throws SQLException {
        String command = "What is the capital of England";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleAddQuestion(command);

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "Invalid command. Please enter a valid command.";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }

    @Test
    public void test_handleAddQuestion_QuestionToLong() throws SQLException {
        String command = "THIS STRING IS 256 CHARACTERS " +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxx? \"test1\"";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleAddQuestion(command);

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "Question is too long. Maximum length is 255 characters.";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }

    @Test
    public void test_handleAddQuestion_AnswerToLong() throws SQLException {
        String command = "A question? " +
                "\"THIS STRING IS 256 CHARACTERS " +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleAddQuestion(command);

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "Answer is too long. Maximum length is 255 characters.";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }





    @Test
    public void test_handleAskQuestion_withAnswers() {
        String command = "What is the capital of France?";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleAskQuestion(command);

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String[] lines = output.split("\n");

        assertEquals(2, lines.length, "The output should have exactly two lines.");

        assertEquals("- Paris", lines[0].trim(), "The output should be '- Paris'.");
        assertEquals("- London", lines[1].trim(), "The output should be '- London'.");
    }

    @Test
    public void test_handleAskQuestion_NoAnswers() {
        String command = "What is the meaning of life?";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleAskQuestion(command);

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "The answer to life, universe, and everything is 42.";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }

    @Test
    public void test_handleUnknownCommand() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        appService.handleUnknownCommand();

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        String expectedOutput = "Unknown command. If you are having problems try help.";

        assertEquals(expectedOutput, output, "The console output did not match the expected result.");
    }








}
