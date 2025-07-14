package radu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import radu.domain.dto.OrderRequest;
import radu.engine.MatchingEngine;
import radu.engine.Order;
import radu.engine.OrderBook;
import radu.service.OrderService;

import java.time.Instant;

/**
 * REST controller for handling order-related operations.
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

    @PostMapping("/addOrder")
    public ResponseEntity<Void> addOrder(@RequestBody OrderRequest req) {
        if(req == null){
            return ResponseEntity.badRequest().build();
        }
        Order order = orderService.createOrderObject(req);
        orderService.addOrder(order);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deleteOrder")
    public ResponseEntity<Order> deleteOrder(@RequestBody OrderRequest req) {
        if(req == null){
            return ResponseEntity.badRequest().build();
        }
        Order order = orderService.createOrderObject(req);
        orderService.deleteOrder(order);
        return ResponseEntity.ok(order);
    }
}
