package Services; // Defines the package for computer vision services

import org.bytedeco.javacv.*; // Imports JavaCV for camera and frame handling
import org.bytedeco.opencv.opencv_core.*; // Imports OpenCV core structures
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier; // Imports classifier for face detection
import utils.MyDatabase; // Imports database utility for storing embeddings
import com.google.gson.*; // Imports GSON for JSON communication with microservice
import org.apache.hc.client5.http.classic.methods.HttpPost; // Imports HTTP POST method for microservice calls
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient; // Imports HTTP client for API communication
import org.apache.hc.client5.http.impl.classic.HttpClients; // Factory for creating HTTP clients
import org.apache.hc.core5.http.io.entity.StringEntity; // For sending JSON strings in HTTP body
import org.apache.hc.core5.http.io.entity.EntityUtils; // Utility for reading HTTP response entities

import javax.imageio.ImageIO; // For converting BufferedImages to byte arrays
import java.awt.image.BufferedImage; // Standard Java image class
import java.io.ByteArrayOutputStream; // For encoding images to Base64
import java.net.URL; // For network URL handling
import java.sql.*; // Standard SQL interfaces
import java.util.Base64; // For encoding image bytes to string
import java.util.logging.Logger; // Standard logging utility
import org.bytedeco.javacv.OpenCVFrameGrabber; // Specifically for grabbing frames from webcam
import org.bytedeco.javacv.Java2DFrameConverter; // For converting OpenCV frames to Java images
import org.bytedeco.javacv.Frame; // Represents a single video frame

/**
 * FaceAuthService manages facial recognition logic.
 * It interfaces with a local Python microservice for heavy embedding extraction
 * and handles local webcam capture via JavaCV.
 */
public class FaceAuthService { // Main class for Face ID logic

    private static final Logger LOG = Logger.getLogger(FaceAuthService.class.getName()); // Logger for this class
    private static final String MICROSERVICE_URL = "http://127.0.0.1:8001"; // URL of the Python backend
    private static final Gson gson = new Gson(); // Reusable JSON instance
    private static final int DEFAULT_TIMEOUT_MS = 15000; // Increased timeout for robust processing

    public static class FaceVerificationResult {
        private final boolean match;
        private final String message;

        public FaceVerificationResult(boolean match, String message) {
            this.match = match;
            this.message = message;
        }

        public boolean isMatch() {
            return match;
        }

        public String getMessage() {
            return message;
        }
    }

    // --- Asynchronous API (Recommended for long run) ---

