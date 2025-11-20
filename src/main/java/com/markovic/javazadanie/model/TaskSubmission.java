package com.markovic.javazadanie.model;

import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id")
    @NotNull(message = "Task: ")
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @NotNull(message = "User is required")
    private User user;

    // napr. text riešenia
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Content is required")
    private String content;

    // napr. link na súbor v cloude
    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    private Double grade;

    private String feedback;

    @PrePersist
    void prePersist() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}
