package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.ewmmain.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
