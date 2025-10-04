package ${package}.controller;

import ${package}.service.OrderService;
import ${package}.domain.Order;
import ${package}.domain.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Order entity operations.
 *
 * This controller demonstrates advanced MetaObjects patterns:
 * - Complex object relationship management (Order -> OrderItems)
 * - Business workflow implementation (order lifecycle)
 * - Transaction management across multiple entities
 * - Computed field handling (totals, calculations)
 * - Business rule validation in REST endpoints
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Creates a new order.
     *
     * POST /api/orders
     * Content-Type: application/json
     * {
     *   "userId": 1,
     *   "shippingAddress": "123 Main St, City, State 12345",
     *   "notes": "Please deliver after 5 PM"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Long userId = getLongFromRequest(request, "userId");
            String status = (String) request.get("status");
            BigDecimal totalAmount = getBigDecimalFromRequest(request, "totalAmount");

            // Validate required fields
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("User ID is required"));
            }
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Order status is required"));
            }
            if (totalAmount == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Total amount is required"));
            }

            Order order = orderService.createOrder(userId, status, totalAmount);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertOrderToMap(order));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Gets an order by ID.
     *
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id) {
        try {
            Optional<Order> orderOpt = orderService.findOrderById(id);

            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();

            return ResponseEntity.ok(convertOrderToMap(order));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to retrieve order: " + e.getMessage()));
        }
    }

    /**
     * Gets an order by order number.
     *
     * GET /api/orders/number/{orderNumber}
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        try {
            Optional<Order> orderOpt = orderService.findOrderByOrderNumber(orderNumber);

            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();

            return ResponseEntity.ok(convertOrderToMap(order));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to retrieve order: " + e.getMessage()));
        }
    }

    /**
     * Gets all orders for a specific user.
     *
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getOrdersByUserId(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.findOrdersByUserId(userId);

            List<Map<String, Object>> orderList = orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderList);
            response.put("count", orderList.size());
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Gets orders by status.
     *
     * GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderService.findOrdersByStatus(status);

            List<Map<String, Object>> orderList = orders.stream()
                .map(this::convertOrderToMap)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderList);
            response.put("count", orderList.size());
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Adds an item to an order.
     *
     * POST /api/orders/{orderId}/items
     * Content-Type: application/json
     * {
     *   "productName": "Wireless Headphones",
     *   "productSku": "WH-001",
     *   "quantity": 2,
     *   "unitPrice": 99.99,
     *   "discountAmount": 10.00
     * }
     */
    @PostMapping("/{orderId}/items")
    public ResponseEntity<Map<String, Object>> addOrderItem(@PathVariable Long orderId,
                                                           @RequestBody Map<String, Object> request) {
        try {
            String productName = (String) request.get("productName");
            String productDescription = (String) request.get("productDescription");
            Integer quantity = getIntegerFromRequest(request, "quantity");
            BigDecimal unitPrice = getBigDecimalFromRequest(request, "unitPrice");

            // Calculate total price from quantity and unit price
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // Validate required fields
            if (productName == null || quantity == null || unitPrice == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Product name, quantity, and unit price are required"));
            }

            if (quantity <= 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Quantity must be positive"));
            }

            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Unit price cannot be negative"));
            }

            Object result = orderService.addOrderItem(orderId, productName, productDescription,
                quantity, unitPrice, totalPrice);

            // For now, return the service result as it's a placeholder implementation
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(createSuccessResponse("Order item functionality: " + result.toString()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to add order item: " + e.getMessage()));
        }
    }

    /**
     * Removes an item from an order.
     *
     * DELETE /api/orders/items/{orderItemId}
     */
    @DeleteMapping("/items/{orderItemId}")
    public ResponseEntity<Map<String, Object>> removeOrderItem(@PathVariable Long orderItemId) {
        try {
            orderService.removeOrderItem(orderItemId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order item removed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to remove order item: " + e.getMessage()));
        }
    }

    /**
     * Gets all items for an order.
     *
     * GET /api/orders/{orderId}/items
     */
    @GetMapping("/{orderId}/items")
    public ResponseEntity<Map<String, Object>> getOrderItems(@PathVariable Long orderId) {
        try {
            List<Object> orderItems = orderService.getOrderItems(orderId);

            // For now, since the service returns placeholder data, create a simple response
            List<Map<String, Object>> itemList = new ArrayList<>();
            if (!orderItems.isEmpty()) {
                for (Object item : orderItems) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("placeholder", item.toString());
                    itemList.add(itemMap);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("items", itemList);
            response.put("count", itemList.size());
            response.put("orderId", orderId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to retrieve order items: " + e.getMessage()));
        }
    }

    /**
     * Updates order status.
     *
     * PUT /api/orders/{orderId}/status
     * Content-Type: application/json
     * {
     *   "status": "CONFIRMED"
     * }
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long orderId,
                                                               @RequestBody Map<String, Object> request) {
        try {
            String newStatus = (String) request.get("status");

            if (newStatus == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Status is required"));
            }

            orderService.updateOrderStatus(orderId, newStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order status updated successfully");
            response.put("newStatus", newStatus);

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Invalid status transition: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update order status: " + e.getMessage()));
        }
    }

    /**
     * Confirms an order.
     *
     * POST /api/orders/{orderId}/confirm
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Long orderId) {
        try {
            orderService.confirmOrder(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order confirmed successfully");
            response.put("status", "CONFIRMED");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Cannot confirm order: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to confirm order: " + e.getMessage()));
        }
    }

    /**
     * Ships an order.
     *
     * POST /api/orders/{orderId}/ship
     */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Map<String, Object>> shipOrder(@PathVariable Long orderId) {
        try {
            orderService.shipOrder(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order shipped successfully");
            response.put("status", "SHIPPED");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Cannot ship order: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to ship order: " + e.getMessage()));
        }
    }

    /**
     * Delivers an order.
     *
     * POST /api/orders/{orderId}/deliver
     */
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<Map<String, Object>> deliverOrder(@PathVariable Long orderId) {
        try {
            orderService.deliverOrder(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order delivered successfully");
            response.put("status", "DELIVERED");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Cannot deliver order: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to deliver order: " + e.getMessage()));
        }
    }

    /**
     * Cancels an order.
     *
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order cancelled successfully");
            response.put("status", "CANCELLED");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Cannot cancel order: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to cancel order: " + e.getMessage()));
        }
    }

    /**
     * Calculates and applies tax to an order.
     *
     * POST /api/orders/{orderId}/calculate-tax
     * Content-Type: application/json
     * {
     *   "taxRate": 0.08
     * }
     */
    @PostMapping("/{orderId}/calculate-tax")
    public ResponseEntity<Map<String, Object>> calculateTax(@PathVariable Long orderId,
                                                          @RequestBody Map<String, Object> request) {
        try {
            BigDecimal taxRate = getBigDecimalFromRequest(request, "taxRate");

            if (taxRate == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Tax rate is required"));
            }

            if (taxRate.compareTo(BigDecimal.ZERO) < 0 || taxRate.compareTo(BigDecimal.ONE) > 0) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Tax rate must be between 0 and 1"));
            }

            orderService.calculateTax(orderId, taxRate);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tax calculated and applied successfully");
            response.put("taxRate", taxRate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to calculate tax: " + e.getMessage()));
        }
    }

    // Private helper methods

    /**
     * Converts an Order domain object to a Map for JSON serialization.
     */
    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", order.getId());
        map.put("userId", order.getUserRef());
        map.put("orderNumber", order.getOrderNumber());
        map.put("orderDate", order.getOrderDate());
        map.put("status", order.getStatus());
        map.put("subtotalAmount", order.getSubtotalAmount());
        map.put("taxAmount", order.getTaxAmount());
        map.put("totalAmount", order.getTotalAmount());

        if (order.getShippingAddress() != null) {
            map.put("shippingAddress", order.getShippingAddress());
        }

        if (order.getNotes() != null) {
            map.put("notes", order.getNotes());
        }

        return map;
    }

    /**
     * Converts an OrderItem domain object to a Map for JSON serialization.
     */
    private Map<String, Object> convertOrderItemToMap(OrderItem orderItem) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", orderItem.getId());
        map.put("orderId", orderItem.getOrderRef());
        map.put("productName", orderItem.getProductName());
        map.put("quantity", orderItem.getQuantity());
        map.put("unitPrice", orderItem.getUnitPrice());
        map.put("lineTotal", orderItem.getTotalPrice());

        if (orderItem.getProductDescription() != null) {
            map.put("productDescription", orderItem.getProductDescription());
        }

        return map;
    }

    /**
     * Creates a standardized error response.
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now());
        return error;
    }

    /**
     * Creates a standardized success response.
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> success = new HashMap<>();
        success.put("success", true);
        success.put("message", message);
        success.put("timestamp", LocalDateTime.now());
        return success;
    }

    /**
     * Safely extracts Long value from request map.
     */
    private Long getLongFromRequest(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Safely extracts Integer value from request map.
     */
    private Integer getIntegerFromRequest(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Safely extracts BigDecimal value from request map.
     */
    private BigDecimal getBigDecimalFromRequest(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}