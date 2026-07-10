package ru.practicum.explorewithme.ewmmain.dto;

public class CommentDto {
    private Long id;
    private String text;
    private String createdOn;
    private String editedOn;
    private UserShortDto author;
    private String status;

    public CommentDto() {
    }

    public CommentDto(Long id, String text, String createdOn, String editedOn, UserShortDto author, String status) {
        this.id = id;
        this.text = text;
        this.createdOn = createdOn;
        this.editedOn = editedOn;
        this.author = author;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getEditedOn() {
        return editedOn;
    }

    public void setEditedOn(String editedOn) {
        this.editedOn = editedOn;
    }

    public UserShortDto getAuthor() {
        return author;
    }

    public void setAuthor(UserShortDto author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
