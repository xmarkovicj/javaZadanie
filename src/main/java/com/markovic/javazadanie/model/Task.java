package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "tasks")
@Data
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup group;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
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
