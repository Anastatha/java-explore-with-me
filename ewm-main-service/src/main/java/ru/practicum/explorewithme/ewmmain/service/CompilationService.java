package ru.practicum.explorewithme.ewmmain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.ewmmain.mapper.CompilationMapper;

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
        Pageable pageable = PageRequest.of(page, size);

        Page<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(pageable)
                : compilationRepository.findByPinned(pinned, pageable);

        return compilations.getContent().stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));
        return CompilationMapper.toDto(compilation);
    }

    public CompilationDto saveCompilation(NewCompilationDto request) {
        Compilation compilation = new Compilation(request.getTitle(), request.getPinned() != null ? request.getPinned() : false);
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(request.getEvents());
            compilation.setEvents(new HashSet<>(events));
        }
        return CompilationMapper.toDto(compilationRepository.save(compilation));
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
        return CompilationMapper.toDto(compilationRepository.save(compilation));
    }
}
