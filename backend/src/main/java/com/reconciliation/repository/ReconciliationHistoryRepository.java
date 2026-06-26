package com.reconciliation.repository;

import com.reconciliation.model.ReconciliationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReconciliationHistoryRepository extends JpaRepository<ReconciliationHistory, Long> {

    Page<ReconciliationHistory> findByUserUsernameAndExecutedAtBetween(
            String username,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<ReconciliationHistory> findByExecutedAtBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT h FROM ReconciliationHistory h " +
           "WHERE (:username IS NULL OR h.user.username = :username) " +
           "AND (:startDate IS NULL OR h.executedAt >= :startDate) " +
           "AND (:endDate IS NULL OR h.executedAt <= :endDate)")
    Page<ReconciliationHistory> findWithFilters(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}