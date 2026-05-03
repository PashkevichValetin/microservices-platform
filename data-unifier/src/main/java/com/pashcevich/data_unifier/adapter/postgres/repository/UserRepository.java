package com.pashcevich.data_unifier.adapter.postgres.repository;

import com.pashcevich.data_unifier.adapter.postgres.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
