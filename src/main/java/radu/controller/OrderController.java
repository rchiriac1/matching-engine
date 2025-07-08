package radu.controller;

import org.springframework.web.bind.annotation.*;

import radu.dto.OrderRequest;
import radu.engine.MatchingEngine;
import radu.engine.Order;

import java.time.Instant;

/**
 * REST com.controller for handling order-related operations.
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    private final MatchingEngine engine = new MatchingEngine(); // Eventually inject

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
        engine.addOrder(req.symbol, order);
        return "Order added with ID: " + order.getId();
    }
}
