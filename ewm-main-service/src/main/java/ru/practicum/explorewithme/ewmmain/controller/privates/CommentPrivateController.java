package ru.practicum.explorewithme.ewmmain.controller.privates;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ewmmain.dto.CommentDto;
import ru.practicum.explorewithme.ewmmain.dto.NewCommentDto;
import ru.practicum.explorewithme.ewmmain.service.CommentService;

@RestController
@RequestMapping("/users/{userId}")
public class CommentPrivateController {
    private final CommentService commentService;

    public CommentPrivateController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
            @PathVariable Long eventId,
            @Validated @RequestBody NewCommentDto request) {
        return commentService.addComment(userId, eventId, request);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
            @PathVariable Long commentId,
            @Validated @RequestBody NewCommentDto request) {
        return commentService.updateComment(userId, commentId, request);
    }
}
