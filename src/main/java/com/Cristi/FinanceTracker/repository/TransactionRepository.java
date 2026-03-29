package com.Cristi.FinanceTracker.repository;

import com.Cristi.FinanceTracker.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transactions, Long> {

    List<Transactions> findByType(String type);
    List<Transactions> findByCategory(String category);
    List<Transactions> findByDateAfter(LocalDate date);
    List<Transactions> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Transactions> findByTypeAndCategory(String type, String category);
    List<Transactions> findByTypeAndDateBetween(String type, LocalDate startDate, LocalDate endDate);
    List<Transactions> findByCategoryAndDateBetween(String category, LocalDate startDate, LocalDate endDate);
    List<Transactions> findByTypeAndCategoryAndDateBetween(String type, String category, LocalDate startDate, LocalDate endDate);
}
