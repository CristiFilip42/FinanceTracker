package com.Cristi.FinanceTracker.controller;

import com.Cristi.FinanceTracker.model.Transactions;
import com.Cristi.FinanceTracker.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class WebController {
    private final TransactionService transactionService;

    public WebController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Transactions> transactions = transactionService.getAllTransactions();

        BigDecimal totalIncome = transactions.stream()
            .filter(t -> t.getType().equals("INCOME"))
                .map(Transactions::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE"))
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        model.addAttribute("transactions", transactions);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("balance",balance);

        return "dashboard";
    }

    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("transaction", new Transactions());
        return "transaction-form";
    }
    @PostMapping("/add")
    public String addTransaction(@ModelAttribute Transactions transactions){
        transactionService.createTransaction(transactions);
        return "redirect:/";
    }
    @GetMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return "redirect:/";
    }
}
