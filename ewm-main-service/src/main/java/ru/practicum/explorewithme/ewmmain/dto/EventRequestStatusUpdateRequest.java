package ru.practicum.explorewithme.ewmmain.dto;

import java.util.List;

public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private String status;

    public EventRequestStatusUpdateRequest() {
    }

    public List<Long> getRequestIds() {
        return requestIds;
    }

    public void setRequestIds(List<Long> requestIds) {
        this.requestIds = requestIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
