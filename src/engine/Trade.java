package engine;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Represents a trade executed between a buy order and a sell order.
 * Stores the IDs of the involved orders, the trade price, quantity, and timestamp.
 */
public class Trade {

    private final long buyOrderId;
    private final long sellOrderId;
    private final double price;
    private final int quantity;
    private final Instant timestamp;

    /**
     * Constructs a Trade object with the given details.
     *
     * @param buyOrderId  ID of the buy order
     * @param sellOrderId ID of the sell order
     * @param price       execution price
     * @param quantity    quantity traded
     */
    public Trade(long buyOrderId, long sellOrderId, double price, int quantity) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }

    /**
     * @return the ID of the buy order
     */
    public long getBuyOrderId() {
        return buyOrderId;
    }

    /**
     * @return the ID of the sell order
     */
    public long getSellOrderId() {
        return sellOrderId;
    }

    /**
     * @return the price at which the trade was executed
     */
    public double getPrice() {
        return price;
    }

    /**
     * @return the quantity traded
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return the timestamp of the trade
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a formatted string representation of the trade,
     * useful for logging and display purposes.
     *
     * @return a human-readable trade summary
     */
    @Override
    public String toString() {
        return String.format(
                "Trade[BUY#%d, SELL#%d, Qty=%d @ %.2f, Time=%s]",
                buyOrderId,
                sellOrderId,
                quantity,
                price,
                DateTimeFormatter.ISO_INSTANT.format(timestamp)
        );
    }
}


