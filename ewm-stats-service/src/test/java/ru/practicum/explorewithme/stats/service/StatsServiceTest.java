package ru.practicum.explorewithme.stats.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StatsServiceTest {

    private final EndpointHitRepository repository = Mockito.mock(EndpointHitRepository.class);
    private final StatsService service = new StatsService(repository);

    @Test
    void saveHit_shouldPersistHitEntity() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.0.1");
        hit.setTimestamp(LocalDateTime.of(2022, 9, 6, 11, 0, 23));

        service.saveHit(hit);

        ArgumentCaptor<ru.practicum.explorewithme.stats.model.EndpointHitEntity> captor = ArgumentCaptor.forClass(ru.practicum.explorewithme.stats.model.EndpointHitEntity.class);
        verify(repository, times(1)).save(captor.capture());

        ru.practicum.explorewithme.stats.model.EndpointHitEntity saved = captor.getValue();
        assertThat(saved.getApp()).isEqualTo("ewm-main-service");
        assertThat(saved.getUri()).isEqualTo("/events/1");
        assertThat(saved.getIp()).isEqualTo("192.168.0.1");
        assertThat(saved.getTimestamp()).isEqualTo(LocalDateTime.of(2022, 9, 6, 11, 0, 23));
    }

    @Test
    void getStats_shouldReturnNonUniqueResults() {
        LocalDateTime start = LocalDateTime.of(2022, 9, 6, 0, 0);
        LocalDateTime end = LocalDateTime.of(2022, 9, 7, 0, 0);
        List<String> uris = List.of("/events/1");
        List<ViewStats> expected = List.of(new ViewStats("ewm-main-service", "/events/1", 3L));

        when(repository.findStats(start, end, uris)).thenReturn(expected);

        List<ViewStats> actual = service.getStats(start, end, uris, false);

        assertThat(actual).containsExactlyElementsOf(expected);
        verify(repository, times(1)).findStats(start, end, uris);
        verify(repository, never()).findStatsUnique(any(), any(), any());
    }

    @Test
    void getStats_shouldReturnUniqueResults() {
        LocalDateTime start = LocalDateTime.of(2022, 9, 6, 0, 0);
        LocalDateTime end = LocalDateTime.of(2022, 9, 7, 0, 0);
        List<String> uris = List.of("/events/1");
        List<ViewStats> expected = List.of(new ViewStats("ewm-main-service", "/events/1", 2L));

        when(repository.findStatsUnique(start, end, uris)).thenReturn(expected);

        List<ViewStats> actual = service.getStats(start, end, uris, true);

        assertThat(actual).containsExactlyElementsOf(expected);
        verify(repository, times(1)).findStatsUnique(start, end, uris);
        verify(repository, never()).findStats(any(), any(), any());
    }
}
