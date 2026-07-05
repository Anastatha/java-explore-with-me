package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("select e from Event e where e.state = :state and (:text is null or lower(e.annotation) like lower(concat('%', :text, '%')) or lower(e.description) like lower(concat('%', :text, '%'))) and (:categories is null or e.category.id in :categories) and (:paid is null or e.paid = :paid) and (:rangeStart is null or e.eventDate >= :rangeStart) and (:rangeEnd is null or e.eventDate <= :rangeEnd)")
    Page<Event> findPublicEvents(@Param("state") EventState state,
                                 @Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 Pageable pageable);

    @Query("select e from Event e where (:users is null or e.initiator.id in :users) and (:states is null or e.state in :states) and (:categories is null or e.category.id in :categories) and (:rangeStart is null or e.eventDate >= :rangeStart) and (:rangeEnd is null or e.eventDate <= :rangeEnd)")
    Page<Event> findByAdminFilters(@Param("users") List<Long> users,
                                   @Param("states") List<EventState> states,
                                   @Param("categories") List<Long> categories,
                                   @Param("rangeStart") LocalDateTime rangeStart,
                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                   Pageable pageable);

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);
}
