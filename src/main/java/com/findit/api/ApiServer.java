package com.findit.api;

import com.findit.config.DatabaseConfig;
import com.findit.dao.UserDAO;
import com.findit.model.*;
import com.findit.service.*;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP REST API Server for FindIT.
 * Runs on port 8080.
 *
 * Provides REST endpoints for:
 *   - Authentication (/api/auth/*)
 *   - Lost & Found Items (/api/items/*)
 *   - Heuristic Matching (/api/matches/*)
 *   - Ownership Claims (/api/claims/*)
 *   - Live Stats Dashboard (/api/dashboard/stats)
 *
 * Also serves the web frontend (HTML/CSS/JS) directly from the "web" directory!
 */
public class ApiServer {

    private static final int PORT = 8080;
    private static final String WEB_DIR = "web";

    private final AuthService      authService      = new AuthService();
    private final ItemService      itemService      = new ItemService();
    private final ClaimService     claimService     = new ClaimService();
    private final MatchingService  matchingService  = new MatchingService();
    private final DashboardService dashboardService = new DashboardService();
    private final UserDAO          userDAO          = new UserDAO();

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Endpoint handler
        server.createContext("/api", new ApiHandler());

        // Static Web files handler
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(12));
        server.start();

        System.out.println("=================================================");
        System.out.println("  FindIT Web Application & REST API Server Started!");
        System.out.println("  URL: http://localhost:" + PORT);
        System.out.println("  API: http://localhost:" + PORT + "/api");
        System.out.println("=================================================");
    }

    // ── Helper: CORS and JSON Response ───────────────────────────────────────

    private static void sendCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        sendCors(exchange);
        String json = GsonUtils.GSON.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("error", message);
        sendJsonResponse(exchange, statusCode, err);
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) return params;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length > 0) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String val = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                params.put(key, val);
            }
        }
        return params;
    }

    // ── Static Web Content Handler ──────────────────────────────────────────

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCors(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isBlank()) {
                path = "/index.html";
            }

            String baseDir = Files.exists(Paths.get("frontend", "dist", "index.html")) ? "frontend/dist" : "web";
            Path filePath = Paths.get(baseDir, path.startsWith("/") ? path.substring(1) : path);
            File file = filePath.toFile();

            if (!file.exists() || file.isDirectory()) {
                // If not found, serve SPA index.html
                file = Paths.get(baseDir, "index.html").toFile();
            }

            if (!file.exists()) {
                String notFound = "<h1>404 Not Found</h1><p>Web file not found: " + path + "</p>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String contentType = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String getContentType(String filename) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css"))  return "text/css; charset=UTF-8";
            if (lower.endsWith(".js"))   return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".png"))  return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".svg"))  return "image/svg+xml";
            if (lower.endsWith(".ico"))  return "image/x-icon";
            return "text/plain; charset=UTF-8";
        }
    }

    // ── REST API Handler ────────────────────────────────────────────────────

    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendCors(exchange);

            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> qParams = parseQueryParams(query);

            try {
                // ── Auth Endpoints ──────────────────────────────────────────
                if (path.equals("/api/auth/login") && "POST".equals(method)) {
                    handleLogin(exchange);
                } else if (path.equals("/api/auth/register") && "POST".equals(method)) {
                    handleRegister(exchange);
                } else if (path.equals("/api/auth/users") && "GET".equals(method)) {
                    handleGetUsers(exchange);

                // ── Items Endpoints ─────────────────────────────────────────
                } else if (path.equals("/api/items") && "GET".equals(method)) {
                    handleGetItems(exchange, qParams);
                } else if (path.equals("/api/items") && "POST".equals(method)) {
                    handleCreateItem(exchange);
                } else if (path.equals("/api/items/my") && "GET".equals(method)) {
                    handleGetMyItems(exchange, qParams);
                } else if (path.startsWith("/api/items/") && "GET".equals(method)) {
                    handleGetItemById(exchange, path);

                // ── Matches Endpoints ───────────────────────────────────────
                } else if (path.equals("/api/matches") && "GET".equals(method)) {
                    handleGetMatches(exchange, qParams);
                } else if (path.equals("/api/matches/recalculate") && "POST".equals(method)) {
                    matchingService.runFullPass();
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("message", "Smart matching recalculation triggered across all items.");
                    sendJsonResponse(exchange, 200, resp);

                // ── Claims Endpoints ────────────────────────────────────────
                } else if (path.equals("/api/claims") && "GET".equals(method)) {
                    handleGetClaims(exchange, qParams);
                } else if (path.equals("/api/claims") && "POST".equals(method)) {
                    handleSubmitClaim(exchange);
                } else if (path.equals("/api/claims/review") && "POST".equals(method)) {
                    handleReviewClaim(exchange);

                // ── Dashboard Stats ─────────────────────────────────────────
                } else if (path.equals("/api/dashboard/stats") && "GET".equals(method)) {
                    Map<String, Integer> stats = dashboardService.getAdminStats();
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("stats", stats);
                    sendJsonResponse(exchange, 200, resp);

                } else {
                    sendError(exchange, 404, "Unknown API endpoint: " + method + " " + path);
                }
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    // ── Endpoint Implementations ────────────────────────────────────────────

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        JsonObject json = GsonUtils.GSON.fromJson(body, JsonObject.class);
        if (json == null || !json.has("email") || !json.has("password")) {
            sendError(exchange, 400, "Email and password are required.");
            return;
        }

        String email = json.get("email").getAsString();
        String password = json.get("password").getAsString();

        User user = authService.login(email, password);
        Map<String, Object> sanitizedUser = sanitizeUser(user);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("user", sanitizedUser);
        sendJsonResponse(exchange, 200, resp);
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        JsonObject json = GsonUtils.GSON.fromJson(body, JsonObject.class);
        if (json == null) {
            sendError(exchange, 400, "Invalid JSON payload.");
            return;
        }

        String fullName = json.has("fullName") ? json.get("fullName").getAsString() : "";
        String email = json.has("email") ? json.get("email").getAsString() : "";
        String password = json.has("password") ? json.get("password").getAsString() : "";
        String confirmPassword = json.has("confirmPassword") ? json.get("confirmPassword").getAsString() : password;

        User user = authService.register(fullName, email, password, confirmPassword);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("user", sanitizeUser(user));
        sendJsonResponse(exchange, 201, resp);
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        try {
            List<User> users = userDAO.findAll();
            List<Map<String, Object>> sanitized = new ArrayList<>();
            for (User u : users) sanitized.add(sanitizeUser(u));
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("users", sanitized);
            sendJsonResponse(exchange, 200, resp);
        } catch (Exception e) {
            sendError(exchange, 500, "Could not load users: " + e.getMessage());
        }
    }

    private void handleGetItems(HttpExchange exchange, Map<String, String> q) throws IOException {
        String type     = q.getOrDefault("type", null);
        String search   = q.getOrDefault("search", null);
        String category = q.getOrDefault("category", null);
        String status   = q.getOrDefault("status", null);

        if ("ALL".equalsIgnoreCase(type)) type = null;
        if ("ALL".equalsIgnoreCase(category)) category = null;

        List<Item> items = itemService.getAll(type, search, category, status);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("items", items);
        sendJsonResponse(exchange, 200, resp);
    }

    private void handleCreateItem(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        JsonObject json = GsonUtils.GSON.fromJson(body, JsonObject.class);
        if (json == null) {
            sendError(exchange, 400, "Invalid JSON payload.");
            return;
        }

        int userId = json.has("userId") ? json.get("userId").getAsInt() : 1;
        String title = json.get("title").getAsString();
        String category = json.get("category").getAsString();
        String color = json.has("color") && !json.get("color").isJsonNull() ? json.get("color").getAsString() : null;
        String brand = json.has("brand") && !json.get("brand").isJsonNull() ? json.get("brand").getAsString() : null;
        String description = json.has("description") && !json.get("description").isJsonNull() ? json.get("description").getAsString() : "";
        String location = json.get("location").getAsString();
        String eventDateStr = json.has("eventDate") ? json.get("eventDate").getAsString() : LocalDate.now().toString();
        LocalDate eventDate = LocalDate.parse(eventDateStr);

        String typeStr = json.get("reportType").getAsString();
        Item.ReportType type = Item.ReportType.valueOf(typeStr.toUpperCase());

        Item created = itemService.reportItem(userId, title, category, color, brand, description, location, eventDate, type);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("item", created);
        sendJsonResponse(exchange, 201, resp);
    }

    private void handleGetMyItems(HttpExchange exchange, Map<String, String> q) throws IOException {
        String uidStr = q.get("userId");
        if (uidStr == null) {
            sendError(exchange, 400, "userId parameter required.");
            return;
        }
        int userId = Integer.parseInt(uidStr);
        List<Item> items = itemService.getMyReports(userId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("items", items);
        sendJsonResponse(exchange, 200, resp);
    }

    private void handleGetItemById(HttpExchange exchange, String path) throws IOException {
        String idStr = path.substring("/api/items/".length());
        int id = Integer.parseInt(idStr);
        Optional<Item> item = itemService.getById(id);
        if (item.isEmpty()) {
            sendError(exchange, 404, "Item not found with id: " + id);
            return;
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("item", item.get());
        sendJsonResponse(exchange, 200, resp);
    }

    private void handleGetMatches(HttpExchange exchange, Map<String, String> q) throws IOException {
        String uidStr = q.get("userId");
        boolean topOnly = Boolean.parseBoolean(q.getOrDefault("top", "false"));

        List<MatchSuggestion> matches;
        if (topOnly || uidStr == null) {
            matches = matchingService.getTopMatches(15);
        } else {
            int userId = Integer.parseInt(uidStr);
            matches = matchingService.getMatchesForUser(userId);
            if (matches.isEmpty()) {
                matches = matchingService.getTopMatches(10);
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("matches", matches);
        sendJsonResponse(exchange, 200, resp);
    }

    private void handleGetClaims(HttpExchange exchange, Map<String, String> q) throws IOException {
        String uidStr = q.get("userId");
        boolean pendingOnly = Boolean.parseBoolean(q.getOrDefault("pending", "false"));

        List<Claim> claims;
        if (uidStr != null) {
            claims = claimService.getMyClaims(Integer.parseInt(uidStr));
        } else if (pendingOnly) {
            claims = claimService.getPendingClaims();
        } else {
            claims = claimService.getAllClaims();
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("claims", claims);
        sendJsonResponse(exchange, 200, resp);
    }

    private void handleSubmitClaim(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        JsonObject json = GsonUtils.GSON.fromJson(body, JsonObject.class);
        if (json == null) {
            sendError(exchange, 400, "Invalid JSON payload.");
            return;
        }

        int itemId = json.get("itemId").getAsInt();
        int claimantId = json.get("claimantId").getAsInt();
        String verification = json.get("verificationDetails").getAsString();

        Claim claim = claimService.submitClaim(itemId, claimantId, verification);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("claim", claim);
        sendJsonResponse(exchange, 201, resp);
    }

    private void handleReviewClaim(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        JsonObject json = GsonUtils.GSON.fromJson(body, JsonObject.class);
        if (json == null) {
            sendError(exchange, 400, "Invalid JSON payload.");
            return;
        }

        int claimId = json.get("claimId").getAsInt();
        int adminId = json.has("adminId") ? json.get("adminId").getAsInt() : 1;
        String action = json.get("action").getAsString(); // APPROVE or REJECT
        String notes = json.has("notes") && !json.get("notes").isJsonNull() ? json.get("notes").getAsString() : "";

        if ("APPROVE".equalsIgnoreCase(action)) {
            claimService.approveClaim(claimId, adminId, notes);
        } else if ("REJECT".equalsIgnoreCase(action)) {
            claimService.rejectClaim(claimId, adminId, notes);
        } else {
            sendError(exchange, 400, "Action must be APPROVE or REJECT.");
            return;
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Claim " + action.toLowerCase() + "d successfully.");
        sendJsonResponse(exchange, 200, resp);
    }

    private static Map<String, Object> sanitizeUser(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", u.getUserId());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole().name());
        m.put("active", u.isActive());
        return m;
    }
}
