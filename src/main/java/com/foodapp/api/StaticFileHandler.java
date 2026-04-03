package com.foodapp.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {

    private final String webDir;

    public StaticFileHandler(String webDir) {
        this.webDir = webDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uriPath = exchange.getRequestURI().getPath();

        // Map "/" to "/index.html"
        if (uriPath.equals("/")) uriPath = "/index.html";

        // Strip leading slash and resolve against web dir
        String relative = uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;
        Path filePath = Paths.get(webDir, relative).normalize();

        // Security: ensure resolved path stays inside webDir
        if (!filePath.startsWith(Paths.get(webDir).normalize())) {
            send(exchange, 403, "text/plain", "Forbidden".getBytes(StandardCharsets.UTF_8));
            return;
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            send(exchange, 404, "text/plain", "Not Found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String contentType = contentTypeFor(filePath.toString());
        byte[] bytes = Files.readAllBytes(filePath);
        send(exchange, 200, contentType, bytes);
    }

    private void send(HttpExchange exchange, int code, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(code, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private String contentTypeFor(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css"))  return "text/css; charset=utf-8";
        if (path.endsWith(".js"))   return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }
}
