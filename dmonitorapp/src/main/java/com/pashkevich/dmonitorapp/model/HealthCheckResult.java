package com.pashkevich.dmonitorapp.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("health_check_results")
public class HealthCheckResult {

    @Id
    private Long id;
    @Column("service_definition_id")
//    @NotNull(message = "Service ID cannot be null")
    private Long serviceDefinitionId;

    @Column("checked_at")
    @CreatedDate
    private LocalDateTime checkAt = LocalDateTime.now();

    private ServiceStatus status;

    @Column("response_time_ms")
    private Long responseTimeMs;

    @Column("message")
    private String errorMessage;

    @Column("additional_info")
    private String additionalInfo;
}
