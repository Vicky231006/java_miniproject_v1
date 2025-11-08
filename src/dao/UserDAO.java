package dao;

import database.DatabaseConnection;
import models.User;
import exceptions.InvalidLoginException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User login(String username, String password) throws InvalidLoginException {
        // Prefer new fields (roll_number, stream, division). Fall back to older student_class or simple login.
        try {
            return loginWithNewFields(username, password);
        } catch (SQLException ex) {
            try {
                return loginWithStudentClass(username, password);
            } catch (SQLException ex2) {
                try {
                    return loginWithoutStudentClass(username, password);
                } catch (SQLException ex3) {
                    throw new InvalidLoginException("Database error during login: " + ex3.getMessage());
                }
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

    private User loginWithNewFields(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, full_name, role, roll_number, stream, division FROM users WHERE username = ? AND password = ?";
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
                    u.setRollNumber(rs.getString("roll_number"));
                    u.setStream(rs.getString("stream"));
                    u.setDivision(rs.getString("division"));
                    // keep legacy field in sync
                    u.setStudentClass(rs.getString("division"));
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
        // try to insert with new fields (roll_number, stream, division); fall back to older columns
        try {
            String sql = "INSERT INTO users (username, password, full_name, role, roll_number, stream, division) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getRole());
                ps.setString(5, user.getRollNumber());
                ps.setString(6, user.getStream());
                ps.setString(7, user.getDivision());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException ex) {
            // fallback: try legacy student_class column insert
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
                        if (keys.next()) return keys.getInt(1);
                    }
                }
            } catch (SQLException ex2) {
                // final fallback: minimal insert
                String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
                try (Connection c = DatabaseConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getPassword());
                    ps.setString(3, user.getFullName());
                    ps.setString(4, user.getRole());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) return keys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public List<User> listUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        // try select with new student fields
        try {
            String sql = "SELECT id, username, full_name, role, roll_number, stream, division FROM users";
            try (Connection c = DatabaseConnection.getConnection();
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(sql)) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(rs.getString("role"));
                    u.setRollNumber(rs.getString("roll_number"));
                    u.setStream(rs.getString("stream"));
                    u.setDivision(rs.getString("division"));
                    u.setStudentClass(rs.getString("division"));
                    list.add(u);
                }
            }
        } catch (SQLException ex) {
            // fallback to legacy
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
        }
        return list;
    }

    public User getById(int id) throws SQLException {
        // try with new fields
        try {
            String sql = "SELECT id, username, full_name, role, roll_number, stream, division FROM users WHERE id = ?";
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
                        u.setRollNumber(rs.getString("roll_number"));
                        u.setStream(rs.getString("stream"));
                        u.setDivision(rs.getString("division"));
                        u.setStudentClass(rs.getString("division"));
                        return u;
                    }
                }
            }
        } catch (SQLException ex) {
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
        }
        return null;
    }

    public boolean updateUser(User user) throws SQLException {
        // try update with new fields
        try {
            String sql = "UPDATE users SET password = ?, full_name = ?, role = ?, roll_number = ?, stream = ?, division = ? WHERE id = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, user.getPassword());
                ps.setString(2, user.getFullName());
                ps.setString(3, user.getRole());
                ps.setString(4, user.getRollNumber());
                ps.setString(5, user.getStream());
                ps.setString(6, user.getDivision());
                ps.setInt(7, user.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            // fallback to legacy update
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
