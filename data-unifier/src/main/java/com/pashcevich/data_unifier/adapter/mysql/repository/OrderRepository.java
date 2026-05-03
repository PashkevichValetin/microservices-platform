package com.pashcevich.data_unifier.adapter.mysql.repository;

import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserId(Long id);

    List<OrderEntity> findByStatus(String status);
}
