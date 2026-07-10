package ru.practicum.explorewithme.ewmmain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.ewmmain.dto.CommentDto;
import ru.practicum.explorewithme.ewmmain.dto.NewCommentDto;
import ru.practicum.explorewithme.ewmmain.exception.ConflictException;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.mapper.CommentMapper;
import ru.practicum.explorewithme.ewmmain.model.Comment;
import ru.practicum.explorewithme.ewmmain.model.CommentStatus;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.User;
import ru.practicum.explorewithme.ewmmain.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final EventServiceSupport support;

    public CommentService(CommentRepository commentRepository,
            EventServiceSupport support) {
        this.commentRepository = commentRepository;
        this.support = support;
    }

    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto request) {
        User author = support.findUser(userId);
        Event event = support.getEventOrThrow(eventId);
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId) {
        support.getEventOrThrow(eventId);
        return commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new NotFoundException(String.format("Comment with id=%d was not found", commentId));
        }
        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Only pending comments can be edited");
        }
        comment.setText(request.getText());
        comment.setEditedOn(LocalDateTime.now());
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto moderateComment(Long commentId, String action) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Comment has already been moderated");
        }
        switch (action) {
            case "approve" -> comment.setStatus(CommentStatus.APPROVED);
            case "reject" -> comment.setStatus(CommentStatus.REJECTED);
            default -> throw new IllegalArgumentException("Unsupported moderation action: " + action);
        }
        return CommentMapper.toDto(commentRepository.save(comment));
    }
}
