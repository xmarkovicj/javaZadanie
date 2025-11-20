package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_groups")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;


    @NotBlank(message = "Group name: ")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Size(max = 2000)
    private String description;

    private LocalDateTime createdAt=LocalDateTime.now();
}
