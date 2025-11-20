package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

//Koment pre radost

@Entity
@Table(name = "tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    @NotNull(message = "Group: ")
    private StudyGroup group;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    @NotNull(message = "Creator: ")
    private User createdBy;

    @NotBlank(message = "Title required")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @Size(max = 2000)
    private String description;

    @Column(nullable = false)
    @NotBlank(message = "Status: ")
    private String status;

    private LocalDateTime deadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if(createdBy == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
