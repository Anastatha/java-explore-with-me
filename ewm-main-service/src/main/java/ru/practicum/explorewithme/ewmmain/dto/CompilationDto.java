package ru.practicum.explorewithme.ewmmain.dto;

import java.util.List;

public class CompilationDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private List<EventShortDto> events;

    public CompilationDto() {
    }

    public CompilationDto(Long id, String title, Boolean pinned, List<EventShortDto> events) {
        this.id = id;
        this.title = title;
        this.pinned = pinned;
        this.events = events;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public List<EventShortDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventShortDto> events) {
        this.events = events;
    }
}
