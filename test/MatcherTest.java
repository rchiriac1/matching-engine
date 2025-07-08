import engine.Order;
import engine.OrderBook;
import engine.OrderType;
import engine.TimeInForce;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for different matching types: FOK, IOC, and GTC.
 */
public class MatcherTest {

    /**
     * Tests that a Fill-or-Kill (FOK) order is only executed if it can be fully matched.
     * In this case, it is fully matched against available sell orders.
     */
    @Test
    void testFOKFullMatch() {
        OrderBook ob = new OrderBook();

        Order sell1 = new Order(OrderType.SELL, "AAPL", 100.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);
        Order sell2 = new Order(OrderType.SELL, "AAPL", 100.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);
        Order buyFOK = new Order(OrderType.BUY, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.FOK);

        ob.addOrder(sell1);
        ob.addOrder(sell2);
        ob.addOrder(buyFOK);

        ob.matchOrders();

        // All orders should be matched
        assertEquals(0, sell1.getQuantity() + sell2.getQuantity());
    }

    /**
     * Tests that an Immediate-or-Cancel (IOC) order is partially matched if possible
     * and the remaining quantity is discarded.
     */
    @Test
    void testIOCPartialMatchAndCancel() {
        OrderBook ob = new OrderBook();

        Order sell = new Order(OrderType.SELL, "AAPL", 100.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);
        Order buyIOC = new Order(OrderType.BUY, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.IOC);

        ob.addOrder(sell);
        ob.addOrder(buyIOC);

        ob.matchOrders();

        // Sell should be completely matched, buy order should be discarded after 5 matched
        assertEquals(0, sell.getQuantity());
    }

    /**
     * Tests that a Good-Till-Cancel (GTC) order is partially matched,
     * and the remaining part stays in the order book.
     */
    @Test
    void testGTCPartialMatchLeavesRemaining() {
        OrderBook ob = new OrderBook();

        Order sell = new Order(OrderType.SELL, "AAPL", 100.0, 6, Instant.now().plusSeconds(60), TimeInForce.GTC);
        Order buy = new Order(OrderType.BUY, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);

        ob.addOrder(sell);
        ob.addOrder(buy);

        ob.matchOrders();

        // Buy order should have 4 quantity remaining in the book
        assertTrue(buy.getQuantity() > 0);
        assertEquals(4, buy.getQuantity());
    }
}
