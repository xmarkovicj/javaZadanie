package com.markovic.javazadanie.model;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.*;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name: ")
    @Size(max=100)
    @Column(nullable = false)
    private String name;


    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    @Size(max = 150)
    @Column(nullable = false, unique = true)
    private String email;


    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    private LocalDateTime createdDate = LocalDateTime.now();

}
