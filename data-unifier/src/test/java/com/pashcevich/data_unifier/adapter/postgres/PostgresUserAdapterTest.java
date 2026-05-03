package com.pashcevich.data_unifier.adapter.postgres;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import com.pashcevich.data_unifier.adapter.postgres.repository.UserRepository;
import com.pashcevich.data_unifier.exception.UserAdapterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostgresUserAdapterTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostgresUserAdapter postgresUserAdapter;

    private UserEntity userEntity;
    private UnifiedCustomerDto userDto;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setName("John Doe");
        userEntity.setEmail("john@example.com");
        userEntity.setRegistrationDate(LocalDateTime.now());

        userDto = UnifiedCustomerDto.builder()
                .id(1L)
                .userId(1L)
                .name("John Doe")
                .email("john@example.com")
                .type("USER")
                .registrationDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        // GIVEN
        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane@example.com");
        user2.setRegistrationDate(LocalDateTime.now());

        List<UserEntity> users = Arrays.asList(userEntity, user2);

        when(userRepository.findAll()).thenReturn(users);

        // WHEN
        List<UserEntity> result = postgresUserAdapter.getAllUsers(); // ИСПРАВЛЕНО: метод возвращает List<UserEntity>

        // THEN
        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_withValidId_shouldReturnUser() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        // WHEN
        Optional<UserEntity> result = postgresUserAdapter.getUserById(1L); // ИСПРАВЛЕНО: метод принимает Long

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_withNullId_shouldReturnEmptyOptional() {
        // WHEN
        Optional<UserEntity> result = postgresUserAdapter.getUserById(null); // ИСПРАВЛЕНО: метод принимает Long

        // THEN
        assertThat(result).isEmpty();
        verifyNoInteractions(userRepository);
    }

    @Test
    void convertToDto_shouldConvertUserEntityToDto() {
        // WHEN
        UnifiedCustomerDto result = postgresUserAdapter.convertToDto(userEntity);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void convertToDto_withNull_shouldReturnNull() {
        // WHEN
        UnifiedCustomerDto result = postgresUserAdapter.convertToDto(null);

        // THEN
        assertThat(result).isNull();
    }

    @Test
    void convertToDtoList_shouldConvertListOfEntities() {
        // GIVEN
        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane@example.com");
        List<UserEntity> users = Arrays.asList(userEntity, user2);

        // WHEN
        List<UnifiedCustomerDto> result = postgresUserAdapter.convertToDtoList(users);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(1).getUserId()).isEqualTo(2L);
    }

    @Test
    void convertToDtoList_withNull_shouldReturnEmptyList() {
        // WHEN
        List<UnifiedCustomerDto> result = postgresUserAdapter.convertToDtoList(null);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrueWhenUserExists() {
        // GIVEN
        when(userRepository.existsById(1L)).thenReturn(true);

        // WHEN
        boolean result = postgresUserAdapter.existsById(1L);

        // THEN
        assertThat(result).isTrue();
        verify(userRepository).existsById(1L);
    }

    @Test
    void count_shouldReturnNumberOfUsers() {
        // GIVEN
        when(userRepository.count()).thenReturn(5L);

        // WHEN
        long result = postgresUserAdapter.count();

        // THEN
        assertThat(result).isEqualTo(5L);
        verify(userRepository).count();
    }

    @Test
    void getAllUsers_withException_shouldThrowUserAdapterException() {
        // GIVEN
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThatThrownBy(() -> postgresUserAdapter.getAllUsers())
                .isInstanceOf(UserAdapterException.class)
                .hasMessageContaining("Failed to fetch users");

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_withException_shouldThrowDataUnificationException() {
        // GIVEN
        when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThatThrownBy(() -> postgresUserAdapter.getUserById(1L))
                .isInstanceOf(UserAdapterException.class)
                .hasMessageContaining("Failed to fetch user by id");

        verify(userRepository).findById(1L);
    }
}