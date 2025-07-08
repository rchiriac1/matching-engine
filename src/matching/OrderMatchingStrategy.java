package matching;

import engine.Order;
import engine.OrderBook;

public interface OrderMatchingStrategy {
    boolean canHandle(Order buy, Order sell);
    void match(OrderBook orderBook);
}
