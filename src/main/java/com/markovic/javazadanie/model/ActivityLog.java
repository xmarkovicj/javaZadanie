package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "activity_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String details;

    @PrePersist
    void prePersist() {
        if(timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }


}
