# PharmaX Project — Error Tracker

This file tracks major compilation, integration, and runtime errors encountered during the PharmaX development and integration process.

## [2026-05-06] Integration Phase — Major Compilation Fixes

### 1. Case-Insensitive Filesystem Collision (Windows)
- **Error:** `MyDataBase.java:7 — class MyDatabase is public, should be declared in a file named MyDatabase.java`
- **Cause:** Windows treats `MyDataBase.java` and `MyDatabase.java` as the same file. Deleting or renaming one affected the other.
- **Solution:** Unified everything into `MyDatabase.java` and ensured the class name matched the filename perfectly.

### 2. Package Casing & Interface Mismatch
- **Error:** `IService.java:6 — interface Iservice is public, should be declared in a file named Iservice.java`
- **Cause:** Collision between `package Services` (User module) and `package services` (Blog module). The Blog services used English method names (`add`, `update`, `delete`, `select`), while the User module used French (`ajouter`, `supprimer`, `modifier`, `recuperer`).
- **Solution:** 
    - Rewrote `services.IService` with English signatures for the Blog module.
    - Removed `implements Iservice` from `ServiceUser` to avoid signature conflicts on the case-insensitive filesystem.

### 3. Missing JavaFX Imports in Unified Main
- **Error:** `cannot find symbol: class StackPane / Button / VBox`
- **Cause:** When merging the entry points into `org.example.Main`, standard JavaFX layout and control imports were missing.
- **Solution:** Added `javafx.scene.control.Button`, `javafx.scene.layout.StackPane`, and `javafx.scene.layout.VBox` imports.

### 4. Controller Cross-Module API Mismatch
- **Error:** `HistoriqueController.java:559 — cannot access Iservice`
- **Cause:** `HistoriqueController` (Blog) was trying to call `.select()` on `ServiceUser` (User module), but `ServiceUser` used `.recuperer()`.
- **Solution:** Updated `HistoriqueController` to use `recuperer()` and fixed package reference to `Models.User`.

### 5. Email Service API Missing Methods
- **Error:** `cannot find symbol: method sendReclamationResolvedEmail / sendConfirmationAsync`
- **Cause:** The `EmailService` from the User module lacked the specific notification methods required by the Blog's Reclamation and Order controllers.
- **Solution:** Integrated both methods into `services.EmailService` with full HTML templates.

### 6. Database Connection Strings
- **Task:** Synchronize connection strings for unified DB.
- **Status:** Updated `MyDatabase.java` and `MyConnection.java` to use `jdbc:mysql://localhost:3306/pharmaxjava`.

### 7. Bad Class File: Package Case Mismatch (EmailService)
- **Error:** `UserController.java:6:16 — cannot access Services.EmailService. bad class file: .../services/EmailService.class`
- **Cause:** `EmailService` was moved to the lowercase `services` package. However, `UserController` was still importing `Services.EmailService` (capital S). The Java compiler enforces package casing when checking class files, causing a "wrong class" error.
- **Solution:** Updated the import in `UserController` to `import services.EmailService;`.

### 8. Missing Class & Ambiguous Package Reference (HistoriqueController)
- **Error:** `HistoriqueController.java:560:21 — cannot access User. class file for User not found`
- **Cause:** `HistoriqueController` used `Models.User::getFirstName` but didn't import `Models.User`. Additionally, it instantiated `services.ServiceUser` (lowercase), but `ServiceUser` is located in the `Services` package.
- **Solution:** Added explicit imports for `Models.User` and `Services.ServiceUser`, and cleaned up the `utilisateurService` instantiation to avoid ambiguous package paths.

