package com.expense.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    
    @Autowired
    private ExpenseRepository repository;
    
    @GetMapping
    public List<Expense> getAll() {
        return repository.findAll();
    }
    
    @PostMapping
    public Expense create(@RequestBody Expense expense) {
        return repository.save(expense);
    }
    
    @PutMapping("/{id}")
    public Expense update(@PathVariable Long id, @RequestBody Expense expense) {
        expense.setId(id);
        return repository.save(expense);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/category/{category}")
    public List<Expense> getByCategory(@PathVariable String category) {
        return repository.findByCategory(category);
    }
    
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", repository.getTotalExpenses());
        stats.put("count", repository.count());
        
        List<Object[]> categoryTotals = repository.getCategoryTotals();
        Map<String, Double> categories = new HashMap<>();
        for (Object[] row : categoryTotals) {
            categories.put((String) row[0], (Double) row[1]);
        }
        stats.put("byCategory", categories);
        
        return stats;
    }
    
    @GetMapping("/date-range")
    public List<Expense> getByDateRange(@RequestParam String start, @RequestParam String end) {
        return repository.findByDateBetween(LocalDate.parse(start), LocalDate.parse(end));
    }
    
    @GetMapping("/csv")
    public ResponseEntity<String> exportCSV() {
        List<Expense> expenses = repository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Category,Description,Amount,Date,Payment Method,Recurring\n");
        
        for (Expense e : expenses) {
            csv.append(String.format("%d,%s,%s,%.2f,%s,%s,%b\n",
                e.getId(), e.getCategory(), e.getDescription(),
                e.getAmount(), e.getDate(), e.getPaymentMethod(), e.isRecurring()));
        }
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=expenses.csv")
            .header("Content-Type", "text/csv")
            .body(csv.toString());
    }
}
