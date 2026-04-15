#!/usr/bin/env powershell

# Set paths
$javaHome = "C:\Users\fatma\.jdks\openjdk-26"
$m2Repo = "C:\Users\fatma\.m2\repository"

# Build classpath
$classpath = @(
    "target\classes",
    "$m2Repo\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar",
    "$m2Repo\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar",
    "$m2Repo\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar",
    "$m2Repo\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar",
    "$m2Repo\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar",
    "$m2Repo\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar",
    "$m2Repo\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar"
) -join ";"

$modulePath = @(
    "$m2Repo\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar"
) -join ";"

# Launch application
& "$javaHome\bin\java.exe" `
    --module-path "$modulePath" `
    --add-modules javafx.controls,javafx.fxml `
    -cp "$classpath" `
    org.example.Main

