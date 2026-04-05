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
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.unbescape.csv.CsvEscape.escapeCsv;

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

    public String exportToCSV(List<Transactions> transactions){
        StringBuilder csv = new StringBuilder();
        csv.append("date,type,category,amount,description\\n");

        for(Transactions t : transactions){
            csv.append(t.getDate()).append(",");
            csv.append(t.getType()).append(",");
            csv.append(escapeCsv(t.getCategory())).append(",");
            csv.append(t.getAmount()).append(",");
            csv.append(escapeCsv(t.getDescription())).append(",");
            csv.append("\n");
        }
        return csv.toString();
    }
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public int importFromCSV(MultipartFile file ) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream())
        );
        String header = reader.readLine();
        if(header == null)
            throw new IllegalAccessError("CSV is empty!");

        int count = 0;
        String line;
        int lineNumber = 1;

        while((line = reader.readLine()) != null){
            lineNumber++;
            if(line.trim().isEmpty()) continue;

            try {
                String[] fields = parseCSVLine(line);
                if(fields.length < 5)
                    throw new IllegalArgumentException("Expected 5 firelds!");

                Transactions t = new Transactions();
                t.setDate(LocalDate.parse(fields[0].trim()));
                t.setType(fields[1].trim().toUpperCase());
                t.setCategory(fields[2].trim());
                t.setAmount(new BigDecimal(fields[3].trim()));
                t.setDescription(fields[4].trim());

                if (t.getType().equals("EXPENSE")) {
                    t.setAmount(t.getAmount().negate());
                }

                transactionRepository.save(t);
                count++;
            } catch (Exception e) {
                System.out.println("Skipped line " + lineNumber + ": " + e.getMessage());
            }
        }
        return count;
    }
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
};


