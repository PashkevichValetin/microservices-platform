package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import com.pashcevich.data_unifier.exception.UserAdapterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresUserAdapter {

    private final UserRepository userRepository;

    public List<UserEntity> getAllUsers() {
        try {
            log.debug("Fetching all users from PostgreSQL");
            return userRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch all users", e);
            throw new UserAdapterException("Failed to fetch users", e);
        }
    }

    public Optional<UserEntity> getUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            log.debug("Fetching user by id: {}", id);
            return userRepository.findById(id);
        } catch (Exception e) {
            log.error("Failed to fetch user by id: {}", id, e);
            throw new DataUnificationException("Failed to fetch user by id: " + id, e);
        }
    }

    public UnifiedCustomerDto convertToDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        return UnifiedCustomerDto.builder()
                .id(user.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .type("USER")
                .registrationDate(user.getRegistrationDate())
                .build();
    }

    public List<UnifiedCustomerDto> convertToDtoList(List<UserEntity> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public long count() {
        return userRepository.count();
    }
}