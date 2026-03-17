package com.Cristi.FinanceTracker.repository;

import com.Cristi.FinanceTracker.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transactions, Long> {

    List<Transactions> findByType(String type);
    List<Transactions> findByCategory(String category);
    List<Transactions> findByDateAfter(LocalDate date);
}
