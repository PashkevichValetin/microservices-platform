package com.pashcevich.data_unifier.adapter.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "updated_at") // Исправлено: было updatedAt
    private LocalDateTime updatedAt;

    public UserEntity(String name, String email) {
        this.name = name;
        this.email = email;
        this.registrationDate = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    public void updateTimestamps() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
}