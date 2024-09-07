package com.example.dto;
import java.util.List;

/**
 * This class represents a question with its answers.
 */
public class Question {
    private final String questionString;
    private final List<Answer> answers;

    public Question(String questionString, List<Answer> answers) {
        this.questionString = questionString;
        this.answers = answers;
    }

    public String getQuestionText() {
        return questionString;
    }

    public List<Answer> getAnswers() {
        return answers;
    }
}