### 9. JVM Native Crash — EXCEPTION_ACCESS_VIOLATION in WebKit (RecaptchaWidget)
- **Error:** `EXCEPTION_ACCESS_VIOLATION (0xc0000005) at pc=...jvm.dll+0x51180e` — Full JVM crash, producing `hs_err_pid*.log`
- **Crash Stack:**
  ```
  com.sun.webkit.WebPage.twkExecuteScript(...)
  javafx.scene.web.WebEngine.executeScript(...)
  utils.RecaptchaWidget.lambda$new$0(...)
  ```
  `siginfo: reading address 0x0000000000000000` (null pointer dereference in native code)
- **Cause:** `RecaptchaWidget` called `webEngine.executeScript("window")` **synchronously inside** the `Worker.State.SUCCEEDED` listener callback. At the exact moment the state transitions to SUCCEEDED, WebKit's native `WebPage` object has not finished its internal initialization. The `twkExecuteScript` native method attempts to dereference a null internal pointer, causing an unrecoverable `EXCEPTION_ACCESS_VIOLATION` (segfault). This is a well-known JavaFX WebView timing bug — the WebEngine reports SUCCEEDED before its native layer is fully ready for script execution.
- **Solution (Updated for JDK 26):** Initially, wrapping the `executeScript("window")` + `setMember()` call inside a single `Platform.runLater(...)` worked on earlier JDKs. However, on **JDK 26 + JavaFX 20**, module restrictions and native access policies introduce additional initialization latency, making a single event-loop tick insufficient. The fix was upgraded to use a `java.util.Timer` polling loop that attempts initialization every 200ms (up to 10 times) until the native layer is confirmed ready, preventing the null dereference.

### 10. "Unresolved compilation problem" at Runtime (Main.java)
- **Error:** `java.lang.Error: Unresolved compilation problem: at org.example.Main.main`
- **Cause:** IntelliJ IDEA's Eclipse Compiler (ECJ) fails to properly link `.class` files if the defined `package` name casing does not precisely match the directory casing, even though Windows is case-insensitive. While moving files from the User module, `User.java` retained `package Models;`, but was placed in the lowercase `models/` directory. The same occurred with `Services` and `Controllers`. Because of this, the compiler failed to resolve imports like `Models.User` inside `Main.java`, but allowed the application to start by replacing the `Main` class body with a stub that throws an `Error` at runtime.
- **Solution:** Ran a workspace-wide search-and-replace script to convert all `Models`, `Controllers`, and `Services` package declarations, imports, and inline instantiations to their strict lowercase forms (`models`, `controllers`, `services`) across all 30+ Java files, perfectly aligning them with their physical directories.

### 11. CSS Parsing Errors and ClassCastException (JDK 26 Compatibility)
- **Error:** `WARNING: CSS Error parsing... Unexpected token '.'` and `WARNING: Caught 'java.lang.ClassCastException: class java.lang.Double cannot be cast to class javafx.css.Size'`
- **Cause:** Two separate issues surfaced on JDK 26: 
  1. **Invalid Inheritance Syntax:** Using `-fx-inherit: .some-class` is not standard JavaFX CSS and was throwing unexpected token warnings.
  2. **Variable Resolution in Radius Properties:** JavaFX's CSS parser (specifically under JDK 26) throws a `ClassCastException` when a CSS custom property (e.g., `-fx-border-radius-lg`) is used as a value for `-fx-background-radius` or `-fx-border-radius`, resolving it as a `Double` instead of the expected `Size`.
- **Solution:** Removed the invalid `-fx-inherit` declarations and replaced the custom property references in radius fields with literal numeric values (e.g., `16`).

### 11. CSS ClassCastException on JDK 26
- **Error:** java.lang.Double cannot be cast to javafx.css.Size for -fx-background-radius
- **Cause:** On OpenJDK 26, the CSS parser fails to cast custom property variables (like -fx-border-radius-lg) to Size when used in radius properties. It resolves them as Double instead.
- **Solution:** Replaced variable references with literal values (e.g., 16) for all radius and border-radius properties in user-module.css. Also removed the non-standard -fx-inherit syntax which caused parsing warnings.
- **Status:** ? RESOLVED
