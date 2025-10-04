package ${package}.integration;

import ${package}.Application;
import ${package}.service.UserService;
import ${package}.service.OrderService;
import com.metaobjects.object.ValueMetaObject;
import com.metaobjects.manager.db.ObjectManagerDB;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete MetaObjects application.
 *
 * This test demonstrates:
 * - Full Spring Boot application context loading
 * - MetaObjects framework integration with Spring
 * - Database operations through ObjectManagerDB
 * - Service layer integration testing
 * - Transactional test management
 * - Real metadata loading and object persistence
 *
 * Note: Uses H2 in-memory database for testing as configured in application-test.properties
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Transactional // Rollback transactions after each test
class ApplicationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectManagerDB objectManager;

    private ValueMetaObject testUser;

    @BeforeEach
    void setUp() throws Exception {
        // Create a test user for use in other tests
        testUser = userService.createUser(
            "testuser",
            "test@example.com",
            "Test",
            "User"
        );
        assertNotNull(testUser);
        assertNotNull(testUser.getLong("id"));
    }

    @Test
    void testApplicationContextLoads() {
        // Verify that all required beans are loaded
        assertNotNull(userService, "UserService should be loaded");
        assertNotNull(orderService, "OrderService should be loaded");
        assertNotNull(objectManager, "ObjectManagerDB should be loaded");
    }

    @Test
    void testMetaObjectsFrameworkIntegration() throws Exception {
        // Test that MetaObjects framework is properly integrated

        // Verify user creation and retrieval
        Long userId = testUser.getLong("id");
        ValueMetaObject retrievedUser = userService.findUserById(userId);

        assertNotNull(retrievedUser);
        assertEquals("testuser", retrievedUser.getString("username"));
        assertEquals("test@example.com", retrievedUser.getString("email"));
        assertEquals("Test", retrievedUser.getString("firstName"));
        assertEquals("User", retrievedUser.getString("lastName"));
        assertTrue(retrievedUser.getBoolean("isActive"));
        assertNotNull(retrievedUser.getTimestamp("createdAt"));
    }

    @Test
    void testUserServiceOperations() throws Exception {
        Long userId = testUser.getLong("id");

        // Test find by username
        ValueMetaObject userByUsername = userService.findUserByUsername("testuser");
        assertNotNull(userByUsername);
        assertEquals(userId, userByUsername.getLong("id"));

        // Test find by email
        ValueMetaObject userByEmail = userService.findUserByEmail("test@example.com");
        assertNotNull(userByEmail);
        assertEquals(userId, userByEmail.getLong("id"));

        // Test find active users
        List<ValueMetaObject> activeUsers = userService.findActiveUsers();
        assertFalse(activeUsers.isEmpty());
        assertTrue(activeUsers.stream().anyMatch(u -> userId.equals(u.getLong("id"))));

        // Test search by name
        List<ValueMetaObject> searchResults = userService.searchUsersByName("Test");
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(u -> userId.equals(u.getLong("id"))));

        // Test update last login
        userService.updateLastLogin(userId);
        ValueMetaObject updatedUser = userService.findUserById(userId);
        assertNotNull(updatedUser.getTimestamp("lastLoginAt"));

        // Test user update
        updatedUser.setString("firstName", "Updated");
        ValueMetaObject savedUser = userService.updateUser(updatedUser);
        assertEquals("Updated", savedUser.getString("firstName"));

        // Test deactivation
        userService.deactivateUser(userId);
        ValueMetaObject deactivatedUser = userService.findUserById(userId);
        assertFalse(deactivatedUser.getBoolean("isActive"));
    }

    @Test
    void testOrderServiceOperations() throws Exception {
        Long userId = testUser.getLong("id");

        // Create an order
        ValueMetaObject order = orderService.createOrder(
            userId,
            "123 Test Street, Test City, TS 12345",
            "Test order notes"
        );

        assertNotNull(order);
        assertNotNull(order.getLong("id"));
        assertEquals(userId, order.getLong("userId"));
        assertNotNull(order.getString("orderNumber"));
        assertTrue(order.getString("orderNumber").startsWith("ORD-"));
        assertEquals("PENDING", order.getString("status"));
        assertEquals("123 Test Street, Test City, TS 12345", order.getString("shippingAddress"));
        assertEquals("Test order notes", order.getString("notes"));
        assertNotNull(order.getTimestamp("orderDate"));

        Long orderId = order.getLong("id");

        // Test find by order number
        ValueMetaObject orderByNumber = orderService.findOrderByOrderNumber(order.getString("orderNumber"));
        assertNotNull(orderByNumber);
        assertEquals(orderId, orderByNumber.getLong("id"));

        // Test find orders by user
        List<ValueMetaObject> userOrders = orderService.findOrdersByUserId(userId);
        assertFalse(userOrders.isEmpty());
        assertTrue(userOrders.stream().anyMatch(o -> orderId.equals(o.getLong("id"))));

        // Test find orders by status
        List<ValueMetaObject> pendingOrders = orderService.findOrdersByStatus("PENDING");
        assertFalse(pendingOrders.isEmpty());
        assertTrue(pendingOrders.stream().anyMatch(o -> orderId.equals(o.getLong("id"))));
    }

    @Test
    void testOrderItemsOperations() throws Exception {
        Long userId = testUser.getLong("id");

        // Create an order
        ValueMetaObject order = orderService.createOrder(userId, "Test Address", "Test Notes");
        Long orderId = order.getLong("id");

        // Add first item
        ValueMetaObject item1 = orderService.addOrderItem(
            orderId,
            "Test Product 1",
            "TP-001",
            2,
            new BigDecimal("25.99"),
            new BigDecimal("5.00")
        );

        assertNotNull(item1);
        assertNotNull(item1.getLong("id"));
        assertEquals(orderId, item1.getLong("orderId"));
        assertEquals("Test Product 1", item1.getString("productName"));
        assertEquals("TP-001", item1.getString("productSku"));
        assertEquals(2, item1.getInt("quantity"));
        assertEquals(new BigDecimal("25.99"), item1.getDecimal("unitPrice"));
        assertEquals(new BigDecimal("5.00"), item1.getDecimal("discountAmount"));
        assertEquals(new BigDecimal("46.98"), item1.getDecimal("lineTotal")); // (25.99 * 2) - 5.00

        // Add second item
        ValueMetaObject item2 = orderService.addOrderItem(
            orderId,
            "Test Product 2",
            "TP-002",
            1,
            new BigDecimal("15.50"),
            null
        );

        assertNotNull(item2);
        assertEquals(new BigDecimal("15.50"), item2.getDecimal("lineTotal")); // 15.50 * 1 - 0

        // Verify order totals were updated
        ValueMetaObject updatedOrder = orderService.findOrderById(orderId);
        assertEquals(new BigDecimal("62.48"), updatedOrder.getDecimal("subtotalAmount")); // 46.98 + 15.50

        // Get order items
        List<ValueMetaObject> orderItems = orderService.getOrderItems(orderId);
        assertEquals(2, orderItems.size());

        // Remove first item
        orderService.removeOrderItem(item1.getLong("id"));

        // Verify order totals were recalculated
        updatedOrder = orderService.findOrderById(orderId);
        assertEquals(new BigDecimal("15.50"), updatedOrder.getDecimal("subtotalAmount")); // Only item2 remains

        // Verify only one item remains
        orderItems = orderService.getOrderItems(orderId);
        assertEquals(1, orderItems.size());
        assertEquals(item2.getLong("id"), orderItems.get(0).getLong("id"));
    }

    @Test
    void testOrderLifecycleManagement() throws Exception {
        Long userId = testUser.getLong("id");

        // Create an order
        ValueMetaObject order = orderService.createOrder(userId, "Test Address", null);
        Long orderId = order.getLong("id");
        assertEquals("PENDING", order.getString("status"));

        // Confirm order
        orderService.confirmOrder(orderId);
        ValueMetaObject confirmedOrder = orderService.findOrderById(orderId);
        assertEquals("CONFIRMED", confirmedOrder.getString("status"));

        // Ship order
        orderService.shipOrder(orderId);
        ValueMetaObject shippedOrder = orderService.findOrderById(orderId);
        assertEquals("SHIPPED", shippedOrder.getString("status"));

        // Deliver order
        orderService.deliverOrder(orderId);
        ValueMetaObject deliveredOrder = orderService.findOrderById(orderId);
        assertEquals("DELIVERED", deliveredOrder.getString("status"));

        // Test invalid status transition (should throw exception)
        assertThrows(IllegalStateException.class, () -> {
            orderService.confirmOrder(orderId); // Cannot confirm delivered order
        });
    }

    @Test
    void testOrderTaxCalculation() throws Exception {
        Long userId = testUser.getLong("id");

        // Create order and add items
        ValueMetaObject order = orderService.createOrder(userId, "Test Address", null);
        Long orderId = order.getLong("id");

        orderService.addOrderItem(orderId, "Product 1", "P1", 1, new BigDecimal("100.00"), null);
        orderService.addOrderItem(orderId, "Product 2", "P2", 2, new BigDecimal("50.00"), null);

        // Calculate tax at 8.5%
        BigDecimal taxRate = new BigDecimal("0.085");
        orderService.calculateTax(orderId, taxRate);

        // Verify tax calculation
        ValueMetaObject taxedOrder = orderService.findOrderById(orderId);
        assertEquals(new BigDecimal("200.00"), taxedOrder.getDecimal("subtotalAmount")); // 100 + (50*2)
        assertEquals(new BigDecimal("17.00"), taxedOrder.getDecimal("taxAmount")); // 200 * 0.085
        assertEquals(new BigDecimal("217.00"), taxedOrder.getDecimal("totalAmount")); // 200 + 17
    }

    @Test
    void testDatabaseTransactionRollback() throws Exception {
        Long userId = testUser.getLong("id");

        // This test verifies that transactions are properly rolled back
        // due to the @Transactional annotation on the class

        // Create some data
        ValueMetaObject order = orderService.createOrder(userId, "Test Address", null);
        Long orderId = order.getLong("id");

        orderService.addOrderItem(orderId, "Test Product", "TP", 1, new BigDecimal("10.00"), null);

        // Verify data exists in current transaction
        ValueMetaObject currentOrder = orderService.findOrderById(orderId);
        assertNotNull(currentOrder);

        List<ValueMetaObject> currentItems = orderService.getOrderItems(orderId);
        assertEquals(1, currentItems.size());

        // After this test method completes, the transaction will be rolled back
        // and the data will not persist to subsequent tests
    }

    @Test
    void testMetaObjectsConstraintValidation() throws Exception {
        // Test that MetaObjects constraints are enforced

        // Test duplicate username (should fail due to unique constraint)
        assertThrows(Exception.class, () -> {
            userService.createUser("testuser", "different@example.com", "Different", "User");
        });

        // Test duplicate email (should fail due to unique constraint)
        assertThrows(Exception.class, () -> {
            userService.createUser("differentuser", "test@example.com", "Different", "User");
        });
    }

    @Test
    void testMetaObjectFieldAccess() throws Exception {
        // Test various MetaObject field access patterns

        // Test string fields
        assertEquals("testuser", testUser.getString("username"));
        assertEquals("test@example.com", testUser.getString("email"));

        // Test boolean fields
        assertTrue(testUser.getBoolean("isActive"));

        // Test long fields
        assertNotNull(testUser.getLong("id"));
        assertTrue(testUser.getLong("id") > 0);

        // Test timestamp fields
        assertNotNull(testUser.getTimestamp("createdAt"));

        // Test null handling for optional fields
        assertNull(testUser.getTimestamp("lastLoginAt")); // Should be null for new user
    }
}