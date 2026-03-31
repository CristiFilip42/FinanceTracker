package com.Cristi.FinanceTracker.service;

import com.Cristi.FinanceTracker.model.Transactions;
import com.Cristi.FinanceTracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    public List<Transactions> getFilteredTransactions(String type, String category,
                                                      LocalDate startDate, LocalDate endDate){
        List<Transactions> transactions = transactionRepository.findAll();
        if (type != null && !type.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> t.getType().equals(type))
                    .toList();
        }
        if (category != null && !category.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> t.getCategory().equals(category))
                    .toList();
        }
        if (startDate != null) {
            transactions = transactions.stream()
                    .filter(t -> !t.getDate().isBefore(startDate))
                    .toList();
        }
        if (endDate != null) {
            transactions = transactions.stream()
                    .filter(t -> !t.getDate().isAfter(endDate))
                    .toList();
        }
        return transactions;
    }
    public Map<String, BigDecimal> getSpendingByCategory(List<Transactions> transactions){
        return transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE"))
                .collect(Collectors.groupingBy(
                        Transactions::getCategory,Collectors.reducing(
                                BigDecimal.ZERO,t -> t.getAmount().abs(),BigDecimal::add
                        )
                ));

    }
    public Map<String, BigDecimal[]> getMonthlyIncomeVsExpense(List<Transactions> transactions){
        Map<String, BigDecimal[]> monthlyData = new TreeMap<>();

        for (Transactions t : transactions){
            String month = t.getDate().getYear() + "-" +
                    String.format("%02d",t.getDate().getMonthValue());

            monthlyData.putIfAbsent(month, new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO});

            if (t.getType().equals("INCOME")){
                monthlyData.get(month)[0] = monthlyData.get(month)[0].add(t.getAmount());
            } else {
                monthlyData.get(month)[1] = monthlyData.get(month)[1].add(t.getAmount().abs());
            }
        }

        return monthlyData;
    }
};


