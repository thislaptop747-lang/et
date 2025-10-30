package com.expense.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {
    
    @Autowired
    private ExpenseRepository repository;
    
    @GetMapping
    public List<Expense> getAll(@RequestHeader("X-User-Id") String userId) {
        return repository.findByUserId(userId);
    }
    
    @PostMapping
    public Expense create(@RequestHeader("X-User-Id") String userId, @RequestBody Expense expense) {
        expense.setUserId(userId);
        return repository.save(expense);
    }
    
    @PutMapping("/{id}")
    public Expense update(@RequestHeader("X-User-Id") String userId, @PathVariable Long id, @RequestBody Expense expense) {
        expense.setId(id);
        expense.setUserId(userId);
        return repository.save(expense);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("X-User-Id") String userId, @PathVariable Long id) {
        Optional<Expense> expense = repository.findById(id);
        if (expense.isPresent() && expense.get().getUserId().equals(userId)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/category/{category}")
    public List<Expense> getByCategory(@RequestHeader("X-User-Id") String userId, @PathVariable String category) {
        return repository.findByUserIdAndCategory(userId, category);
    }
    
    @GetMapping("/stats")
    public Map<String, Object> getStats(@RequestHeader("X-User-Id") String userId) {
        Map<String, Object> stats = new HashMap<>();
        Double total = repository.getTotalExpensesByUserId(userId);
        stats.put("total", total != null ? total : 0.0);
        stats.put("count", repository.getCountByUserId(userId));
        
        List<Object[]> categoryTotals = repository.getCategoryTotalsByUserId(userId);
        Map<String, Double> categories = new HashMap<>();
        for (Object[] row : categoryTotals) {
            categories.put((String) row[0], (Double) row[1]);
        }
        stats.put("byCategory", categories);
        
        return stats;
    }
    
    @GetMapping("/date-range")
    public List<Expense> getByDateRange(@RequestHeader("X-User-Id") String userId, @RequestParam String start, @RequestParam String end) {
        return repository.findByUserIdAndDateBetween(userId, LocalDate.parse(start), LocalDate.parse(end));
    }
    
    @GetMapping("/csv")
    public ResponseEntity<String> exportCSV(@RequestHeader("X-User-Id") String userId) {
        List<Expense> expenses = repository.findByUserId(userId);
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Category,Description,Amount,Date,Payment Method,Recurring\n");
        
        for (Expense e : expenses) {
            csv.append(String.format("%d,%s,%s,%.2f,%s,%s,%b\n",
                e.getId(), e.getCategory(), e.getDescription(),
                e.getAmount(), e.getDate(), e.getPaymentMethod(), e.isRecurring()));
        }
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=cache-expenses.csv")
            .header("Content-Type", "text/csv")
            .body(csv.toString());
    }
}
