package com.Cristi.FinanceTracker.controller;

import com.Cristi.FinanceTracker.model.Transactions;
import com.Cristi.FinanceTracker.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {
    private final TransactionService transactionService;

    public WebController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/")
    public String dashboard(Model model,
                            @RequestParam(required = false) String type,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {

        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        List<Transactions> transactions = transactionService.getFilteredTransactions(type, category, start, end);

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
        System.out.println("=== FILTER DEBUG ===");
        System.out.println("Type: " + type);
        System.out.println("Category: " + category);
        System.out.println("Start: " + startDate);
        System.out.println("End: " + endDate);

        Map<String, BigDecimal> spendingByCategory = transactionService.getSpendingByCategory(transactions);
        Map<String, BigDecimal[]> monthlyData = transactionService.getMonthlyIncomeVsExpense(transactions);

        model.addAttribute("categoryLabels", spendingByCategory.keySet());
        model.addAttribute("categoryValues", spendingByCategory.values());

        model.addAttribute("monthLabels", monthlyData.keySet());
        model.addAttribute("monthlyIncome", monthlyData.values().stream()
                .map(arr -> arr[0]).toList());
        model.addAttribute("monthlyExpenses", monthlyData.values().stream()
                .map(arr -> arr[1]).toList());

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

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCSV(){
        List<Transactions> transactions = transactionService.getAllTransactions();
        String csv = transactionService.exportToCSV(transactions);

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=transactions.csv")
                    .header("Content-Type", "text/csv")
                    .body(csv.getBytes());
    }

    @PostMapping("/import")
    public String importCSV(@RequestParam("file")MultipartFile file,
                            RedirectAttributes redirectAttributes){
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file");
            return "redirect:/";
        }
        try {
            int count = transactionService.importFromCSV(file);
            redirectAttributes.addFlashAttribute("success",
                    "Imported " + count + " transactions successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Import failed: " + e.getMessage());
        }
        return "redirect:/";
    }

}
