package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresUserAdapter {

    private final UserRepository userRepository;

    public List<UnifiedCustomerDto> getAllUserForUnification() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUnifiedDto)
                .toList();
    }

    public UnifiedCustomerDto getUserById(String userId) {
        log.debug("Fetching user by ID: {}", userId);

        try {
            // 1. Преобразуем String userId в Long
            Long userIdLong;
            try {
                userIdLong = Long.parseLong(userId);
            } catch (NumberFormatException e) {
                log.error("Invalid user ID format: {}", userId);
                return null;
            }

            // 2. Ищем пользователя в репозитории
            Optional<UserEntity> userEntity = userRepository.findById(userIdLong);

            // 3. Преобразуем в UnifiedCustomerDto или возвращаем null
            return userEntity
                    .map(this::convertToUnifiedDtoWithLogging)
                    .orElseGet(() -> {
                        log.debug("User not found with ID: {}", userId);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Error fetching user with ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Database error while fetching user: " + userId, e);
        }
    }

    private UnifiedCustomerDto convertToUnifiedDto(UserEntity user) {
        UnifiedCustomerDto dto = new UnifiedCustomerDto();
        dto.setUserId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRegistrationDate(user.getRegistrationDate());
        return dto;
    }

    private UnifiedCustomerDto convertToUnifiedDtoWithLogging(UserEntity user) {
        UnifiedCustomerDto dto = convertToUnifiedDto(user);
        log.debug("User found: ID={}, Email={}, Name={}",
                user.getId(), user.getEmail(), user.getName());
        return dto;
    }
}