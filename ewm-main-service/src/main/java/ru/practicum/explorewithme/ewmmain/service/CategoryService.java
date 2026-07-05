package ru.practicum.explorewithme.ewmmain.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.ewmmain.dto.CategoryDto;
import ru.practicum.explorewithme.ewmmain.dto.NewCategoryDto;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.model.Category;
import ru.practicum.explorewithme.ewmmain.repository.CategoryRepository;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.stream.Collectors;
import ru.practicum.explorewithme.ewmmain.mapper.CategoryMapper;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public CategoryDto addCategory(NewCategoryDto request) {
        Category saved = repository.save(CategoryMapper.toEntity(request));
        return CategoryMapper.toDto(saved);
    }

    public void deleteCategory(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(String.format("Category with id=%d was not found", id));
        }
        repository.deleteById(id);
    }

    public CategoryDto updateCategory(Long id, CategoryDto request) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", id)));
        category.setName(request.getName());
        return new CategoryDto(category.getId(), category.getName());
    }

    public List<CategoryDto> getCategories(int from, int size) {
        int page = from / Math.max(size, 1);
        return repository.findAll(PageRequest.of(page, size)).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategory(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", id)));
        return CategoryMapper.toDto(category);
    }
}
