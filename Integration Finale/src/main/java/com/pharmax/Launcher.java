package com.pharmax;

/**
 * Launcher — Workaround for "JavaFX runtime components are missing" error.
 * 
 * When running a JavaFX app without module-info.java from IntelliJ,
 * the main class CANNOT extend Application directly.
 * This launcher class calls the full application main() indirectly, bypassing the check.
 * 
 * Updated to use the full application with all management features instead of just blog management.
 */
public class Launcher {
    public static void main(String[] args) {
        org.example.Main.main(args);
    }
}
