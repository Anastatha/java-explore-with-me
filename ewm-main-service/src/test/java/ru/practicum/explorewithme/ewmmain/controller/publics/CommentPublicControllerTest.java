package ru.practicum.explorewithme.ewmmain.controller.publics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.ewmmain.dto.CommentDto;
import ru.practicum.explorewithme.ewmmain.dto.UserShortDto;
import ru.practicum.explorewithme.ewmmain.service.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentPublicController.class)
class CommentPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    void getComments_returnsApprovedComments() throws Exception {
        CommentDto comment = new CommentDto(2L, "Great event", "2026-01-01 12:00:00", null,
                new UserShortDto(1L, "john"), "APPROVED");
        when(commentService.getCommentsByEvent(eq(3L))).thenReturn(List.of(comment));

        mockMvc.perform(get("/events/3/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }
}
