import org.junit.jupiter.api.Test;
import radu.engine.Order;
import radu.engine.OrderBook;
import radu.engine.OrderType;
import radu.engine.TimeInForce;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for core order book functionality.
 */
public class OrderBookTest {

    // ✅ 1. Test: Adding a Buy Order
    @Test
    void testAddBuyOrder() {
        OrderBook ob = new OrderBook();
        Order buyOrder = new Order(OrderType.BUY, "AAPL", 150.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);
        ob.addOrder(buyOrder);
        assertDoesNotThrow(ob::matchOrders);
    }

    // ✅ 2. Test: Matching GTC Orders Fully
    @Test
    void testMatchGTCOrders() {
        OrderBook ob = new OrderBook();
        Order buy = new Order(OrderType.BUY, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);
        Order sell = new Order(OrderType.SELL, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);
        ob.addOrder(buy);
        ob.addOrder(sell);
        ob.matchOrders();
        assertTrue(ob.getBuyOrders().isEmpty());
        assertTrue(ob.getSellOrders().isEmpty());
    }

    // ✅ 3. Test: GTC Partial Match (Buy > Sell)
    @Test
    void testGTCPartialMatch() {
        OrderBook ob = new OrderBook();
        Order buy = new Order(OrderType.BUY, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);
        Order sell = new Order(OrderType.SELL, "AAPL", 100.0, 4, Instant.now().plusSeconds(60), TimeInForce.GTC);
        ob.addOrder(buy);
        ob.addOrder(sell);
        ob.matchOrders();

        assertEquals(6, ob.getBuyOrders().peek().getQuantity());
        assertTrue(ob.getSellOrders().isEmpty());
    }

    // ✅ 4. Test: FOK Order Not Matched
    @Test
    void testFOKNotMatched() {
        OrderBook ob = new OrderBook();
        Order fokBuy = new Order(OrderType.BUY, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.FOK);
        Order sell = new Order(OrderType.SELL, "AAPL", 100.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);
        ob.addOrder(fokBuy);
        ob.addOrder(sell);
        ob.matchOrders();
        assertTrue(ob.getBuyOrders().isEmpty());
        assertFalse(ob.getSellOrders().isEmpty());
    }

    // ✅ 5. Test: Order Update
    @Test
    void testUpdateOrder() {
        OrderBook ob = new OrderBook();
        Order buy = new Order(OrderType.BUY, "AAPL", 100.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);
        ob.addOrder(buy);
        boolean updated = ob.updateOrder(buy.getId(), 110.0, 10);
        assertTrue(updated);
        assertEquals(110.0, ob.getBuyOrders().peek().getPrice());
        assertEquals(10, ob.getBuyOrders().peek().getQuantity());
    }

    // ✅ 6. Test: Cancel Order by ID
    @Test
    void testCancelOrder() {
        OrderBook ob = new OrderBook();
        Order sell = new Order(OrderType.SELL, "AAPL", 100.0, 8, Instant.now().plusSeconds(60), TimeInForce.GTC);
        ob.addOrder(sell);
        boolean cancelled = ob.cancelOrder(sell.getId());
        assertTrue(cancelled);
        assertTrue(ob.getSellOrders().isEmpty());
    }

    // ✅ 7. Test: Purge Expired Orders
    @Test
    void testPurgeExpiredOrder() {
        OrderBook ob = new OrderBook();
        Order expired = new Order(OrderType.SELL, "AAPL", 100.0, 5, Instant.now().minusSeconds(5), TimeInForce.GTC);
        ob.addOrder(expired);
        ob.purgeExpiredOrders();
        assertTrue(ob.getSellOrders().isEmpty());
    }
}
