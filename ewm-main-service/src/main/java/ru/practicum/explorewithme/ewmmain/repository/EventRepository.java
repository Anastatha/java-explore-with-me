package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.ewmmain.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);
}
