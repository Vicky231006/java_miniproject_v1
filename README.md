# Online Quiz Management System

This project is a simple JavaFX + JDBC (MySQL) application for managing quizzes.

Prerequisites
- Java JDK 17+ (you mentioned Java 25 installed at: C:\\Program Files\\Java\\jdk-25)
- JavaFX SDK (you mentioned: C:\\Program Files\\javafx-sdk-25)
- MySQL server with user `root` and password `2310`.
- MySQL Connector/J (JDBC) jar (download mysql-connector-java)

Setup
1. Import or place the project in a folder, e.g. `C:\Users\Vicky\Desktop\new_quiz_system`.
2. Create the database and tables by running the SQL in `sql/create_quizdb.sql` (use MySQL Workbench or the mysql CLI):

   mysql -u root -p < sql/create_quizdb.sql

   Enter password: 2310

Compile
1. Open Git Bash or another bash shell.
2. Set environment variables (adjust paths if different):

```bash
export JAVAFX_LIB="/c/Program Files/javafx-sdk-25/lib"
export MYSQL_JAR="/c/path/to/mysql-connector-java-8.0.xx.jar"
export JDK="/c/Program Files/Java/jdk-25/bin"
```

3. Compile all Java files into `out`:

```bash
mkdir -p out
javac --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml -cp "$MYSQL_JAR" -d out $(find src -name "*.java")
```

Run

```bash
java --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml -cp "out:$MYSQL_JAR" Main
```

Login credentials (seeded):
- Teacher: username `teacher1`, password `pass123` (role TEACHER)
- Student: username `student1`, password `pass123` (role STUDENT)

Notes
- Passwords are stored in plain text for demo simplicity. Do not use this in production.
- If you move the resources folder, update the FXML loading paths in `Main` and controllers.


# Update this path if connector jar is elsewhere
export MYSQL_JAR="C:\Users\Vicky\Desktop\QuizManagementSystem\lib\mysql-connector-j-8.0.33.jar"
export JAVAFX_LIB="C:\Program Files\javafx-sdk-25\lib"

# Prepare sources list for javac
find src -name "*.java" > sources.txt

# Compile into out/
mkdir -p out
javac --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml -cp "$MYSQL_JAR" -d out @sources.txt
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "out;%MYSQL_JAR%" Main