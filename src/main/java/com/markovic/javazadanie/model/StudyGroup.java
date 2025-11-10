package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "groups")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private LocalDateTime createdAt=LocalDateTime.now();
}
