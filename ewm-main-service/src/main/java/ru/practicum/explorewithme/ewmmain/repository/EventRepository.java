package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.ewmmain.model.Event;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);
}
