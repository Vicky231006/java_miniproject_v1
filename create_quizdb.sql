-- File: sql/create_quizdb.sql
-- Create database and schema for Online Quiz Management System
CREATE DATABASE IF NOT EXISTS quizdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quizdb;

-- Users table (teachers and students)
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(200) NOT NULL,
  role ENUM('TEACHER','STUDENT') NOT NULL,
  roll_number VARCHAR(20),
  stream ENUM('Computer Engg', 'Mech Engg', 'Comp Sci Engg', 'ECS'),
  division CHAR(1),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quizzes table
CREATE TABLE IF NOT EXISTS quizzes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  teacher_id INT NOT NULL,
  course_name VARCHAR(100),
  deadline TIMESTAMP,
  time_limit INT, -- in minutes
  target_stream VARCHAR(100), -- comma-separated streams or 'ALL'
  target_divisions VARCHAR(20), -- comma-separated divisions or 'ALL'
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Questions table
CREATE TABLE IF NOT EXISTS questions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  quiz_id INT NOT NULL,
  question_text TEXT NOT NULL,
  option_a VARCHAR(255) NOT NULL,
  option_b VARCHAR(255) NOT NULL,
  option_c VARCHAR(255) NOT NULL,
  option_d VARCHAR(255) NOT NULL,
  correct_option CHAR(1) NOT NULL,
  FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Results table
CREATE TABLE IF NOT EXISTS results (
  id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  quiz_id INT NOT NULL,
  score DOUBLE NOT NULL,
  total_questions INT NOT NULL,
  taken_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  answers TEXT,
  FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Seed sample users (demo only - plain text passwords)
INSERT INTO users (username, password, full_name, role) VALUES
('teacher1', 'pass123', 'Alice Teacher', 'TEACHER'),
('student1', 'pass123', 'Bob Student', 'STUDENT');
