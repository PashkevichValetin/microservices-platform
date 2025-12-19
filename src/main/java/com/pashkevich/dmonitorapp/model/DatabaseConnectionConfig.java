package com.pashkevich.dmonitorapp.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("database_connection")
public class DatabaseConnectionConfig {

    @Id
    private Long id;

    @NotBlank(message = "Database name cannot be empty")
    @Column("name")
    private String name;

    @NotBlank(message = "Connection URL cannot be empty")
    @Column("connection_url")
    private String connectionUrl;

    @NotBlank(message = "Username cannot be empty")
    @Column("username")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Column("password")
    private String password;

    @NotBlank(message = "Driver class name cannot be empty")
    @Column("driver_class_name")
    private String driverClassName;

    @NotNull(message = "Active status must be specified")
    @Column("is_active")
    private Boolean isActive;

    @Column("created_at")
    private LocalDateTime createAt;
}
