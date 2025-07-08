package radu.matching;

import radu.engine.Order;
import radu.engine.OrderBook;
import radu.engine.TimeInForce;

import java.util.logging.Logger;

/**
 * Handles Good-Till-Cancel (GTC) order com.matching logic.
 * GTC orders are partially matched if needed and remaining quantity is kept in the order book.
 */
public class GTCMatcher implements OrderMatchingStrategy {

    private static final Logger logger = Logger.getLogger(GTCMatcher.class.getName());

    /**
     * Determines if this strategy should handle the given orders.
     *
     * @param buy  the top buy order
     * @param sell the top sell order
     * @return true if either order is GTC
     */
    @Override
    public boolean canHandle(Order buy, Order sell) {
        return buy.getTif() == TimeInForce.GTC || sell.getTif() == TimeInForce.GTC;
    }

    /**
     * Matches a Good-Till-Cancel (GTC) order with the given order book.
     * Remaining unmatched quantity stays in the order book.
     *
     * @param orderBook order book we work on
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
        logger.info("GTC match: " + minQuantity + " units @ " + sellOrder.getPrice());

        if (buyOrder.getQuantity() > 0) orderBook.addOrder(buyOrder);
        else orderBook.cancelOrder(buyOrder.getId());

        if (sellOrder.getQuantity() > 0) orderBook.addOrder(sellOrder);
        else orderBook.cancelOrder(sellOrder.getId());
    }
}

