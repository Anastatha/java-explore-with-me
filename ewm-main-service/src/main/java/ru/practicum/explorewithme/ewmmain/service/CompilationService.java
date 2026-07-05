package ru.practicum.explorewithme.ewmmain.service;

import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.ewmmain.dto.CompilationDto;
import ru.practicum.explorewithme.ewmmain.dto.NewCompilationDto;
import ru.practicum.explorewithme.ewmmain.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.model.Compilation;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.repository.CompilationRepository;
import ru.practicum.explorewithme.ewmmain.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import ru.practicum.explorewithme.ewmmain.dto.EventShortDto;
import ru.practicum.explorewithme.ewmmain.dto.CategoryDto;
import ru.practicum.explorewithme.ewmmain.dto.UserShortDto;
import org.springframework.data.domain.PageRequest;

@Service
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public CompilationService(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        int page = from / Math.max(size, 1);
        List<Compilation> comps = compilationRepository.findAll(PageRequest.of(page, size)).getContent();
        return comps.stream()
                .filter(c -> pinned == null || Boolean.valueOf(pinned).equals(c.getPinned()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));
        return toDto(compilation);
    }

    public CompilationDto saveCompilation(NewCompilationDto request) {
        Compilation compilation = new Compilation(request.getTitle(), request.getPinned() != null ? request.getPinned() : false);
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(request.getEvents());
            compilation.setEvents(new HashSet<>(events));
        }
        return toDto(compilationRepository.save(compilation));
    }

    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(String.format("Compilation with id=%d was not found", compId));
        }
        compilationRepository.deleteById(compId);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null) {
            compilation.setEvents(new HashSet<>(eventRepository.findAllById(request.getEvents())));
        }
        return toDto(compilationRepository.save(compilation));
    }

    private CompilationDto toDto(Compilation compilation) {
        List<EventShortDto> events = compilation.getEvents().stream()
                .map(event -> new EventShortDto(
                        event.getId(),
                        event.getAnnotation(),
                        new CategoryDto(event.getCategory().getId(), event.getCategory().getName()),
                        0L,
                        event.getEventDate().toString(),
                        new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()),
                        event.getPaid(),
                        event.getTitle(),
                        0L
                ))
                .collect(Collectors.toList());
        return new CompilationDto(compilation.getId(), compilation.getTitle(), compilation.getPinned(), events);
    }
}
