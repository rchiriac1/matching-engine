package radu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import radu.domain.dto.OrderRequest;
import radu.domain.repositories.BuyOrderRepo;
import radu.domain.repositories.SellOrderRepo;
import radu.engine.MatchingEngine;
import radu.engine.Order;
import radu.engine.OrderBook;
import radu.engine.OrderType;

import java.time.Instant;

@Service
public class OrderService {

    BuyOrderRepo buyOrderRepo;
    SellOrderRepo sellOrderRepo;
    MatchingEngine matchingEngine;

    @Autowired
    public OrderService(BuyOrderRepo buyOrderRepo, SellOrderRepo sellOrderRepo, MatchingEngine matchingEngine) {
        this.buyOrderRepo = buyOrderRepo;
        this.sellOrderRepo = sellOrderRepo;
        this.matchingEngine = matchingEngine;
    }

    public void addOrder(Order order) {
        matchingEngine.addOrder(order.getSymbol(), order);
        if(order.getType() == OrderType.BUY) {
            buyOrderRepo.save(order);
        }
        else {
            sellOrderRepo.save(order);
        }
    }

    public Order createOrderObject(OrderRequest req) {
        return new Order(
                req.type,
                req.symbol,
                req.price,
                req.quantity,
                req.expiry != null ? req.expiry : Instant.now().plusSeconds(60),
                req.tif
        );
    }

    public Order deleteOrder(Order order) {
        matchingEngine.cancelOrder(order.getSymbol(), order.getId());
        if(order.getType() == OrderType.BUY) {
            buyOrderRepo.delete(order);
        }
        else {
            sellOrderRepo.delete(order);
        }
        return order;
    }
}
