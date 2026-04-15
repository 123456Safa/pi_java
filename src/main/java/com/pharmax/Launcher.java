package com.pharmax;

/**
 * Launcher — Workaround for "JavaFX runtime components are missing" error.
 * 
 * When running a JavaFX app without module-info.java from IntelliJ,
 * the main class CANNOT extend Application directly.
 * This launcher class calls MainApp.main() indirectly, bypassing the check.
 * 
 * Run THIS class instead of MainApp.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