    /**
     * Enrolls a face asynchronously to avoid freezing the UI.
     */
    public java.util.concurrent.CompletableFuture<String> enrollFaceAsync(String email) { // Async wrapper
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> { // Runs task in background thread
            try { // Error handling block
                return enrollFace(email); // Calls the synchronous enrollment logic
            } catch (Exception e) { // Catches any capture or API errors
                LOG.severe("Async enrollment failed: " + e.getMessage()); // Logs error
                return null; // Returns failure
            } // End of try-catch
        }); // End of CompletableFuture
    } // End of enrollFaceAsync

    /**
     * Verifies a face asynchronously.
     */
    public java.util.concurrent.CompletableFuture<Boolean> verifyFaceAsync(String email) { // Async wrapper
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> { // Runs in background
            try { // Error handling
                return verifyFace(email); // Calls synchronous verification
            } catch (Exception e) { // Catches errors
                LOG.severe("Async verification failed: " + e.getMessage()); // Logs
                return false; // Returns failure
            } // End of try-catch
        }); // End of CompletableFuture
    } // End of verifyFaceAsync

    public java.util.concurrent.CompletableFuture<FaceVerificationResult> verifyFaceDetailedAsync(String email) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return verifyFaceDetailed(email);
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    message = "Face verification failed due to a service error.";
                } else {
                    message = "Face verification failed: " + message;
                }
                LOG.severe("Async verification failed: " + message);
                return new FaceVerificationResult(false, message);
            }
        });
    }

    // ─── Enregistrement du visage ─────────────────────────────────────────────

    /**
     * Synchronous method to capture a face and store its embedding.
     */
    public String enrollFace(String email) throws Exception { // Main enrollment logic
        if (email == null) return null;
        String normalizedEmail = email.trim().toLowerCase();

        String base64Image = captureAndEncode(); // Captures face from webcam and encodes to Base64
        if (base64Image == null) { // Checks if capture was successful
            LOG.warning("Capture failed for enrollment: " + normalizedEmail); // Logs failure
            return null; // Returns failure
        } // End of capture check

        // Extract facial embedding from Python microservice
        String embedding = extractEmbeddingFromMicroservice(base64Image); // Calls API to get feature vector
        if (embedding == null) { // Checks if API returned a valid result
            LOG.warning("Embedding extraction failed for: " + normalizedEmail); // Logs failure
            return null; // Returns failure
        } // End of embedding check

        // Store embedding in database
        Connection conn = MyDatabase.getInstance().getConnection(); // Gets DB connection
        try (PreparedStatement ps = conn.prepareStatement( // Prepares update statement
                "UPDATE user SET face_encoding = ?, face_auth_enabled = 1 WHERE email = ?")) { // SQL query
            ps.setString(1, embedding); // Binds the JSON embedding string
            ps.setString(2, normalizedEmail); // Binds the user email
            ps.executeUpdate(); // Saves to database
        } // End of try-with-resources
        LOG.info("Face enrolled successfully for " + normalizedEmail); // Logs success
        return embedding; // Returns the generated embedding
    } // End of enrollFace

    // ─── Vérification du visage ───────────────────────────────────────────────

    /**
     * Verifies if the person currently at the webcam matches the stored identity.
     */
    /**
     * Verifies if the person currently at the webcam matches the stored identity.
     * Uses the optimized single-endpoint API call.
     */
    public boolean verifyFace(String email) throws Exception { // Main verification logic
        FaceVerificationResult result = verifyFaceDetailed(email);
        return result.isMatch();
    } // End of verifyFace

    public FaceVerificationResult verifyFaceDetailed(String email) throws Exception {
        if (email == null) {
            LOG.warning("Verification failed: Email is null");
            return new FaceVerificationResult(false, "Email is missing.");
        }
        String normalizedEmail = email.trim().toLowerCase();

        // Retrieve stored embedding from database
        String storedEmbedding = getStoredEncoding(normalizedEmail);
        if (storedEmbedding == null) {
            LOG.severe("Verification failed: No stored embedding found for " + normalizedEmail + ". Ensure Face ID is correctly set up for this user.");
            return new FaceVerificationResult(false, "No enrolled face found for this account.");
        }

        // Capture current face
        LOG.info("Initializing camera capture for " + normalizedEmail);
        String base64Image = captureAndEncode();
        if (base64Image == null) {
            LOG.severe("Capture failed during verification for: " + normalizedEmail + ". Camera might be unavailable.");
            return new FaceVerificationResult(false, "Camera capture failed. Please check camera access.");
        }

        LOG.info("Sending capture to microservice for verification...");
        FaceVerificationResult result = verifyFaceWithMicroserviceDetailed(base64Image, storedEmbedding);
        LOG.info("Face verification for " + normalizedEmail + " result: " + result.isMatch());
        return result;
    }

    /**
     * Vérifie le visage en utilisant un grabber déjà ouvert (pour la prévisualisation).
     */
    public boolean verifyFaceFromGrabber(String email, OpenCVFrameGrabber grabber) throws Exception { // Optimized verification
        // Retrieve stored embedding from database
        String storedEmbedding = getStoredEncoding(email); // Fetches DB vector
        if (storedEmbedding == null) { // Check for setup
            LOG.warning("No stored embedding for grabber verification: " + email); // Log
            return false; // Fail
        } // End of check

        Java2DFrameConverter converter = new Java2DFrameConverter(); // For frame conversion
        // Capture 5 frames and use the best one
        String bestCapture = null; // Storage for best frame
        for (int i = 0; i < 5; i++) { // Loop to get stable frame
            Frame frame = grabber.grab(); // Grabs next frame from stream
            if (frame != null && frame.image != null) { // Validates frame
                BufferedImage img = converter.convert(frame); // Converts to Java Image
                if (img != null) { // Validates conversion
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(); // Stream for encoding
                    ImageIO.write(img, "jpg", baos); // Writes as JPG
                    bestCapture = Base64.getEncoder().encodeToString(baos.toByteArray()); // Encodes to B64
                } // End conversion check
            } // End frame check
            Thread.sleep(100); // Small delay between attempts
        } // End capture loop

        if (bestCapture == null) { // check if any capture worked
            LOG.warning("Failed to capture frames for grabber verification"); // Log
            return false; // Fail
        } // End capture check

        // Extract embedding from captured frame
        String currentEmbedding = extractEmbeddingFromMicroservice(bestCapture); // Gets current vector
        if (currentEmbedding == null) { // check for API error
            LOG.warning("Embedding extraction failed in grabber verification for: " + email); // Log
            return false; // Fail
        } // End extraction check

        // Compare embeddings using microservice
        boolean match = compareEmbeddingsWithMicroservice(storedEmbedding, currentEmbedding); // API comparison
        LOG.info("Grabber-based face verification result for " + email + ": " + match); // Log
        return match; // return result
    } // End verifyFaceFromGrabber

    // ─── Statut ───────────────────────────────────────────────────────────────

    /**
     * Checks if Face ID is enabled for a specific user.
     */
    public boolean isFaceAuthEnabled(String email) throws SQLException { // DB check
        if (email == null) return false;
        String normalizedEmail = email.trim().toLowerCase();
        
        Connection conn = MyDatabase.getInstance().getConnection(); // Gets DB
        try (PreparedStatement ps = conn.prepareStatement( // Prepares query
                "SELECT face_auth_enabled FROM user WHERE email = ?")) { // selects flag
            ps.setString(1, normalizedEmail); // binds email
            ResultSet rs = ps.executeQuery(); // executes
            return rs.next() && rs.getInt("face_auth_enabled") == 1; // returns true if 1
        } // End try-with-resources
    } // End isFaceAuthEnabled

    /**
     * Disables Face ID for a user.
     */
    public void disableFaceAuth(String email) throws SQLException { // Deletion method
        if (email == null) return;
        String normalizedEmail = email.trim().toLowerCase();
        
        Connection conn = MyDatabase.getInstance().getConnection(); // Gets DB
        try (PreparedStatement ps = conn.prepareStatement( // Prepares update
                "UPDATE user SET face_auth_enabled = 0, face_encoding = NULL WHERE email = ?")) { // Resets fields
            ps.setString(1, normalizedEmail); // Binds email
            ps.executeUpdate(); // Executes
        } // End try-with-resources
    } // End disableFaceAuth

    // ─── Capture webcam ───────────────────────────────────────────────────────

    /**
     * Internal method to open webcam, show preview, and capture a frame.
     */
    private String captureAndEncode() throws Exception { // Private capture utility
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0); // Initializes camera 0
        try { // Try to start camera
            grabber.start(); // Opens the camera device
        } catch (Exception e) { // Catches camera access errors
            LOG.severe("Could not start webcam: " + e.getMessage()); // Logs error
            return null; // Fails
        } // End try-catch

        // Creates a UI window for the camera feed
        double gamma = grabber.getGamma() > 0 ? grabber.getGamma() : 2.2; // Fallback if gamma is 0
        CanvasFrame canvas = new CanvasFrame("Face ID - Capture", CanvasFrame.getDefaultGamma() / gamma);
        canvas.setCanvasSize(640, 480); // Sets window dimensions
        canvas.setAlwaysOnTop(true); // Ensure it's visible
        
        Java2DFrameConverter converter = new Java2DFrameConverter(); // Frame converter instance
        String result = null; // Stores encoded image
        long startTime = System.currentTimeMillis(); // Track start time for timeout

        try { // Main capture loop
            Thread.sleep(200); // Wait for camera to warm up and canvas to map
            
            java.util.List<BufferedImage> frameCandidates = new java.util.ArrayList<>();
            
            while (canvas.isVisible() && (System.currentTimeMillis() - startTime) < DEFAULT_TIMEOUT_MS) {
                Frame frame = grabber.grab(); // Grabs raw frame from camera
                if (frame == null) break; // Exits if camera disconnects

                // Show the live feed
                canvas.showImage(frame); // Updates the UI window with the frame

                // Start collecting candidates after 1.5 seconds to allow exposure adjustment
                if (System.currentTimeMillis() - startTime > 1500 && frameCandidates.size() < 5) {
                    BufferedImage img = converter.convert(frame);
                    if (img != null) frameCandidates.add(img);
                    Thread.sleep(100); // Take shots every 100ms
                }

                // Once we have a burst of 5 shots, pick the best one (middle of the burst usually has best focus/exposure)
                if (frameCandidates.size() >= 5) {
                    BufferedImage bestImg = frameCandidates.get(2); // Use the middle frame
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bestImg, "jpg", baos);
                    result = Base64.getEncoder().encodeToString(baos.toByteArray());
                    break;
                }
            } // End while loop
        } finally { // Cleanup block
            canvas.dispose(); // Closes capture window
            grabber.stop(); // Stops camera
            grabber.release(); // Releases camera resources
        } // End finally

        return result; // Returns encoded image or null
    } // End captureAndEncode

    // ─── Database Methods ─────────────────────────────────────────────────────

    /**
     * Internal helper to fetch the stored vector for an email.
     */
    private String getStoredEncoding(String email) throws SQLException { // DB lookup
        if (email == null) return null;
        String normalizedEmail = email.trim().toLowerCase();
        
        Connection conn = MyDatabase.getInstance().getConnection(); // Gets DB
        try (PreparedStatement ps = conn.prepareStatement( // Prepares select
                "SELECT face_encoding FROM user WHERE email = ?")) { // Selects vector
            ps.setString(1, normalizedEmail); // binds email
            ResultSet rs = ps.executeQuery(); // executes
            return rs.next() ? rs.getString("face_encoding") : null; // returns vector or null
        } // End try-with-resources
    } // End getStoredEncoding

    // ─── Microservice Communication ───────────────────────────────────────────

    /**
     * Extracts facial embedding from the Python microservice
     * @param base64Image Base64 encoded JPEG image
     * @return JSON array of embeddings as string, or null if failed
     */
    private String extractEmbeddingFromMicroservice(String base64Image) { // External API call
        try { // Error handling
            CloseableHttpClient httpClient = HttpClients.createDefault(); // Creates HTTP client
            HttpPost request = new HttpPost(MICROSERVICE_URL + "/extract-embedding-base64"); // Sets endpoint
            request.setHeader("Content-Type", "application/json"); // Sets JSON header

            // Prepare request body
            JsonObject requestBody = new JsonObject(); // JSON container
            requestBody.addProperty("image_b64", base64Image); // Adds B64 image data

            request.setEntity(new StringEntity(gson.toJson(requestBody))); // Attaches JSON body to request

            var response = httpClient.execute(request, responseHandler -> { // Executes request
                String responseBody = EntityUtils.toString(responseHandler.getEntity()); // Reads response
                JsonObject result = JsonParser.parseString(responseBody).getAsJsonObject(); // Parses result

                if (result.has("accepted") && result.get("accepted").getAsBoolean()) { // Checks if face was detected
                    // Return embedding as JSON array string
                    JsonArray embedding = result.getAsJsonArray("embedding"); // Extracts vector
                    return embedding.toString(); // Returns vector as string
                } // End detection check
                LOG.warning("Microservice rejected image: " + 
                    (result.has("reason") ? result.get("reason").getAsString() : "unknown reason")); // Logs reason
                return null; // Fails
            }); // End execution callback

            httpClient.close(); // Closes client connection
            return response; // Returns vector string
        } catch (Exception e) { // Catches network/API errors
            LOG.severe("Error extracting embedding from microservice: " + e.getMessage()); // Logs
            e.printStackTrace(); // Prints stack trace
            return null; // Fails
        } // End try-catch
    } // End extractEmbeddingFromMicroservice

    /**
     * Compares two facial embeddings using the Python microservice
     * @param embedding1 First embedding as JSON array string
     * @param embedding2 Second embedding as JSON array string
     * @return true if faces match, false otherwise
     */
    private boolean compareEmbeddingsWithMicroservice(String embedding1, String embedding2) { // Comparison API call
        try { // Error handling
            CloseableHttpClient httpClient = HttpClients.createDefault(); // Creates HTTP client
            HttpPost request = new HttpPost(MICROSERVICE_URL + "/compare-embeddings"); // Sets comparison endpoint
            request.setHeader("Content-Type", "application/json"); // Sets JSON header

            // Prepare request body
            JsonObject requestBody = new JsonObject(); // JSON container
            requestBody.add("embedding1", JsonParser.parseString(embedding1).getAsJsonArray()); // Attaches stored vector
            requestBody.add("embedding2", JsonParser.parseString(embedding2).getAsJsonArray()); // Attaches current vector
            requestBody.addProperty("tolerance", 0.55); // Sets match threshold (lower is stricter)

            request.setEntity(new StringEntity(gson.toJson(requestBody))); // Attaches body

            var response = httpClient.execute(request, responseHandler -> { // Executes call
                String responseBody = EntityUtils.toString(responseHandler.getEntity()); // Reads result
                JsonObject result = JsonParser.parseString(responseBody).getAsJsonObject(); // Parses result

                if (result.has("match")) { // Checks for match boolean
                    boolean match = result.get("match").getAsBoolean(); // Extracts match status
                    double similarity = result.get("similarity").getAsDouble(); // Extracts raw score
                    LOG.info("Comparison result - Match: " + match + ", Similarity: " + 
                        String.format("%.4f", similarity)); // Logs detailed result
                    return match; // Returns result
                } // End match check
                return false; // Fails if result is missing
            }); // End execution callback

            httpClient.close(); // Closes connection
            return response; // Returns boolean match
        } catch (Exception e) { // Catches errors
            LOG.severe("Error comparing embeddings with microservice: " + e.getMessage()); // Logs
            e.printStackTrace(); // Trace
            return false; // Fail
        } // End try-catch
    } // End compareEmbeddingsWithMicroservice
    /**
     * Optimized single-call verification that handles both extraction and comparison in the microservice.
     */
    private boolean verifyFaceWithMicroservice(String base64Image, String storedEmbedding) {
        return verifyFaceWithMicroserviceDetailed(base64Image, storedEmbedding).isMatch();
    }

    private FaceVerificationResult verifyFaceWithMicroserviceDetailed(String base64Image, String storedEmbedding) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(MICROSERVICE_URL + "/verify-face");
            request.setHeader("Content-Type", "application/json");

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("image", base64Image);
            requestBody.add("stored_embedding", JsonParser.parseString(storedEmbedding).getAsJsonArray());

            request.setEntity(new StringEntity(gson.toJson(requestBody)));

            var response = httpClient.execute(request, responseHandler -> {
                String responseBody = EntityUtils.toString(responseHandler.getEntity());
                JsonObject result = JsonParser.parseString(responseBody).getAsJsonObject();

                if (result.has("match")) {
                    boolean match = result.get("match").getAsBoolean();
                    double score = result.has("score") ? result.get("score").getAsDouble() : 0.0;
                    String message = null;
                    if (result.has("message")) {
                        message = result.get("message").getAsString();
                    } else if (result.has("reason")) {
                        message = result.get("reason").getAsString();
                    } else if (result.has("error")) {
                        message = result.get("error").getAsString();
                    }
                    if (message == null || message.trim().isEmpty()) {
                        message = match ? "Face verified." : "Face not recognized.";
                    }
                    LOG.info("Verified face - Match: " + match + ", Score: " + String.format("%.4f", score));
                    return new FaceVerificationResult(match, message);
                }

                String message = null;
                if (result.has("error")) {
                    message = result.get("error").getAsString();
                } else if (result.has("reason")) {
                    message = result.get("reason").getAsString();
                }
                if (message == null || message.trim().isEmpty()) {
                    message = "Invalid response from face service.";
                }
                return new FaceVerificationResult(false, message);
            });

            httpClient.close();
            return response;
        } catch (Exception e) {
            LOG.severe("Error in optimized verify-face: " + e.getMessage());
            String message = e.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = "Face verification failed due to a service error.";
            } else {
                message = "Face verification failed: " + message;
            }
            return new FaceVerificationResult(false, message);
        }
    }
} // End of FaceAuthService class
