package radu.matching;

import radu.engine.Order;
import radu.engine.OrderBook;
import radu.engine.TimeInForce;

import java.util.logging.Logger;


/**
 * Handles Immediate-Or-Cancel (IOC) order com.matching logic.
 * IOC orders are matched instantly for whatever is available, and remaining quantity is discarded.
 */
public class IOCMatcher implements OrderMatchingStrategy {

    private static final Logger logger = Logger.getLogger(IOCMatcher.class.getName());

    /**
     * Determines if this strategy should handle the given orders.
     *
     * @param buy  the top buy order
     * @param sell the top sell order
     * @return true if either order is IOC
     */
    @Override
    public boolean canHandle(Order buy, Order sell) {
        return buy.getTif() == TimeInForce.IOC || sell.getTif() == TimeInForce.IOC;
    }

    /**
     * Matches an Immediate-Or-Cancel (IOC) order with the given order book.
     * Remaining unmatched quantity is discarded.
     *
     * @param orderBook the order book we work on
     */
    @Override
    public void match(OrderBook orderBook) {
        Order buyOrder = orderBook.getBuyOrders().poll();
        Order sellOrder = orderBook.getSellOrders().poll();

        assert buyOrder != null;
        assert sellOrder != null;
        int minQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

        buyOrder.reduceQuantity(minQuantity);
        sellOrder.reduceQuantity(minQuantity);

        orderBook.recordTrade(buyOrder.getId(), sellOrder.getId(), sellOrder.getPrice(), minQuantity);
        logger.info("IOC match: " + minQuantity + " units @ " + sellOrder.getPrice());

        orderBook.cancelOrder(buyOrder.getId());
        orderBook.cancelOrder(sellOrder.getId());

    }
}
