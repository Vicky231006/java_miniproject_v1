package dao;

import database.DatabaseConnection;
import models.Quiz;
import exceptions.QuizNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;


public class QuizDAO {

    public int addQuiz(Quiz quiz) throws SQLException {
        // try insert with extended quiz fields; fall back to legacy if needed
        try {
            String sql = "INSERT INTO quizzes (title, description, teacher_id, course_name, deadline, time_limit, target_stream, target_divisions) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, quiz.getTitle());
                ps.setString(2, quiz.getDescription());
                ps.setInt(3, quiz.getTeacherId());
                ps.setString(4, quiz.getCourseName());
                if (quiz.getDeadline() != null) ps.setTimestamp(5, Timestamp.valueOf(quiz.getDeadline()));
                else ps.setNull(5, Types.TIMESTAMP);
                if (quiz.getTimeLimit() != null) ps.setInt(6, quiz.getTimeLimit());
                else ps.setNull(6, Types.INTEGER);
                ps.setString(7, quiz.getTargetStream());
                ps.setString(8, quiz.getTargetDivisions());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            // fallback to legacy insert
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
        }
        return -1;
    }

    public boolean updateQuiz(Quiz quiz) throws SQLException {
        // try update with extended fields
        try {
            String sql = "UPDATE quizzes SET title = ?, description = ?, course_name = ?, deadline = ?, time_limit = ?, target_stream = ?, target_divisions = ? WHERE id = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, quiz.getTitle());
                ps.setString(2, quiz.getDescription());
                ps.setString(3, quiz.getCourseName());
                if (quiz.getDeadline() != null) ps.setTimestamp(4, Timestamp.valueOf(quiz.getDeadline()));
                else ps.setNull(4, Types.TIMESTAMP);
                if (quiz.getTimeLimit() != null) ps.setInt(5, quiz.getTimeLimit());
                else ps.setNull(5, Types.INTEGER);
                ps.setString(6, quiz.getTargetStream());
                ps.setString(7, quiz.getTargetDivisions());
                ps.setInt(8, quiz.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            String sql = "UPDATE quizzes SET title = ?, description = ? WHERE id = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, quiz.getTitle());
                ps.setString(2, quiz.getDescription());
                ps.setInt(3, quiz.getId());
                return ps.executeUpdate() > 0;
            }
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
        // try select with extended fields
        try {
            String sql = "SELECT id, title, description, teacher_id, course_name, deadline, time_limit, target_stream, target_divisions, created_at FROM quizzes WHERE id = ?";
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
                        q.setCourseName(rs.getString("course_name"));
                        Timestamp t = rs.getTimestamp("deadline");
                        if (t != null) q.setDeadline(t.toLocalDateTime());
                        int tl = rs.getInt("time_limit");
                        if (!rs.wasNull()) q.setTimeLimit(tl);
                        q.setTargetStream(rs.getString("target_stream"));
                        q.setTargetDivisions(rs.getString("target_divisions"));
                        return q;
                    } else {
                        throw new QuizNotFoundException("Quiz with id " + id + " not found.");
                    }
                }
            }
        } catch (SQLException ex) {
            // fallback to legacy
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
    }

    public List<Quiz> listQuizzes() throws SQLException {
        List<Quiz> list = new ArrayList<>();
        // try extended select first; fall back to legacy select if the schema doesn't have new columns
        String extended = "SELECT id, title, description, teacher_id, course_name, deadline, time_limit, target_stream, target_divisions, created_at FROM quizzes ORDER BY created_at DESC";
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(extended)) {
            System.out.println("QuizDAO: using extended SELECT for quizzes");
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTitle(rs.getString("title"));
                q.setDescription(rs.getString("description"));
                q.setTeacherId(rs.getInt("teacher_id"));
                // safe column reads: check metadata before accessing
                try {
                    boolean hasCourse = false;
                    boolean hasDeadline = false;
                    boolean hasTimeLimit = false;
                    boolean hasTargetStream = false;
                    boolean hasTargetDiv = false;
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        String col = meta.getColumnLabel(i);
                        if ("course_name".equalsIgnoreCase(col)) hasCourse = true;
                        if ("deadline".equalsIgnoreCase(col)) hasDeadline = true;
                        if ("time_limit".equalsIgnoreCase(col)) hasTimeLimit = true;
                        if ("target_stream".equalsIgnoreCase(col)) hasTargetStream = true;
                        if ("target_divisions".equalsIgnoreCase(col)) hasTargetDiv = true;
                    }
                    if (hasCourse) q.setCourseName(rs.getString("course_name"));
                    if (hasDeadline) {
                        Timestamp t = rs.getTimestamp("deadline");
                        if (t != null) q.setDeadline(t.toLocalDateTime());
                    }
                    if (hasTimeLimit) {
                        int tl = rs.getInt("time_limit");
                        if (!rs.wasNull()) q.setTimeLimit(tl);
                    }
                    if (hasTargetStream) q.setTargetStream(rs.getString("target_stream"));
                    if (hasTargetDiv) q.setTargetDivisions(rs.getString("target_divisions"));
                } catch (SQLException ignored) {
                    // be forgiving: if any column access fails, continue with default/nulls
                }
                list.add(q);
            }
            return list;
        } catch (SQLException ex) {
            System.out.println("QuizDAO: extended SELECT failed, falling back to legacy SELECT: " + ex.getMessage());
            // fallback to legacy select without the new columns
            String legacy = "SELECT id, title, description, teacher_id, created_at FROM quizzes ORDER BY created_at DESC";
            try (Connection c = DatabaseConnection.getConnection();
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(legacy)) {
                while (rs.next()) {
                    Quiz q = new Quiz();
                    q.setId(rs.getInt("id"));
                    q.setTitle(rs.getString("title"));
                    q.setDescription(rs.getString("description"));
                    q.setTeacherId(rs.getInt("teacher_id"));
                    list.add(q);
                }
                return list;
            }
    }
    }

    public List<Quiz> listQuizzesByTeacher(int teacherId) throws SQLException {
        List<Quiz> list = new ArrayList<>();
        // try extended select
        try {
            String sql = "SELECT id, title, description, teacher_id, course_name, deadline, time_limit, target_stream, target_divisions FROM quizzes WHERE teacher_id = ? ORDER BY created_at DESC";
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
                        q.setCourseName(rs.getString("course_name"));
                        Timestamp t = rs.getTimestamp("deadline");
                        if (t != null) q.setDeadline(t.toLocalDateTime());
                        int tl = rs.getInt("time_limit");
                        if (!rs.wasNull()) q.setTimeLimit(tl);
                        q.setTargetStream(rs.getString("target_stream"));
                        q.setTargetDivisions(rs.getString("target_divisions"));
                        list.add(q);
                    }
                }
            }
        } catch (SQLException ex) {
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
        }
        return list;
    }
}
