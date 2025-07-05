import java.util.HashMap;
import java.util.Map;

public class MatchingEngine {
    private final Map<String, OrderBook> books = new HashMap<>();

    public void addOrderBook(String symbol, OrderBook newOrderBook) {
        OrderBook orderBook = books.get(symbol);
        if (orderBook == null) {
            books.put(symbol, newOrderBook);
        }
    }

    public void addOrder(String symbol, Order order) {
        OrderBook orderBook = books.get(symbol);
        if (orderBook == null) {
            addOrderBook(symbol, new OrderBook());
            orderBook = books.get(symbol);
        }
        orderBook.addOrder(order);
        // books.computeIfAbsent(symbol, k -> new OrderBook()).addOrder(order);
    }

    public void cancelOrder(String symbol, long orderId) {
        OrderBook orderBook = books.get(symbol);
        if (orderBook != null) {
            orderBook.cancelOrder(orderId);
        }
    }

    public void match(String symbol) {
        OrderBook orderBook = books.get(symbol);
        if (orderBook != null) {
            orderBook.matchOrders();
        }
    }

    public void printOrderBook(String symbol) {
        OrderBook orderBook = books.get(symbol);
        if (orderBook != null) {
            orderBook.printOrderBook();
        }
    }

    public void printTradeHistory(String symbol) {
        OrderBook orderBook = books.get(symbol);
        if (orderBook != null) {
            orderBook.printTradeHistory();
        }
    }

    public boolean updateOrder(String symbol, long orderId, double newPrice, int newQuantity) {
        OrderBook orderBook = books.get(symbol);
        if(orderBook != null) {
            orderBook.updateOrder(orderId, newPrice, newQuantity);
            return true;
        }
        return false;
    }

}
