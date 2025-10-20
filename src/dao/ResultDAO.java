package dao;

import database.DatabaseConnection;
import models.Result;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {

    public int addResult(Result r) throws SQLException {
        String sql = "INSERT INTO results (student_id, quiz_id, score, total_questions, answers) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getStudentId());
            ps.setInt(2, r.getQuizId());
            ps.setDouble(3, r.getScore());
            ps.setInt(4, r.getTotalQuestions());
            ps.setString(5, r.getAnswers());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public List<Result> listResultsByStudent(int studentId) throws SQLException {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT id, student_id, quiz_id, score, total_questions, answers, taken_at FROM results WHERE student_id = ? ORDER BY taken_at DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Result r = new Result();
                    r.setId(rs.getInt("id"));
                    r.setStudentId(rs.getInt("student_id"));
                    r.setQuizId(rs.getInt("quiz_id"));
                    r.setScore(rs.getDouble("score"));
                    r.setTotalQuestions(rs.getInt("total_questions"));
                    r.setAnswers(rs.getString("answers"));
                    r.setTakenAt(rs.getTimestamp("taken_at").toLocalDateTime());
                    list.add(r);
                }
            }
        }
        return list;
    }

    public List<Result> listResultsByQuiz(int quizId) throws SQLException {
        List<Result> list = new ArrayList<>();
        String sql = "SELECT id, student_id, quiz_id, score, total_questions, answers, taken_at FROM results WHERE quiz_id = ? ORDER BY taken_at DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Result r = new Result();
                    r.setId(rs.getInt("id"));
                    r.setStudentId(rs.getInt("student_id"));
                    r.setQuizId(rs.getInt("quiz_id"));
                    r.setScore(rs.getDouble("score"));
                    r.setTotalQuestions(rs.getInt("total_questions"));
                    r.setAnswers(rs.getString("answers"));
                    r.setTakenAt(rs.getTimestamp("taken_at").toLocalDateTime());
                    list.add(r);
                }
            }
        }
        return list;
    }
}
