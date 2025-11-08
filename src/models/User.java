package models;

import java.time.LocalDateTime;

/**
 * User model extended to support roll number, stream and division for students.
 * Backwards-compatible fields (studentClass) are kept for older DBs.
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String role; // TEACHER or STUDENT

    // New student-specific fields
    private String rollNumber;
    private String stream; // e.g., "Computer Engg", "Mech Engg"
    private String division; // e.g., "A", "B"

    // Legacy field kept for compatibility
    private String studentClass;

    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username, String password, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getStream() { return stream; }
    public void setStream(String stream) { this.stream = stream; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    // Legacy compatibility
    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
