package ru.practicum.explorewithme.ewmmain.dto;

import jakarta.validation.constraints.NotBlank;

public class NewCommentDto {
    @NotBlank
    private String text;

    public NewCommentDto() {
    }

    public NewCommentDto(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
