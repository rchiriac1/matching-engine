package radu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import radu.domain.repositories.BuyOrderRepo;
import radu.domain.repositories.SellOrderRepo;
import radu.engine.MatchingEngine;
import radu.engine.Order;
import radu.engine.OrderBook;
import radu.engine.OrderType;

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
}
