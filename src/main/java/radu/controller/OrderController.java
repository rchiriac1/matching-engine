package radu.controller;

import org.springframework.web.bind.annotation.*;

import radu.domain.dto.OrderRequest;
import radu.engine.MatchingEngine;
import radu.engine.Order;
import radu.engine.OrderBook;
import radu.service.OrderService;

import java.time.Instant;

/**
 * REST com.controller for handling order-related operations.
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    private final MatchingEngine engine;
    private final OrderService orderService;

    public OrderController(MatchingEngine engine, OrderService orderService) {
        this.engine = engine;
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public String addOrder(@RequestBody OrderRequest req) {
        Order order = new Order(
                req.type,
                req.symbol,
                req.price,
                req.quantity,
                req.expiry != null ? req.expiry : Instant.now().plusSeconds(60),
                req.tif
        );
        orderService.addOrder(order);
        return "Order added with ID: " + order.getId();
    }
}
