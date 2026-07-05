package ru.practicum.explorewithme.ewmmain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.explorewithme.ewmmain.dto.CategoryDto;
import ru.practicum.explorewithme.ewmmain.dto.NewCategoryDto;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.model.Category;
import ru.practicum.explorewithme.ewmmain.repository.CategoryRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addCategory_ShouldReturnSavedCategoryDto() {
        NewCategoryDto request = new NewCategoryDto();
        request.setName("Music");

        Category saved = new Category("Music");
        saved.setId(1L);

        when(repository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.addCategory(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Music", result.getName());
        verify(repository).save(any(Category.class));
    }

    @Test
    void deleteCategory_WhenNotFound_ShouldThrowNotFoundException() {
        when(repository.existsById(1L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.deleteCategory(1L));

        assertEquals("Category with id=1 was not found", exception.getMessage());
        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void updateCategory_WhenExists_ShouldReturnUpdatedDto() {
        Category existing = new Category("Music");
        existing.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Category.class))).thenReturn(existing);

        CategoryDto request = new CategoryDto();
        request.setId(1L);
        request.setName("Sports");

        CategoryDto result = categoryService.updateCategory(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Sports", result.getName());
        verify(repository).findById(1L);
    }
}
