package ru.practicum.explorewithme.ewmmain.mapper;

import ru.practicum.explorewithme.ewmmain.dto.CommentDto;
import ru.practicum.explorewithme.ewmmain.model.Comment;

import java.time.format.DateTimeFormatter;

public final class CommentMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CommentMapper() {
    }

    public static CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getCreatedOn() == null ? null : comment.getCreatedOn().format(FORMATTER),
                comment.getEditedOn() == null ? null : comment.getEditedOn().format(FORMATTER),
                UserMapper.toShortDto(comment.getAuthor()),
                comment.getStatus() == null ? null : comment.getStatus().name());
    }
}
