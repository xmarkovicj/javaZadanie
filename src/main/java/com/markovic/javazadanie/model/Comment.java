package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id")
    @NotNull(message = "Task is required")
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    @NotNull(message = "Author is required")
    private User author;

    @Column(nullable = false, length = 2000)
    @NotBlank(message = "Content is required")
    @Size(max = 2000)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
