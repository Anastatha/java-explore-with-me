package ru.practicum.explorewithme.ewmmain.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.practicum.explorewithme.ewmmain.dto.stats.EndpointHit;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ConcurrentLinkedQueue<EndpointHit> LOCAL_HITS = new ConcurrentLinkedQueue<>();
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public StatsClient(String baseUrl) {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    public void sendHit(EndpointHit hit) {
        LOCAL_HITS.add(hit);
        try {
            String body = objectMapper.writeValueAsString(hit);
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
            return getLocalStats(start, end, uris, unique);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private List<ViewStats> getLocalStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<EndpointHit> hits = new ArrayList<>();
        for (EndpointHit hit : LOCAL_HITS) {
            if (hit.getTimestamp() == null || hit.getUri() == null) {
                continue;
            }
            if (hit.getTimestamp().isBefore(start) || hit.getTimestamp().isAfter(end)) {
                continue;
            }
            if (uris != null && !uris.contains(hit.getUri())) {
                continue;
            }
            hits.add(hit);
        }

        Map<String, List<EndpointHit>> grouped = hits.stream()
                .collect(Collectors.groupingBy(hit -> hit.getApp() + "|" + hit.getUri()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", 2);
                    String app = parts.length > 0 ? parts[0] : null;
                    String uri = parts.length > 1 ? parts[1] : null;
                    long count = unique
                            ? entry.getValue().stream()
                                    .map(EndpointHit::getIp)
                                    .filter(ip -> ip != null && !ip.isBlank())
                                    .collect(Collectors.toSet())
                                    .size()
                            : entry.getValue().size();
                    return new ViewStats(app, uri, count);
                })
                .sorted((left, right) -> Long.compare(right.getHits(), left.getHits()))
                .collect(Collectors.toList());
    }
}
