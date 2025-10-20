package models;

import java.time.LocalDateTime;

public class Quiz {
    private int id;
    private String title;
    private String description;
    private int teacherId;
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
