package ${package}.service;

import ${package}.domain.Order;
import ${package}.domain.User;
import ${package}.repository.OrderRepository;
import ${package}.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Collections;

/**
 * Service class for Order entity operations using standard JPA patterns.
 *
 * This service demonstrates:
 * - Generated domain objects from MetaObjects metadata
 * - Standard Spring Data JPA repositories
 * - Relationship management between entities (User-Order)
 * - Business logic validation
 * - Proper transaction management
 */
@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new order for a user
     */
    public Order createOrder(Long userId, String status, BigDecimal totalAmount) {
        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Validate required fields
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Order status is required");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount must be non-negative");
        }

        // Create and save order
        Order order = new Order();
        order.setUserRef(userId);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setOrderDate(LocalDateTime.now().toString());

        return orderRepository.save(order);
    }

    /**
     * Find order by ID
     */
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Find all orders for a user
     */
    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserRefOrderByOrderDateDesc(userId);
    }

    /**
     * Find orders by status
     */
    @Transactional(readOnly = true)
    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    /**
     * Find all orders
     */
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * Update order status
     */
    public Order updateOrderStatus(Long id, String newStatus) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Order status is required");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Update order total amount
     */
    public Order updateOrderTotal(Long id, BigDecimal newTotalAmount) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (newTotalAmount == null || newTotalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount must be non-negative");
        }

        order.setTotalAmount(newTotalAmount);
        return orderRepository.save(order);
    }

    /**
     * Delete order by ID
     */
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }

    /**
     * Calculate total order value for a user
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateUserOrderTotal(Long userId) {
        return orderRepository.calculateTotalAmountByUserId(userId);
    }

    /**
     * Count orders for a user
     */
    @Transactional(readOnly = true)
    public long countOrdersByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    /**
     * Count orders by status
     */
    @Transactional(readOnly = true)
    public long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Get recent orders (last 30 days)
     */
    @Transactional(readOnly = true)
    public List<Order> getRecentOrders() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return orderRepository.findByOrderDateAfterOrderByOrderDateDesc(thirtyDaysAgo);
    }

    // Method aliases for controller compatibility

    /**
     * Find order by ID (alias for findById)
     */
    @Transactional(readOnly = true)
    public Optional<Order> findOrderById(Long id) {
        return findById(id);
    }

    /**
     * Find orders by user ID (alias for findByUserId)
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByUserId(Long userId) {
        return findByUserId(userId);
    }

    /**
     * Find orders by status (alias for findByStatus)
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByStatus(String status) {
        return findByStatus(status);
    }

    /**
     * Find order by order number
     */
    @Transactional(readOnly = true)
    public Optional<Order> findOrderByOrderNumber(String orderNumber) {
        // This would need a repository method, for now return empty
        return Optional.empty();
    }

    // Order lifecycle methods

    /**
     * Confirm an order
     */
    public Order confirmOrder(Long id) {
        return updateOrderStatus(id, "CONFIRMED");
    }

    /**
     * Ship an order
     */
    public Order shipOrder(Long id) {
        return updateOrderStatus(id, "SHIPPED");
    }

    /**
     * Deliver an order
     */
    public Order deliverOrder(Long id) {
        return updateOrderStatus(id, "DELIVERED");
    }

    /**
     * Cancel an order
     */
    public Order cancelOrder(Long id) {
        return updateOrderStatus(id, "CANCELLED");
    }

    // OrderItem management (simplified for now)

    /**
     * Add order item (simplified)
     */
    public Object addOrderItem(Long orderId, String productName, String productDescription, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        // Simplified - would need OrderItem entity and repository
        return Map.of("message", "OrderItem functionality not yet implemented");
    }

    /**
     * Remove order item (simplified)
     */
    public void removeOrderItem(Long orderItemId) {
        // Simplified - would need OrderItem repository
    }

    /**
     * Get order items (simplified)
     */
    public List<Object> getOrderItems(Long orderId) {
        // Simplified - would need OrderItem repository
        return Collections.emptyList();
    }

    /**
     * Calculate tax for order
     */
    public BigDecimal calculateTax(Long orderId, BigDecimal taxRate) {
        Order order = findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        return order.getTotalAmount().multiply(taxRate);
    }
}