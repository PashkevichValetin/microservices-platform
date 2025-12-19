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
@Table("service_definitions")
public class ServiceDefinition {

    @Id
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;
    @NotBlank(message = "URL cannot be empty")
    private String url;

    @Column("check_interval_seconds")
    @NotNull(message = "Check interval must be specified")
    private Integer checkIntervalSeconds;

    @Column("check_type")
    @NotNull(message = "Check type must be null")
    private CheckType checkType;

    @Column("database_config_id")
    private Long databaseConfigId;

    @Column("created_at")
    private LocalDateTime createAT;

    @Column("updated_at")
    private LocalDateTime updateAt;

    @Column("is_active")
    private Boolean isActive = true;

}
