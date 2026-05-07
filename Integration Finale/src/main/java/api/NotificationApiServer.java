package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import models.Notification;
import services.NotificationService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Lightweight HTTP Server to expose Notification APIs
 * Endpoints:
 * - GET /api/notifications/user/{id}
 * - POST /api/notifications/send
 */
public class NotificationApiServer {

    private static final int PORT = 8081;
    private final NotificationService notificationService;

    public NotificationApiServer() {
        this.notificationService = new NotificationService();
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // GET /api/notifications/user/{id}
            server.createContext("/api/notifications/user/", new GetUserNotificationsHandler());

            // POST /api/notifications/send
            server.createContext("/api/notifications/send", new SendNotificationHandler());

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            System.out.println("🚀 Notification API Server démarré sur le port " + PORT);
        } catch (IOException e) {
            System.err.println("❌ Impossible de démarrer l'API Server : " + e.getMessage());
        }
    }

    private class GetUserNotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length > 4) {
                    try {
                        int userId = Integer.parseInt(parts[4]);
                        List<Notification> notifications = notificationService.getUserNotifications(userId);

                        // Simple JSON generation
                        StringBuilder json = new StringBuilder("[");
                        for (int i = 0; i < notifications.size(); i++) {
                            Notification n = notifications.get(i);
                            json.append(String.format("{\"id\":%d,\"title\":\"%s\",\"message\":\"%s\",\"type\":\"%s\",\"isRead\":%b}",
                                    n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.isRead()));
                            if (i < notifications.size() - 1) json.append(",");
                        }
                        json.append("]");

                        sendResponse(exchange, 200, json.toString());
                    } catch (NumberFormatException e) {
                        sendResponse(exchange, 400, "{\"error\": \"ID invalide\"}");
                    }
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"ID manquant\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    private class SendNotificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // In a real app, parse the JSON body. For simplicity we extract parameters or read raw string
                String requestBody = new String(exchange.getRequestBody().readAllBytes());

                // Extremely simple parsing for demonstration (assuming simple key=value or json format)
                // Real usage would require a JSON parser like Jackson or Gson
                try {
                    int userId = extractInt(requestBody, "userId");
                    String title = extractString(requestBody, "title");
                    String message = extractString(requestBody, "message");
                    String type = extractString(requestBody, "type");
                    String email = extractString(requestBody, "email");

                    notificationService.sendNotification(userId, title, message, type, email);

                    sendResponse(exchange, 201, "{\"success\": true, \"message\": \"Notification envoyée\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\": \"Données invalides\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    // Quick helpers for parsing JSON strings manually
    private int extractInt(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }

    private String extractString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
