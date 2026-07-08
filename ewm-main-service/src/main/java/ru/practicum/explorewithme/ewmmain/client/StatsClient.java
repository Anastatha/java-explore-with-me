package ru.practicum.explorewithme.ewmmain.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.practicum.explorewithme.ewmmain.dto.stats.ViewStats;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public StatsClient(String baseUrl) {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    public void sendHit(ru.practicum.explorewithme.ewmmain.dto.stats.EndpointHit hit) {
        try {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("app", hit.getApp());
            bodyMap.put("uri", hit.getUri());
            bodyMap.put("ip", hit.getIp());
            bodyMap.put("timestamp", hit.getTimestamp() == null ? null : hit.getTimestamp().format(FORMATTER));
            String body = objectMapper.writeValueAsString(bodyMap);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/hit"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        try {
            StringBuilder uriBuilder = new StringBuilder(baseUrl + "/stats?start=").append(encode(start.format(FORMATTER)))
                    .append("&end=").append(encode(end.format(FORMATTER)))
                    .append("&unique=").append(unique);
            if (uris != null) {
                uris.forEach(uri -> uriBuilder.append("&uris=").append(encode(uri)));
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uriBuilder.toString()))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Arrays.asList(objectMapper.readValue(response.body(), ViewStats[].class));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return List.of();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
