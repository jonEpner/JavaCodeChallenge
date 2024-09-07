-- Drop tables if they exist
DROP TABLE IF EXISTS question_answers;
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS questions;


-- Create table for Questions
CREATE TABLE IF NOT EXISTS questions
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(255) NOT NULL
);

-- Create table for Answers
CREATE TABLE IF NOT EXISTS answers
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    answer_text VARCHAR(255) NOT NULL
);

-- Create table to link Questions and Answers
CREATE TABLE IF NOT EXISTS question_answers
(
    question_id INT,
    answer_id   INT,
    PRIMARY KEY (question_id, answer_id),
    FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
    FOREIGN KEY (answer_id) REFERENCES answers (id) ON DELETE CASCADE
);

-- Insert initial data into Questions
INSERT INTO questions (question_text)
VALUES ('What is the capital of France?'),
       ('What is 2 + 2?');

-- Insert initial data into Answers
INSERT INTO answers (answer_text)
VALUES ('Paris'),
       ('4'),
       ('London'),
       ('5');

-- Link Questions and Answers
-- For example, linking 'What is the capital of France?' with 'Paris' and 'London'
INSERT INTO question_answers (question_id, answer_id)
VALUES (1, 1),
       (1, 3),
       (2, 2),
       (2, 4);