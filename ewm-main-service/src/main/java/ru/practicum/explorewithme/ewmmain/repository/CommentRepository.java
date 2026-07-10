package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.ewmmain.model.Comment;
import ru.practicum.explorewithme.ewmmain.model.CommentStatus;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status);

    List<Comment> findByAuthorId(Long authorId);

    @Query("select c from Comment c where :status is null or c.status = :status")
    List<Comment> findByStatus(@Param("status") CommentStatus status);
}
