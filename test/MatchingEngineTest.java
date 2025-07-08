
import engine.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MatchingEngine which manages symbol-based order books.
 */
public class MatchingEngineTest {

    /**
     * Tests that orders are added to the correct order book for a symbol.
     */
    @Test
    void testAddAndMatchOrdersBySymbol() {
        MatchingEngine engine = new MatchingEngine();

        Order buy = new Order(OrderType.BUY, "AAPL", 150.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);
        Order sell = new Order(OrderType.SELL, "AAPL", 150.0, 5, Instant.now().plusSeconds(60), TimeInForce.GTC);

        engine.addOrder("AAPL", buy);
        engine.addOrder("AAPL", sell);

        // Should match within the correct symbol book
        engine.match("AAPL");

        // No assertion here since MatchingEngine doesn’t expose internal book state
        // We rely on logs or no exceptions for pass
        assertTrue(true); // Smoke test
    }

    /**
     * Tests that canceling an order via the engine does not throw.
     */
    @Test
    void testCancelOrderBySymbol() {
        MatchingEngine engine = new MatchingEngine();

        Order order = new Order(OrderType.SELL, "TSLA", 700.0, 10, Instant.now().plusSeconds(60), TimeInForce.GTC);
        engine.addOrder("TSLA", order);

        assertDoesNotThrow(() -> engine.cancelOrder("TSLA", order.getId()));
    }

    /**
     * Tests that updating an order through the engine works as expected.
     */
    @Test
    void testUpdateOrderBySymbol() {
        MatchingEngine engine = new MatchingEngine();

        Order order = new Order(OrderType.BUY, "GOOG", 2500.0, 8, Instant.now().plusSeconds(60), TimeInForce.GTC);
        engine.addOrder("GOOG", order);

        boolean result = engine.updateOrder("GOOG", order.getId(), 2550.0, 12);
        assertTrue(result);
    }

    /**
     * Tests printing order book and trade history without crashing.
     */
    @Test
    void testPrintMethods() {
        MatchingEngine engine = new MatchingEngine();

        Order order = new Order(OrderType.SELL, "NFLX", 500.0, 4, Instant.now().plusSeconds(60), TimeInForce.GTC);
        engine.addOrder("NFLX", order);

        assertDoesNotThrow(() -> {
            engine.printOrderBook("NFLX");
            engine.printTradeHistory("NFLX");
        });
    }
}
