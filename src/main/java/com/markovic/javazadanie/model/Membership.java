package com.markovic.javazadanie.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;





@Entity
@Table(name = "memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Kto
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    // v ktorej skupine

    @ManyToOne
    @JoinColumn(name="group_id", nullable = false)
    private StudyGroup group;

    private String role = "MEMBER";

    private LocalDateTime joinedAt = LocalDateTime.now();
}
