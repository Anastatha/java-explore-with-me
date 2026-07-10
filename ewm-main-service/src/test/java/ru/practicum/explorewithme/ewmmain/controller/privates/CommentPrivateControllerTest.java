package ru.practicum.explorewithme.ewmmain.controller.privates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.ewmmain.dto.CommentDto;
import ru.practicum.explorewithme.ewmmain.dto.NewCommentDto;
import ru.practicum.explorewithme.ewmmain.dto.UserShortDto;
import ru.practicum.explorewithme.ewmmain.service.CommentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentPrivateController.class)
class CommentPrivateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void addComment_returnsCreated() throws Exception {
        NewCommentDto request = new NewCommentDto();
        request.setText("Nice event!");

        CommentDto response = new CommentDto(1L, "Nice event!", "2026-01-01 12:00:00", null,
                new UserShortDto(1L, "john"), "PENDING");

        when(commentService.addComment(eq(1L), eq(2L), any(NewCommentDto.class))).thenReturn(response);

        mockMvc.perform(post("/users/1/events/2/comments")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void updateComment_returnsOk() throws Exception {
        NewCommentDto request = new NewCommentDto();
        request.setText("Updated comment");
        CommentDto response = new CommentDto(2L, "Updated comment", "2026-01-01 12:00:00", "2026-01-01 12:05:00",
                new UserShortDto(1L, "john"), "PENDING");

        when(commentService.updateComment(eq(1L), eq(2L), any(NewCommentDto.class))).thenReturn(response);

        mockMvc.perform(patch("/users/1/comments/2")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }
}
