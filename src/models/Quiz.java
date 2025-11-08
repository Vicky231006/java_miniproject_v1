package models;

import java.time.LocalDateTime;

/**
 * Quiz model extended with course, deadline, time limit and targeting fields.
 */
public class Quiz {
    private int id;
    private String title;
    private String description;
    private int teacherId;

    // New fields
    private String courseName;
    private LocalDateTime deadline;
    private Integer timeLimit; // minutes
    private String targetStream; // comma-separated or 'ALL'
    private String targetDivisions; // comma-separated or 'ALL'

    private LocalDateTime createdAt;

    public Quiz() {}

    public Quiz(int id, String title, String description, int teacherId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public Integer getTimeLimit() { return timeLimit; }
    public void setTimeLimit(Integer timeLimit) { this.timeLimit = timeLimit; }

    public String getTargetStream() { return targetStream; }
    public void setTargetStream(String targetStream) { this.targetStream = targetStream; }

    public String getTargetDivisions() { return targetDivisions; }
    public void setTargetDivisions(String targetDivisions) { this.targetDivisions = targetDivisions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
