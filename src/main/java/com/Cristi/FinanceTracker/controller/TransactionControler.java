package com.Cristi.FinanceTracker.controller;

import com.Cristi.FinanceTracker.model.Transactions;
import com.Cristi.FinanceTracker.repository.TransactionRepository;
import com.Cristi.FinanceTracker.service.TransactionService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionControler {
    private final TransactionService transactionService;

    public TransactionControler(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transactions> getAll(){
        return transactionService.getAllTransactions();
    }
    @GetMapping("/{id}")
    public Transactions getById(@PathVariable Long id){
        return transactionService.getTransactionById(id);
    }
    @PostMapping
    public Transactions create(@RequestBody Transactions transaction) {
        return transactionService.createTransaction(transaction);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}
