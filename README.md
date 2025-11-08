# Quiz Management System

A comprehensive JavaFX-based quiz system that allows teachers to create and manage quizzes, and students to take them with features like timed submissions and automatic grading.

## 📋 Features

- **User Authentication**: Separate teacher and student logins
- **Teacher Features**:
  - Create quizzes with multiple-choice questions
  - Set time limits and deadlines
  - Target specific streams/divisions
  - View quiz results and statistics
- **Student Features**:
  - Take quizzes with real-time countdown timer
  - Auto-submit when time expires
  - View scores immediately
  - Track progress across multiple quizzes
- **Smart Features**:
  - Automatic grading
  - Stream/division-based access control
  - Deadline enforcement
  - Real-time countdown timer with warnings

## 🚀 Quick Start Guide

### Prerequisites

1. Java Development Kit (JDK) 17 or higher
2. MySQL Server 8.0 or higher
3. MySQL Connector/J (included in lib folder)
4. JavaFX SDK (included in lib folder)

### Database Setup

1. Install MySQL and start the MySQL server
2. Create the database and tables:
```bash
# Log into MySQL
mysql -u root -p

# Create database
CREATE DATABASE quizdb;

# Import schema
mysql -u root -p quizdb < create_quizdb.sql
```

### Application Setup

1. Clone/download the project
2. Navigate to the project directory
3. Run the application:
```bash
# Windows
./build_and_run.bat

# Linux/Mac
./build_and_run.sh
```

## 🏗️ Project Structure & Architecture

### Database Layer (`src/database/`)
- **DatabaseConnection.java**
  - Manages database connections
  - Uses connection pooling for efficiency
  - Called by all DAO classes

### Data Access Objects (`src/dao/`)
- **QuizDAO.java**
  - Handles quiz CRUD operations
  - Manages quiz metadata (deadlines, time limits)
  - Called by TeacherDashboardController and StudentDashboardController
- **QuestionDAO.java**
  - Manages quiz questions
  - Stores and retrieves question options
  - Used during quiz creation and attempts
- **ResultDAO.java**
  - Records quiz attempts and scores
  - Tracks student performance
  - Called after quiz submission
- **UserDAO.java**
  - Manages user authentication
  - Stores user profiles and roles
  - Used during login and registration

### Models (`src/models/`)
- **Quiz.java**
  - Core quiz properties (title, description)
  - Time limit and deadline handling
  - Stream/division targeting logic
- **Question.java**
  - Question text and options
  - Correct answer validation
  - Used in quiz creation/attempts
- **User.java**
  - User profile information
  - Role-based access control
  - Student/Teacher differentiation
- **Result.java**
  - Quiz attempt records
  - Score calculation
  - Answer tracking

### User Interface (`src/ui/`)
- **LoginController.java**
  - Entry point for authentication
  - Role-based navigation
  - Called when app starts
- **TeacherDashboardController.java**
  - Quiz management interface
  - Results viewing
  - Main teacher workspace
- **StudentDashboardController.java**
  - Quiz listing and access
  - Progress tracking
  - Main student workspace
- **QuizCreationController.java**
  - Quiz builder interface
  - Question management
  - Called from teacher dashboard
- **QuizAttemptController.java**
  - Quiz taking interface
  - Timer management
  - Auto-submission logic
- **ResultsController.java**
  - Score display
  - Answer review
  - Called after quiz submission

### Resources (`resources/`)
- **FXML Files**
  - UI layout definitions
  - Style configurations
  - Screen transitions
- **CSS Files**
  - Theme definitions
  - Visual styling
  - Component appearances

## 🔄 Program Flow

1. **Login Process**:
   - `LoginController` authenticates via `UserDAO`
   - Redirects to appropriate dashboard based on role

2. **Teacher Flow**:
   ```
   TeacherDashboard → QuizCreation → Questions Added → Quiz Published
                   ↳ ViewResults → Student Performance Analysis
   ```

3. **Student Flow**:
   ```
   StudentDashboard → Quiz Selection → QuizAttempt → Timer Starts
                   → Auto/Manual Submit → Results Display
   ```

4. **Quiz Creation**:
   - Teacher creates quiz (QuizCreationController)
   - Adds questions (QuestionDAO)
   - Sets parameters (time limit, deadline)
   - Quiz saved to database (QuizDAO)

5. **Quiz Taking**:
   - Student starts quiz (QuizAttemptController)
   - Timer initializes if time limit set
   - Questions loaded from QuestionDAO
   - Answers saved via ResultDAO
   - Score calculated and displayed

## Default Login Credentials
- **Teacher**: username `teacher1`, password `pass123`
- **Student**: username `student1`, password `pass123`

## 💡 Tips & Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check MySQL is running
   - Verify credentials in DatabaseConnection.java
   - Ensure database 'quizdb' exists

2. **JavaFX Not Found**
   - Verify JavaFX SDK in lib folder
   - Check module path in build script

3. **Timer Not Showing**
   - Ensure time_limit is set when creating quiz
   - Check QuizAttempt.fxml has timerLabel defined

### Best Practices

1. **For Teachers**:
   - Set reasonable time limits
   - Provide clear instructions
   - Test quiz before publishing

2. **For Students**:
   - Check deadline before starting
   - Monitor timer during attempt
   - Save answers regularly

## 🔒 Security Features

- Passwords are securely hashed
- Role-based access control
- Session management
- SQL injection prevention
- Cross-stream visibility control

## 📚 Development Guide

### Adding New Features

1. Create/modify model class
2. Update corresponding DAO
3. Add UI controller if needed
4. Create FXML layout
5. Wire up in existing flows

### Database Modifications

1. Back up existing data
2. Update create_quizdb.sql
3. Run migration scripts
4. Update affected DAO classes

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create pull request

## 📧 Support

For issues or questions:
1. Check troubleshooting guide
2. Review database logs
3. Contact system administrator