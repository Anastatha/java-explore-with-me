package ru.practicum.explorewithme.ewmmain.mapper;

import ru.practicum.explorewithme.ewmmain.dto.CategoryDto;
import ru.practicum.explorewithme.ewmmain.dto.NewCategoryDto;
import ru.practicum.explorewithme.ewmmain.model.Category;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static CategoryDto toDto(Category category) {
        return category == null ? null : new CategoryDto(category.getId(), category.getName());
    }

    public static Category toEntity(NewCategoryDto request) {
        return request == null ? null : new Category(request.getName());
    }

    public static Category toEntity(CategoryDto dto) {
        return dto == null ? null : new Category(dto.getName());
    }
}
