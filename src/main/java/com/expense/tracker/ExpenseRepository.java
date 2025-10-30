package com.expense.tracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(String userId);
    List<Expense> findByUserIdAndCategory(String userId, String category);
    List<Expense> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId")
    Double getTotalExpensesByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.userId = :userId")
    Long getCountByUserId(@Param("userId") String userId);
    
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.userId = :userId GROUP BY e.category")
    List<Object[]> getCategoryTotalsByUserId(@Param("userId") String userId);
}
