package ru.practicum.explorewithme.stats.service;

import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.model.EndpointHitEntity;
import ru.practicum.explorewithme.stats.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsService {

    private final EndpointHitRepository repository;

    public StatsService(EndpointHitRepository repository) {
        this.repository = repository;
    }

    public void saveHit(EndpointHit hit) {
        EndpointHitEntity entity = new EndpointHitEntity(
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
        repository.save(entity);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (unique) {
            return repository.findStatsUnique(start, end, uris);
        }
        return repository.findStats(start, end, uris);
    }
}
