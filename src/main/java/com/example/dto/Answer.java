package com.example.dto;

/**
 * This class represents an answer to a question.
 */
public class Answer {
    private final String answerString;

    public Answer(String answerString) {
        this.answerString = answerString;
    }

    public String getAnswerText() {
        return answerString;
    }
}
