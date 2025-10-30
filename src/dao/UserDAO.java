package dao;

import database.DatabaseConnection;
import models.User;
import exceptions.InvalidLoginException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User login(String username, String password) throws InvalidLoginException {
        // attempt to query including student_class; if the DB doesn't have that column yet,
        // fallback to a simpler select (for backward compatibility)
        try {
            return loginWithStudentClass(username, password);
        } catch (SQLException ex) {
            // if the error indicates unknown column, try fallback
            try {
                return loginWithoutStudentClass(username, password);
            } catch (SQLException ex2) {
                throw new InvalidLoginException("Database error during login: " + ex2.getMessage());
            }
        }
    }

    private User loginWithStudentClass(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, full_name, role, student_class FROM users WHERE username = ? AND password = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPassword(rs.getString("password"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(rs.getString("role"));
                    u.setStudentClass(rs.getString("student_class"));
                    return u;
                } else {
                    throw new SQLException("Invalid username or password.");
                }
            }
        }
    }

    private User loginWithoutStudentClass(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, full_name, role FROM users WHERE username = ? AND password = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPassword(rs.getString("password"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(rs.getString("role"));
                    return u;
                } else {
                    throw new SQLException("Invalid username or password.");
                }
            }
        }
    }

    // CRUD: Add user (teacher or student)
    public int addUser(User user) throws SQLException {
        // try extended insert with student_class; fall back if the column doesn't exist
        try {
            String sql = "INSERT INTO users (username, password, full_name, role, student_class) VALUES (?, ?, ?, ?, ?)";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getRole());
                ps.setString(5, user.getStudentClass());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            // fallback: try insert without student_class
            String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getRole());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public List<User> listUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        // try extended select
        try {
            String sql = "SELECT id, username, full_name, role, student_class FROM users";
            try (Connection c = DatabaseConnection.getConnection();
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(sql)) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(rs.getString("role"));
                    u.setStudentClass(rs.getString("student_class"));
                    list.add(u);
                }
            }
        } catch (SQLException ex) {
            // fallback
            String sql = "SELECT id, username, full_name, role FROM users";
            try (Connection c = DatabaseConnection.getConnection();
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(sql)) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(rs.getString("role"));
                    list.add(u);
                }
            }
        }
        return list;
    }

    public User getById(int id) throws SQLException {
        // try extended
        try {
            String sql = "SELECT id, username, full_name, role, student_class FROM users WHERE id = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setFullName(rs.getString("full_name"));
                        u.setRole(rs.getString("role"));
                        u.setStudentClass(rs.getString("student_class"));
                        return u;
                    }
                }
            }
        } catch (SQLException ex) {
            String sql = "SELECT id, username, full_name, role FROM users WHERE id = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setFullName(rs.getString("full_name"));
                        u.setRole(rs.getString("role"));
                        return u;
                    }
                }
            }
        }
        return null;
    }

    public boolean updateUser(User user) throws SQLException {
        // try extended update
        try {
            String sql = "UPDATE users SET password = ?, full_name = ?, role = ?, student_class = ? WHERE id = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, user.getPassword());
                ps.setString(2, user.getFullName());
                ps.setString(3, user.getRole());
                ps.setString(4, user.getStudentClass());
                ps.setInt(5, user.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            // fallback without student_class
            String sql = "UPDATE users SET password = ?, full_name = ?, role = ? WHERE id = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, user.getPassword());
                ps.setString(2, user.getFullName());
                ps.setString(3, user.getRole());
                ps.setInt(4, user.getId());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
