package com.Cristi.FinanceTracker.service;

import com.Cristi.FinanceTracker.model.Transactions;
import com.Cristi.FinanceTracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;  // declare the field

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;  // Spring injects it here
    }

    public Transactions createTransaction(Transactions transactions){
        if(transactions.getDate().isAfter(LocalDate.now())){
            throw new RuntimeException("Cannot create a transaction in the future!");
        }
        if(transactions.getType().equals("EXPENSE")){
            transactions.setAmount(transactions.getAmount().negate());
        }
        if(transactions.getAmount().abs().compareTo(BigDecimal.valueOf(1000)) > 0){
            System.out.println("Large transaction: " + transactions.getAmount());
        }
        return transactionRepository.save(transactions);
    }
    public List<Transactions> getAllTransactions(){
        return transactionRepository.findAll();
    }
    public Transactions getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new RuntimeException("Transaction not found");
        }
        transactionRepository.deleteById(id);
    }
};


