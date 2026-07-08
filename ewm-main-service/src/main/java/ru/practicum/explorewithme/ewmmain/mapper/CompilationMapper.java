package ru.practicum.explorewithme.ewmmain.mapper;

import ru.practicum.explorewithme.ewmmain.dto.CompilationDto;
import ru.practicum.explorewithme.ewmmain.model.Compilation;

import java.util.stream.Collectors;

public final class CompilationMapper {
    private CompilationMapper() {
    }

    public static CompilationDto toDto(Compilation compilation) {
        return compilation == null ? null : new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                compilation.getEvents().stream()
                        .map(event -> EventMapper.toShortDto(event, 0L, 0L))
                        .collect(Collectors.toList())
        );
    }
}
