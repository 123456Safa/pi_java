@echo off
setlocal enabledelayedexpansion

REM Set Java home
set JAVA_HOME=C:\Users\fatma\.jdks\openjdk-26

REM Set classpath
set CLASSPATH=target\classes
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar
set CLASSPATH=!CLASSPATH!;C:\Users\fatma\.m2\repository\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar

REM Launch application
"%JAVA_HOME%\bin\java.exe" --module-path "C:\Users\fatma\.m2\repository\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar" --add-modules javafx.controls,javafx.fxml -cp "!CLASSPATH!" org.example.Main

pause

