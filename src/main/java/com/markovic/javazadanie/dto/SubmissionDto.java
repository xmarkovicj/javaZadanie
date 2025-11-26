package com.markovic.javazadanie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmissionDto {
    private Long id;
    private String content;
    private String submittedAt;
    private String authorEmail;
}
