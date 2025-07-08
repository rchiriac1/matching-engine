package engine;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
public class Order {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1); //  why atomic long

    private final long id;
    private final OrderType type;
    private final String symbol;
    private final double price;
    private int quantity;
    private final Instant timestamp;
    private final Instant expiry;
    private final TimeInForce tif;

    public Order(OrderType type, String symbol, double price, int quantity, Instant expiry, TimeInForce tif) {
        this.expiry = expiry;
        this.tif = tif;
        this.id = ID_GENERATOR.getAndIncrement();
        this.type = type;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }

    public Order(long id, OrderType type, String symbol, double price, int quantity, Instant timestamp, Instant expiry, TimeInForce tif) {
        this.id = id;
        this.type = type;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.expiry = expiry;
        this.tif = tif;
    }

    public TimeInForce getTif() {
        return tif;
    }
    public Instant getExpiry() {
        return expiry;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getId() {
        return id;
    }

    public OrderType getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void reduceQuantity(int amount) {
        if(amount > quantity || amount <= 0)  {
            throw new IllegalArgumentException("Invalid quantity: " + amount);
        }
        this.quantity -= amount;
    }

    @Override
    public String toString() {
        return "engine.Order{" +
                "id=" + id +
                ", type=" + type +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                '}';
    }
}
