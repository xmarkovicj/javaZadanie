package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    @NotNull(message = "Group: ")
    private StudyGroup group;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    @NotNull(message = "Uploader: ")
    private User uploadedBy;

    @Column(nullable = false)
    @NotBlank(message = "Title: ")
    @Size(max = 255)
    private String title;

    @Size(max = 100)
    private String type;

    @NotBlank(message = "Path or url: ")
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
