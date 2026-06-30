package com.nasim.example.DailyAdviceServer.socket.messageGenerator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AiMessageGenerator implements MessageGenerator {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String onMessage() {
        try {
            String requestBody = """
                    {
                      "model": "qwen2.5:3b",
                      "prompt": "Give me one short daily advice in simple English. Only one sentence.",
                      "stream": false
                    }
                    """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return extractResponse(response.body());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Could not generate advice now.";
        }
    }

    private String extractResponse(String json) {
        String key = "\"response\":\"";
        int start = json.indexOf(key);

        if (start == -1) {
            return "No advice generated.";
        }

        start += key.length();
        int end = json.indexOf("\"", start);

        if (end == -1) {
            return "No advice generated.";
        }

        return json.substring(start, end)
                .replace("\\n", " ")
                .replace("\\\"", "\"")
                .trim();
    }
}