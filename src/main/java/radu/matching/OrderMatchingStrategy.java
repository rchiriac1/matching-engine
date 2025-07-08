package radu.matching;

import radu.engine.Order;
import radu.engine.OrderBook;

public interface OrderMatchingStrategy {
    boolean canHandle(Order buy, Order sell);
    void match(OrderBook orderBook);
}
