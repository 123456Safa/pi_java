package utils;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * A JavaFX widget that hosts a WebView to execute Google reCAPTCHA v3
 * JavaScript and retrieve the generated token.
 *
 * Because reCAPTCHA validates the page's domain/origin, we cannot load the
 * HTML via file:// — Google will reject it with "Invalid domain for site key".
 * Instead, we spin up a tiny localhost HTTP server so the page is served from
 * http://localhost:<port>, which IS a valid domain for reCAPTCHA.
 */
public class RecaptchaWidget {

    private final WebView webView;
    private final WebEngine webEngine;
    private boolean isReady = false;
    private final JavaConnector javaConnector;
    private HttpServer localServer;

    // Callback to be executed when a token is received
    private Consumer<String> currentTokenCallback;
    private Consumer<String> currentErrorCallback;

    public RecaptchaWidget() {
        this.webView = new WebView();
        // Size it to fit the full reCAPTCHA badge when it expands on hover
        this.webView.setVisible(true);
        this.webView.setManaged(true);
        this.webView.setPrefSize(256, 74);
        this.webView.setMaxSize(256, 74);

        // Make the WebView itself have no background
        this.webView.setStyle("-fx-background-color: transparent;");

        this.webEngine = webView.getEngine();
        // Transparent page background so the white box is gone
        this.webEngine.setUserStyleSheetLocation(
            "data:text/css," +
            "body { background: transparent !important; }"
        );
        this.javaConnector = new JavaConnector();

        // Start a tiny localhost HTTP server to serve the recaptcha HTML
        int port = startLocalServer();

        // Load the page from localhost so Google sees a valid domain
        webEngine.load("http://localhost:" + port + "/recaptcha.html");

        // Inject the JavaConnector once the page loads successfully
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                // Use a delayed retry loop to ensure WebKit's native layer is fully
                // initialized before calling executeScript(). A single Platform.runLater
                // is not sufficient on JDK 26 where native access restrictions add
                // initialization latency to WebKit's internal WebPage object.
                final int MAX_RETRIES = 10;
                final long RETRY_DELAY_MS = 200;
                java.util.Timer retryTimer = new java.util.Timer("recaptcha-init", true);
                retryTimer.schedule(new java.util.TimerTask() {
                    int attempt = 0;
                    @Override
                    public void run() {
                        attempt++;
                        Platform.runLater(() -> {
                            try {
                                JSObject window = (JSObject) webEngine.executeScript("window");
                                window.setMember("javaConnector", javaConnector);
                                isReady = true;
                                retryTimer.cancel();
                                System.out.println("reCAPTCHA Widget is ready (attempt " + attempt + ", served from localhost:" + port + ").");
                            } catch (Exception e) {
                                if (attempt >= MAX_RETRIES) {
                                    retryTimer.cancel();
                                    System.err.println("Failed to inject JavaConnector after " + MAX_RETRIES + " attempts: " + e.getMessage());
                                }
                            }
                        });
                    }
                }, RETRY_DELAY_MS, RETRY_DELAY_MS);
            } else if (newValue == Worker.State.FAILED) {
                System.err.println("Failed to load reCAPTCHA HTML.");
            }
        });
    }

    /**
     * Starts a minimal HTTP server on a random available port.
     * It serves only the recaptcha.html file from the resources folder.
     *
     * @return the port the server is listening on.
     */
    private int startLocalServer() {
        try {
            // Port 0 = pick any available port automatically
            localServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

            localServer.createContext("/recaptcha.html", exchange -> {
                try (InputStream is = getClass().getResourceAsStream("/html/recaptcha.html")) {
                    if (is == null) {
                        String error = "recaptcha.html not found in resources";
                        exchange.sendResponseHeaders(404, error.length());
                        exchange.getResponseBody().write(error.getBytes());
                        exchange.getResponseBody().close();
                        return;
                    }

                    byte[] htmlBytes = is.readAllBytes();
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, htmlBytes.length);
                    exchange.getResponseBody().write(htmlBytes);
                    exchange.getResponseBody().close();
                }
            });

            localServer.setExecutor(null); // use default executor
            localServer.start();

            int port = localServer.getAddress().getPort();
            System.out.println("reCAPTCHA local server started on port " + port);
            return port;

        } catch (IOException e) {
            throw new RuntimeException("Failed to start local reCAPTCHA server: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the WebView node so it can be added to the scene graph.
     * The WebView must be attached to the scene for the JS engine to run correctly.
     */
    public WebView getWebView() {
        return webView;
    }

    /**
     * Requests a reCAPTCHA token asynchronously.
     *
     * @param actionName The action name (e.g., 'login', 'register').
     * @param onSuccess Callback invoked when the token is successfully generated.
     * @param onError Callback invoked if an error occurs during token generation.
     */
    public void requestToken(String actionName, Consumer<String> onSuccess, Consumer<String> onError) {
        if (!isReady || webEngine == null) {
            if (onError != null) {
                onError.accept("reCAPTCHA is not fully loaded yet. Please try again in a moment.");
            }
            return;
        }

        this.currentTokenCallback = onSuccess;
        this.currentErrorCallback = onError;

        // Execute the JS function defined in recaptcha.html
        Platform.runLater(() -> {
            try {
                webEngine.executeScript("executeRecaptcha('" + actionName + "');");
            } catch (Exception e) {
                if (currentErrorCallback != null) {
                    currentErrorCallback.accept("Error executing JS: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Stops the local HTTP server. Call this when the application shuts down.
     */
    public void shutdown() {
        if (localServer != null) {
            localServer.stop(0);
            System.out.println("reCAPTCHA local server stopped.");
        }
    }

    /**
     * The bridge class injected into the JavaScript context.
     * Methods must be public to be accessible from JS.
     */
    public class JavaConnector {
        public void onToken(String token) {
            if (currentTokenCallback != null) {
                Platform.runLater(() -> currentTokenCallback.accept(token));
            }
        }

        public void onError(String errorMsg) {
            if (currentErrorCallback != null) {
                Platform.runLater(() -> currentErrorCallback.accept(errorMsg));
            }
        }
    }
}
