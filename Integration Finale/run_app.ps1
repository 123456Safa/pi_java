# Script PowerShell pour lancer l'application avec les paramètres JavaFX corrects

$JAVA_HOME = "C:\Users\fatma\.jdks\openjdk-26"
$JAVA_EXE = "$JAVA_HOME\bin\java.exe"
$PROJECT_DIR = "C:\Users\fatma\IdeaProjects\pidevjava"
$M2_REPO = "C:\Users\fatma\.m2\repository"

# Modules JavaFX (utiliser le séparateur Windows `;`)
$MODULE_PATH = @(
    "$M2_REPO\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar",
    "$M2_REPO\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar",
    "$M2_REPO\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar",
    "$M2_REPO\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar"
) -join ";"

# Classpath
$CLASSPATH = @(
    "$PROJECT_DIR\target\classes",
    "$M2_REPO\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar",
    "$M2_REPO\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar",
    "$M2_REPO\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar",
    "$M2_REPO\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar",
    "$M2_REPO\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar",
    "$M2_REPO\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar",
    "$M2_REPO\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar",
    "$M2_REPO\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar",
    "$M2_REPO\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar",
    "$M2_REPO\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar"
) -join ";"

# Lancer l'application
& "$JAVA_EXE" `
    --module-path "$MODULE_PATH" `
    --add-modules javafx.controls,javafx.fxml `
    --enable-native-access javafx.graphics `
    -Dfile.encoding=UTF-8 `
    -classpath "$CLASSPATH" `
    org.example.Main

