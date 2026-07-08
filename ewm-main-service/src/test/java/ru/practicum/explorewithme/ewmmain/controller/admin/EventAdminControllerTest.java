package ru.practicum.explorewithme.ewmmain.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.ewmmain.dto.CategoryDto;
import ru.practicum.explorewithme.ewmmain.dto.EventFullDto;
import ru.practicum.explorewithme.ewmmain.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.ewmmain.dto.UserShortDto;
import ru.practicum.explorewithme.ewmmain.service.EventService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventAdminController.class)
class EventAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @Test
    void getEvents_returnsList() throws Exception {
        EventFullDto dto = new EventFullDto(1L, "annotation...long...text", new CategoryDto(1L, "cat"), 0L,
                "2026-01-01 10:00:00", "desc...long...text", "2026-12-01 10:00:00",
                new UserShortDto(1L, "u"), null, false, 0, null, true, "PENDING", "title", 0L);
        when(eventService.getEventsForAdmin(nullable(List.class), nullable(List.class), nullable(List.class), nullable(String.class), nullable(String.class), anyInt(), anyInt()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/events").param("from", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void updateEvent_callsService() throws Exception {
        UpdateEventAdminRequest req = new UpdateEventAdminRequest();
        req.setTitle("new title");
        EventFullDto result = new EventFullDto(2L, "annotation", new CategoryDto(1L, "c"), 0L,
                "2026-01-01 10:00:00", "desc", "2026-12-01 10:00:00",
                new UserShortDto(2L, "u2"), null, false, 0, null, true, "PENDING", "new title", 0L);
        when(eventService.updateEventAdmin(eq(2L), any(UpdateEventAdminRequest.class))).thenReturn(result);

        mockMvc.perform(patch("/admin/events/2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("new title"));
    }
}
