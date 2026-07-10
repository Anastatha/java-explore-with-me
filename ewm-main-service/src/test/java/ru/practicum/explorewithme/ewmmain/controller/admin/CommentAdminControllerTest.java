package ru.practicum.explorewithme.ewmmain.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.ewmmain.dto.CommentDto;
import ru.practicum.explorewithme.ewmmain.dto.UserShortDto;
import ru.practicum.explorewithme.ewmmain.service.CommentService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentAdminController.class)
class CommentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    void moderateComment_approvesComment() throws Exception {
        CommentDto response = new CommentDto(2L, "Great event", "2026-01-01 12:00:00", null,
                new UserShortDto(1L, "john"), "APPROVED");

        when(commentService.moderateComment(eq(2L), eq("approve"))).thenReturn(response);

        mockMvc.perform(patch("/admin/comments/2").param("action", "approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
