package ru.practicum.explorewithme.ewmmain.controller.privates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.ewmmain.dto.*;
import ru.practicum.explorewithme.ewmmain.service.EventService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventPrivateController.class)
class EventPrivateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @Test
    void getUserEvents_returnsShortList() throws Exception {
        EventShortDto dto = new EventShortDto(1L, "ann", new CategoryDto(1L, "c"), 0L,
                "2026-12-01 10:00:00", new UserShortDto(1L, "u"), false, "title", 0L);
        when(eventService.getUserEvents(eq(1L), anyInt(), anyInt())).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/1/events").param("from", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void addEvent_createsAndReturnsFull() throws Exception {
        NewEventDto req = new NewEventDto();
        req.setAnnotation("annotation long text......");
        req.setCategory(1L);
        req.setDescription("description long text......");
        req.setEventDate("2026-12-01 10:00:00");
        req.setLocation(new LocationDto(10.0f, 20.0f));
        req.setTitle("title");

        EventFullDto created = new EventFullDto(10L, req.getAnnotation(), new CategoryDto(1L, "c"), 0L,
                "2026-01-01 10:00:00", req.getDescription(), req.getEventDate(), new UserShortDto(1L, "u"),
                new LocationDto(10.0f, 20.0f), false, 0, null, true, "PENDING", req.getTitle(), 0L);

        when(eventService.createUserEvent(eq(1L), any(NewEventDto.class))).thenReturn(created);

        mockMvc.perform(post("/users/1/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("title"));
    }
}
