package ru.practicum.explorewithme.ewmmain.controller.privates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.ewmmain.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.ewmmain.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.ewmmain.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.ewmmain.service.ParticipationRequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestPrivateController.class)
class RequestPrivateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParticipationRequestService requestService;

    @Test
    void addRequest_returnsDto() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto(1L, "2026-01-01 10:00:00", 2L, 1L, "PENDING");
        when(requestService.addParticipationRequest(eq(1L), eq(2L))).thenReturn(dto);

        mockMvc.perform(post("/users/1/requests").param("eventId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserRequests_returnsList() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto(1L, "2026-01-01 10:00:00", 2L, 1L, "PENDING");
        when(requestService.getUserRequests(eq(1L))).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void cancelRequest_returnsDto() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto(5L, "2026-01-01 10:00:00", 2L, 1L, "CANCELED");
        when(requestService.cancelRequest(eq(1L), eq(5L))).thenReturn(dto);

        mockMvc.perform(patch("/users/1/requests/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void changeRequestStatus_returnsResult() throws Exception {
        EventRequestStatusUpdateRequest req = new EventRequestStatusUpdateRequest();
        req.setRequestIds(List.of(1L));
        req.setStatus("CONFIRMED");
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(List.of(), List.of());
        when(requestService.changeRequestStatus(eq(1L), eq(2L), any(EventRequestStatusUpdateRequest.class))).thenReturn(result);

        mockMvc.perform(patch("/users/1/events/2/requests")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
