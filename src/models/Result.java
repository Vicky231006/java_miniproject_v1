package models;

import java.time.LocalDateTime;

public class Result {
    private int id;
    private int studentId;
    private int quizId;
    private double score;
    private int totalQuestions;
    private String answers; // simple representation
    private LocalDateTime takenAt;

    public Result() {}

    public Result(int id, int studentId, int quizId, double score, int totalQuestions, String answers) {
        this.id = id;
        this.studentId = studentId;
        this.quizId = quizId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.answers = answers;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }
}
