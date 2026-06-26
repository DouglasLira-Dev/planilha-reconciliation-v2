package com.reconciliation.repository;

import com.reconciliation.model.ReconciliationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconciliationHistoryRepository extends JpaRepository<ReconciliationHistory, Long> {
}