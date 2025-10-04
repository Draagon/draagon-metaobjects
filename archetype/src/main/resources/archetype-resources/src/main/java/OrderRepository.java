package ${package}.repository;

import ${package}.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Order entity.
 *
 * This repository extends JpaRepository to provide:
 * - Standard CRUD operations (save, findById, findAll, delete, etc.)
 * - Custom query methods for order-specific business logic
 * - User-order relationship queries
 * - Order status and date-based queries
 * - Aggregate functions for reporting
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user ID, ordered by order date (newest first)
     */
    List<Order> findByUserRefOrderByOrderDateDesc(Long userId);

    /**
     * Find orders by status, ordered by order date (newest first)
     */
    List<Order> findByStatusOrderByOrderDateDesc(String status);

    /**
     * Find orders after a specific date, ordered by order date (newest first)
     */
    List<Order> findByOrderDateAfterOrderByOrderDateDesc(LocalDateTime date);

    /**
     * Find orders by user and status
     */
    List<Order> findByUserIdAndStatus(Long userId, String status);

    /**
     * Find orders with total amount greater than specified value
     */
    List<Order> findByTotalAmountGreaterThan(BigDecimal amount);

    /**
     * Find orders with total amount between specified values
     */
    List<Order> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Count orders by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count orders by status
     */
    long countByStatus(String status);

    /**
     * Count orders created after specific date
     */
    long countByOrderDateAfter(LocalDateTime date);

    /**
     * Calculate total order amount for a user
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.userId = :userId")
    BigDecimal calculateTotalAmountByUserId(@Param("userId") Long userId);

    /**
     * Calculate total order amount by status
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal calculateTotalAmountByStatus(@Param("status") String status);

    /**
     * Find top N most recent orders
     */
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findTopNRecentOrders(int limit);

    /**
     * Find orders for a user within date range
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders with status and minimum amount
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.totalAmount >= :minAmount ORDER BY o.totalAmount DESC")
    List<Order> findByStatusAndMinAmount(@Param("status") String status, @Param("minAmount") BigDecimal minAmount);

    /**
     * Get order summary statistics by status
     */
    @Query("SELECT o.status, COUNT(o), AVG(o.totalAmount), SUM(o.totalAmount) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatisticsByStatus();
}