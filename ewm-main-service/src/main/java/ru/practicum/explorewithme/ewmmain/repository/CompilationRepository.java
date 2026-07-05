package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.ewmmain.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
}
