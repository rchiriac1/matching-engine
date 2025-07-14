package radu.engine;

import lombok.Getter;
import radu.matching.FOKMatcher;
import radu.matching.GTCMatcher;
import radu.matching.IOCMatcher;
import radu.matching.OrderMatchingStrategy;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;


/**
 * Represents an order book that handles the addition, com.matching, expiration,
 * updating, and cancellation of buy and sell orders for a given security.
 * It supports GTC, FOK, and IOC order types, and maintains trade history.
 */
public class OrderBook {

    private static final Logger logger = Logger.getLogger(OrderBook.class.getName());
    /**
     * -- GETTER --
     *  Gets buy orders PQ
     *
     * @return buy orders
     */
    @Getter
    private final PriorityQueue<Order> buyOrders;
    /**
     * -- GETTER --
     *  Gets sell orders PQ
     *
     * @return sell orders
     */
    @Getter
    private final PriorityQueue<Order> sellOrders;
    private final HashMap<Long, Order> mapOrders = new HashMap<>();
    private final List<Trade> tradeHistory = new ArrayList<>();
    private final PriorityQueue<Order> orderExpiration;
    private final List<OrderMatchingStrategy> strategies = List.of(
            new FOKMatcher(),
            new IOCMatcher(),
            new GTCMatcher()
    );

    /**
     * Constructs a new empty com.engine.OrderBook.
     * Initializes the priority queues for buy and sell orders and expiration tracking.
     */
    public OrderBook() {
        buyOrders = new PriorityQueue<>((o1, o2) -> {
            int priceCompare = Double.compare(o2.getPrice(), o1.getPrice()); // higher price first
            return priceCompare != 0 ? priceCompare : o1.getTimestamp().compareTo(o2.getTimestamp());
        });

        // lower price first
        sellOrders = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice).thenComparing(Order::getTimestamp));

        orderExpiration = new PriorityQueue<>(Comparator.comparing(Order::getExpiry));

    }

    /**
     * Adds an order to the order book.
     *
     * @param order the order to add
     * @throws IllegalArgumentException if the order type is invalid
     */
    public void addOrder(Order order) {
        orderExpiration.add(order);
        if(order.getType() == OrderType.BUY) {
            buyOrders.add(order);
            mapOrders.put(order.getId(), order);
        }
        else if(order.getType() == OrderType.SELL) {
            sellOrders.add(order);
            mapOrders.put(order.getId(), order);
        }
        else {
            throw new IllegalArgumentException("Invalid order type");
        }
    }

    /**
     * Prints the current state of the order book, including buy and sell orders.
     */
    public void printOrderBook() {
        // Print the contents of buyOrders and sellOrders
        System.out.println("com.engine.Order Book, buy orders");
        System.out.println("------------------------------------");
        buyOrders.iterator().forEachRemaining(System.out::println);
        System.out.println("com.engine.Order Book, sell orders");
        System.out.println("------------------------------------");
        sellOrders.iterator().forEachRemaining(System.out::println);
    }

    /**
     * Matches orders in the order book while the best buy order price is greater than or equal to
     * the best sell order price, following TIF (Time-In-Force) constraints.
     */
    public void matchOrders() {
        logger.info("Matching Orders");
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buy = buyOrders.peek();
            Order sell = sellOrders.peek();

            if (buy.getPrice() < sell.getPrice()) break;

            for (OrderMatchingStrategy strategy : strategies) {
                if (strategy.canHandle(buy, sell)) {
                    strategy.match(this);
                    break;
                }
            }
        }
    }

    public void recordTrade(long buyId, long sellId, double price, int quantity) {
        System.out.println("Matched order: BUY " + quantity + " @ " + price +
                " between com.engine.Order#" + mapOrders.get(buyId).getId() + " and com.engine.Order#" + mapOrders.get(sellId).getId());
        tradeHistory.add(new Trade(buyId, sellId, price, quantity));
    }

    /**
     * Cancels an order by ID.
     *
     * @param orderId the ID of the order to cancel
     * @return true if the order was found and canceled, false otherwise
     */
    public boolean cancelOrder(long orderId) {
        Order order = mapOrders.get(orderId);
        if(order != null) {
            if(order.getType() == OrderType.BUY) {
                buyOrders.remove(order);
            }
            else {
                sellOrders.remove(order);
            }
            mapOrders.remove(orderId);
            orderExpiration.remove(order);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Prints all trades that have occurred in the order book.
     */
    public void printTradeHistory() {
        System.out.println("com.engine.Trade history: ");
        for(Trade trade : tradeHistory) {
            logger.fine("Trade: " + trade);
        }
    }

    /**
     * Updates the price and quantity of an existing order.
     *
     * @param orderId     the ID of the order to update
     * @param newPrice    the new price
     * @param newQuantity the new quantity
     * @return true if the update was successful, false otherwise
     */
    public boolean updateOrder(long orderId, double newPrice, int newQuantity) {
        Order updateOrder = mapOrders.get(orderId);
        // copy pq to insert elements so we can add them back while we search for our orderId
        PriorityQueue<Order> pq;
        if(updateOrder.getType() == OrderType.BUY) {
            pq = new PriorityQueue<>(buyOrders.comparator());
        }
        else {
            pq = new PriorityQueue<>(sellOrders.comparator());
        }
        boolean found = false;
        if(updateOrder != null){
            Order newOrder = new Order(orderId, updateOrder.getType(), updateOrder.getSymbol(), newPrice,
                    newQuantity, Instant.now(), updateOrder.getExpiry(), updateOrder.getTif());
            OrderType type = updateOrder.getType();
            mapOrders.replace(orderId, updateOrder, newOrder);

            orderExpiration.remove(updateOrder);
            orderExpiration.add(newOrder);

            PriorityQueue<Order> sameTypeOrders;
            if(type.equals(OrderType.BUY)) {
                sameTypeOrders = buyOrders;
            }
            else {
                sameTypeOrders = sellOrders;
            }

            // go through the queue until you find the order to update
            while(!found){
                    Order current = sameTypeOrders.poll();
                    if(current.getId() == orderId){
                        pq.add(newOrder);
                        found = true;
                    }
                    else {
                        pq.add(current);
                    }
                }
            // reinsert into the original priority queue
            while(!pq.isEmpty()){
                sameTypeOrders.add(pq.poll());
            }
            logger.info("Updated order: " + newOrder);
            return true;
        }
        logger.warning("Couldn't update order: " + orderId);
        return false;
    }

    /**
     * Removes all orders that have expired based on their expiry timestamp.
     */
    public void purgeExpiredOrders() {
        while(!orderExpiration.isEmpty()) {
            if(orderExpiration.peek().getExpiry().isBefore(Instant.now())) {
                Order expiredOrder = orderExpiration.poll();
                cancelOrder(expiredOrder.getId());

                logger.info("Expired Order: " + expiredOrder);
            }
        }
    }
}

