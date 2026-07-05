package ru.practicum.explorewithme.ewmmain.dto;

import java.util.List;

public class NewCompilationDto {
    private String title;
    private Boolean pinned = false;
    private List<Long> events;

    public NewCompilationDto() {
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

    public List<Long> getEvents() {
        return events;
    }

    public void setEvents(List<Long> events) {
        this.events = events;
    }
}
