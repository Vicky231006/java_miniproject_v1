@echo off
REM Adjust paths below if your installation paths differ
set "JAVAFX_LIB=C:\Program Files\javafx-sdk-25\lib"
set "MYSQL_JAR=C:\Users\Vicky\Desktop\QuizManagementSystem\lib\mysql-connector-j-8.0.33.jar"
set "JAVA_EXE=C:\Program Files\Java\jdk-25\bin\java.exe"

"%JAVA_EXE%" --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp "out;%MYSQL_JAR%" Main
