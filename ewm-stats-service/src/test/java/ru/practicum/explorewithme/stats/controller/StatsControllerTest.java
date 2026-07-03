package ru.practicum.explorewithme.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatsService statsService;

    @Test
    void hit_shouldReturnCreated() throws Exception {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.0.1");
        hit.setTimestamp(LocalDateTime.of(2022, 9, 6, 11, 0, 23));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hit)))
                .andExpect(status().isCreated());

        verify(statsService, times(1)).saveHit(any(EndpointHit.class));
    }

    @Test
    void getStats_shouldReturnOkAndStatsJson() throws Exception {
        String start = "2022-09-06 00:00:00";
        String end = "2022-09-07 00:00:00";
        List<ViewStats> expected = List.of(new ViewStats("ewm-main-service", "/events/1", 3L));

        when(statsService.getStats(any(), any(), any(), eq(false))).thenReturn(expected);

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end)
                        .param("uris", "/events/1")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));

        verify(statsService, times(1)).getStats(any(), any(), any(), eq(false));
    }
}
