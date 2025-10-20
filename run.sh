#!/usr/bin/env bash
# Adjust these paths if needed
JAVAFX_LIB="/c/Program Files/javafx-sdk-25/lib"
MYSQL_JAR="/c/Users/Vicky/Desktop/QuizManagementSystem/lib/mysql-connector-j-8.0.33.jar"

java --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp "$(pwd)/out:$MYSQL_JAR" Main
