package com.markovic.javazadanie.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUpdateDto {
    private String title;
    private String description;
    private LocalDateTime deadline; // môže byť null = nemením
    private String status;
}
