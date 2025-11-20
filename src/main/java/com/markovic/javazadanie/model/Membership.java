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
    @Column(name = "membership_id")
    private Long membershipId;

    //Kto
    @ManyToOne(optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    // v ktorej skupine

    @ManyToOne(optional = false)
    @JoinColumn(name="group_id", nullable = false)
    private StudyGroup group;

    private String role = "MEMBER";

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();
}
