package ru.practicum.explorewithme.ewmmain.controller.admin;

import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ewmmain.dto.CommentDto;
import ru.practicum.explorewithme.ewmmain.service.CommentService;

@RestController
@RequestMapping("/admin/comments")
public class CommentAdminController {
    private final CommentService commentService;

    public CommentAdminController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PatchMapping("/{commentId}")
    public CommentDto moderateComment(@PathVariable Long commentId,
            @RequestParam("action") String action) {
        return commentService.moderateComment(commentId, action);
    }
}
