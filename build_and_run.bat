@echo off
REM build_and_run.bat - compiles the project and runs the JavaFX application
REM Edit the paths below if your installations are in different locations

set "JAVAFX_LIB=C:\Program Files\javafx-sdk-25\lib"
set "MYSQL_JAR=C:/Users/Vicky/Desktop/new_quiz_system/mysql-connector-j-8.0.33/mysql-connector-j-8.0.33.jar"
set "JDK_BIN=C:\Program Files\Java\jdk-25\bin"
set "JAVAC=%JDK_BIN%\javac.exe"
set "JAVA=%JDK_BIN%\java.exe"

echo ==================================================
echo Building Online Quiz Management System
echo Java: %JAVA%
echo JavaFX lib: %JAVAFX_LIB%
echo MySQL connector: %MYSQL_JAR%
echo ==================================================

REM prepare sources list
if exist sources.txt del sources.txt
for /R "%~dp0src" %%f in (*.java) do @echo %%f>>sources.txt

if not exist "%JAVAC%" (
  echo ERROR: javac not found at %JAVAC%
  echo Please update JDK_BIN in this script to point to your JDK bin folder.
  pause
  exit /b 1
)

if not exist "%MYSQL_JAR%" (
  echo ERROR: MySQL connector JAR not found at %MYSQL_JAR%
  echo Please download mysql-connector-java and update MYSQL_JAR in this script.
  pause
  exit /b 1
)

if not exist "%JAVAFX_LIB%" (
  echo ERROR: JavaFX lib folder not found at %JAVAFX_LIB%
  echo Please update JAVAFX_LIB in this script to point to your JavaFX SDK lib folder.
  pause
  exit /b 1
)

mkdir out 2>nul

echo Compiling sources...
"%JAVAC%" --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "%MYSQL_JAR%" -d out @sources.txt
if errorlevel 1 (
  echo Compilation failed. See errors above.
  pause
  exit /b 1
)

echo Compilation succeeded.
echo Launching application...

"%JAVA%" --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp "out;%MYSQL_JAR%" Main
set RC=%ERRORLEVEL%
echo Application exited with code %RC%
pause
exit /b %RC%
