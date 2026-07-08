package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.ewmmain.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Override
    @EntityGraph(attributePaths = {
            "events",
            "events.category",
            "events.initiator"
    })
    Page<Compilation> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "events",
            "events.category",
            "events.initiator"
    })
    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);
}
