package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "resources")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup group;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private String title;

    private String type;

    @Column(name = "path_or_url", nullable = false)
    private String pathOrUrl;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;


    @PrePersist
    void prePersist() {
        if(uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}
