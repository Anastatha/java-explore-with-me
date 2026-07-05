package ru.practicum.explorewithme.stats.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.util.DateTimeUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class StatsClient {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public StatsClient(String baseUrl) {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    public void sendHit(EndpointHit hit) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(hit);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/hit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        client.send(request, HttpResponse.BodyHandlers.discarding());
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) throws IOException, InterruptedException {
        String url = baseUrl + "/stats?start=" + encode(start.format(DateTimeUtils.FORMATTER)) +
                "&end=" + encode(end.format(DateTimeUtils.FORMATTER)) +
                "&unique=" + unique;
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                url += "&uris=" + encode(uri);
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return Arrays.asList(mapper.readValue(response.body(), ViewStats[].class));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
