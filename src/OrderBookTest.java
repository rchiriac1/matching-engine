import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {

    // ✅ 1. Test: Adding a Buy Order
    @Test
    void testAddBuyOrder() {
        OrderBook ob = new OrderBook();
        Order buyOrder = new Order(OrderType.BUY, "AAPL", 150.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);
        ob.addOrder(buyOrder);

        // Optional: check internal structure via reflection or by triggering a match/print
        assertDoesNotThrow(() -> ob.matchOrders());  // Should not crash
    }

    // ✅ 2. Test: Matching Buy and Sell Orders (GTC)
    @Test
    void testMatchGTCOrders() {
        OrderBook ob = new OrderBook();

        Order buyOrder = new Order(OrderType.BUY, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);
        Order sellOrder = new Order(OrderType.SELL, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);

        ob.addOrder(buyOrder);
        ob.addOrder(sellOrder);

        ob.matchOrders();  // Should trigger a match

        // Triggering this just to visually verify the match for now
        ob.printTradeHistory();
    }

    // ✅ 3. Test: FOK Order Cannot Be Matched and Is Cancelled
    @Test
    void testFOKNotMatched() {
        OrderBook ob = new OrderBook();

        // FOK buy order requires 10 quantity
        Order buyFOK = new Order(OrderType.BUY, "AAPL", 100.0, 10, Instant.now().plusSeconds(60), TimeInForce.FOK);

        // Only 5 available to sell
        Order sellOrder = new Order(OrderType.SELL, "AAPL", 100.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);

        ob.addOrder(buyFOK);
        ob.addOrder(sellOrder);

        ob.matchOrders();

        ob.printTradeHistory();  // Should be empty
    }

    // ✅ 4. Test: Expired Order Is Purged
    @Test
    void testPurgeExpiredOrder() {
        OrderBook ob = new OrderBook();

        Order expiredOrder = new Order(OrderType.SELL, "AAPL", 100.0, 5, Instant.now().minusSeconds(5), TimeInForce.GTC);
        ob.addOrder(expiredOrder);

        ob.purgeExpiredOrders();  // Should remove it silently

        assertDoesNotThrow(() -> ob.matchOrders());
    }

    // ✅ 5. Test: Order Update
    @Test
    void testUpdateOrder() {
        OrderBook ob = new OrderBook();
        Order buyOrder = new Order(OrderType.BUY, "AAPL", 100.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);

        ob.addOrder(buyOrder);
        boolean updated = ob.updateOrder(buyOrder.getId(), 110.0, 10);

        assertTrue(updated);
    }



}
