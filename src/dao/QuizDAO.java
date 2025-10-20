package dao;

import database.DatabaseConnection;
import models.Quiz;
import exceptions.QuizNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {

    public int addQuiz(Quiz quiz) throws SQLException {
        String sql = "INSERT INTO quizzes (title, description, teacher_id) VALUES (?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            ps.setInt(3, quiz.getTeacherId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateQuiz(Quiz quiz) throws SQLException {
        String sql = "UPDATE quizzes SET title = ?, description = ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            ps.setInt(3, quiz.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteQuiz(int id) throws SQLException {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Quiz getQuizById(int id) throws SQLException, QuizNotFoundException {
        String sql = "SELECT id, title, description, teacher_id, created_at FROM quizzes WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Quiz q = new Quiz();
                    q.setId(rs.getInt("id"));
                    q.setTitle(rs.getString("title"));
                    q.setDescription(rs.getString("description"));
                    q.setTeacherId(rs.getInt("teacher_id"));
                    return q;
                } else {
                    throw new QuizNotFoundException("Quiz with id " + id + " not found.");
                }
            }
        }
    }

    public List<Quiz> listQuizzes() throws SQLException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT id, title, description, teacher_id FROM quizzes ORDER BY created_at DESC";
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTitle(rs.getString("title"));
                q.setDescription(rs.getString("description"));
                q.setTeacherId(rs.getInt("teacher_id"));
                list.add(q);
            }
        }
        return list;
    }

    public List<Quiz> listQuizzesByTeacher(int teacherId) throws SQLException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT id, title, description, teacher_id FROM quizzes WHERE teacher_id = ? ORDER BY created_at DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Quiz q = new Quiz();
                    q.setId(rs.getInt("id"));
                    q.setTitle(rs.getString("title"));
                    q.setDescription(rs.getString("description"));
                    q.setTeacherId(rs.getInt("teacher_id"));
                    list.add(q);
                }
            }
        }
        return list;
    }
}
