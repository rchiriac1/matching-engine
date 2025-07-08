package matching;

import engine.Order;
import engine.OrderBook;
import engine.OrderType;
import engine.TimeInForce;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/**
 * Handles Fill-Or-Kill (FOK) order matching logic.
 * FOK orders are fully matched immediately or discarded entirely.
 */
public class FOKMatcher implements OrderMatchingStrategy{

    private static final Logger logger = Logger.getLogger(FOKMatcher.class.getName());

    @Override
    public boolean canHandle(Order buy, Order sell) {
        return buy.getTif() == TimeInForce.FOK || sell.getTif() == TimeInForce.FOK;
    }

    /**
     * Attempts to fully match a Fill-or-Kill (FOK) order. If not fully matched, the order is canceled.
     *
     * @param orderBook order book we work on
     */
    @Override
    public void match(OrderBook orderBook) {
        Order topBuy = orderBook.getBuyOrders().peek();
        Order topSell = orderBook.getSellOrders().peek();

        if (topBuy == null || topSell == null) return;

        Order fokOrder = (topBuy.getTif() == TimeInForce.FOK) ? topBuy : topSell;
        boolean isBuyFOK = fokOrder.getType() == OrderType.BUY;

        PriorityQueue<Order> counterQueue = isBuyFOK
                ? new PriorityQueue<>(orderBook.getSellOrders())  // clone to avoid modifying original
                : new PriorityQueue<>(orderBook.getBuyOrders());

        List<Order> candidates = new ArrayList<>();
        int totalAvailable = 0;

        // Scan matching candidates
        for (Order o : counterQueue) {
            boolean priceOk = isBuyFOK
                    ? fokOrder.getPrice() >= o.getPrice()
                    : fokOrder.getPrice() <= o.getPrice();

            if (!priceOk) break;

            candidates.add(o);
            totalAvailable += o.getQuantity();

            if (totalAvailable >= fokOrder.getQuantity()) break;
        }

        if (totalAvailable < fokOrder.getQuantity()) {
            logger.warning("FOK not possible, discarding order: " + fokOrder);
            orderBook.cancelOrder(fokOrder.getId());
            return;
        }

        // Remove from live queues
        Order main = isBuyFOK ? orderBook.getBuyOrders().poll() : orderBook.getSellOrders().poll();
        PriorityQueue<Order> liveCounterQueue = isBuyFOK ? orderBook.getSellOrders() : orderBook.getBuyOrders();
        for (Order c : candidates) liveCounterQueue.remove(c);

        int remaining = fokOrder.getQuantity();
        for (Order counter : candidates) {
            int tradeQty = Math.min(remaining, counter.getQuantity());

            long buyId = isBuyFOK ? main.getId() : counter.getId();
            long sellId = isBuyFOK ? counter.getId() : main.getId();

            orderBook.recordTrade(buyId, sellId, counter.getPrice(), tradeQty);
            logger.info("FOK partial fill: " + tradeQty + " units @ " + counter.getPrice());

            remaining -= tradeQty;
            counter.reduceQuantity(tradeQty);

            if (counter.getQuantity() > 0) {
                orderBook.addOrder(counter); // re-add partially matched
            }

            if (remaining == 0) break;
        }

        logger.info("FOK fully matched: " + fokOrder);
    }
}
