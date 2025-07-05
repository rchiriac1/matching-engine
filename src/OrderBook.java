import java.time.Instant;
import java.util.*;


public class OrderBook {
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final HashMap<Long, Order> mapOrders = new HashMap<>();
    private final List<Trade> tradeHistory = new ArrayList<>();
    private final PriorityQueue<Order> orderExpiration;

    public OrderBook() {
        buyOrders = new PriorityQueue<>((o1, o2) -> {
            int priceCompare = Double.compare(o2.getPrice(), o1.getPrice()); // higher price first
            return priceCompare != 0 ? priceCompare : o1.getTimestamp().compareTo(o2.getTimestamp());
        });

        // lower price first
        sellOrders = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice).thenComparing(Order::getTimestamp));

        orderExpiration = new PriorityQueue<>(Comparator.comparing(Order::getExpiry));

    }

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

    public void printOrderBook() {
        // Print the contents of buyOrders and sellOrders
        System.out.println("Order Book, buy orders");
        System.out.println("------------------------------------");
        buyOrders.iterator().forEachRemaining(System.out::println);
        System.out.println("Order Book, sell orders");
        System.out.println("------------------------------------");
        sellOrders.iterator().forEachRemaining(System.out::println);
    }

    public void matchOrders() {
        // TODO: While top buy >= top sell, match orders
        while(!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            printOrderBook();
            System.out.println("Matching Orders");
            System.out.println("--------------------------");
            if(buyOrders.peek().getPrice() >= sellOrders.peek().getPrice()) {
                int minQuantity = Math.min(buyOrders.peek().getQuantity(), sellOrders.peek().getQuantity());

                if(buyOrders.peek().getTif() == TimeInForce.FOK || sellOrders.peek().getTif() == TimeInForce.FOK) {
                    if(buyOrders.peek().getTif() == TimeInForce.FOK){
                        addFOK(buyOrders.peek(), sellOrders);
                    }
                    else {
                        addFOK(sellOrders.peek(), buyOrders);
                    }
                }
                else if (sellOrders.peek().getTif() == TimeInForce.IOC || buyOrders.peek().getTif() == TimeInForce.IOC) {
                    addIOC(minQuantity);
                }
                else if (sellOrders.peek().getTif() == TimeInForce.GTC || buyOrders.peek().getTif() == TimeInForce.GTC) {
                    addGTC(minQuantity);
                }
            }
            else {
                break;
            }
        }
    }

    public void addFOK(Order toMatch, PriorityQueue<Order> counterOrders) {
        List<Order> candidates = new ArrayList<>();
        int totalQuantity = 0;

        // checking if we can match the fok order
        for (Order o : counterOrders) {
            boolean priceOk = toMatch.getType() == OrderType.BUY
                    ? toMatch.getPrice() >= o.getPrice()
                    : toMatch.getPrice() <= o.getPrice();

            if (priceOk) {
                candidates.add(o);
                totalQuantity += o.getQuantity();
                if (totalQuantity >= toMatch.getQuantity()) break;
            }
        }

        if (totalQuantity < toMatch.getQuantity()) {
            System.out.println("FOK not possible, discarding " + toMatch);
            cancelOrder(toMatch.getId());
            return;
        }

        // matching phase
        PriorityQueue<Order> liveQueue = (toMatch.getType() == OrderType.BUY) ? sellOrders : buyOrders;
        liveQueue.removeAll(candidates);
        Order mainOrder = (toMatch.getType() == OrderType.BUY) ? buyOrders.poll() : sellOrders.poll();
        int remaining = toMatch.getQuantity();

        for (Order counter : candidates) {
            int tradeQty = Math.min(remaining, counter.getQuantity());

            tradeHistory.add(new Trade(
                    toMatch.getType() == OrderType.BUY ? mainOrder.getId() : counter.getId(),
                    toMatch.getType() == OrderType.BUY ? counter.getId() : mainOrder.getId(),
                    counter.getPrice(),
                    tradeQty
            ));

            remaining -= tradeQty;
            counter.reduceQuantity(tradeQty);

            if (counter.getQuantity() > 0) {
                addOrder(counter); // re add partially matched orders
            }

            if (remaining == 0) break;
        }

        System.out.println("FOK fully matched: " + toMatch);
    }


    public void addIOC(int minQuantity) {
        Order buyOrder = buyOrders.poll();
        Order sellOrder = sellOrders.poll();

        buyOrder.reduceQuantity(minQuantity);
        sellOrder.reduceQuantity(minQuantity);

        cancelOrder(buyOrder.getId());
        cancelOrder(sellOrder.getId());


        System.out.println("Matched order: BUY " + minQuantity + " @ " + buyOrder.getPrice() +
                " between Order#" + buyOrder.getId() + " and Order#" + sellOrder.getId());
        tradeHistory.add(new Trade(buyOrder.getId(), sellOrder.getId(), sellOrder.getPrice(), minQuantity));
    }

    public void addGTC(int minQuantity) {
        Order buyOrder = buyOrders.poll();
        Order sellOrder = sellOrders.poll();

        buyOrder.reduceQuantity(minQuantity);
        sellOrder.reduceQuantity(minQuantity);


        if (sellOrder.getQuantity() > 0) {
            addOrder(sellOrder);
        }
        else {
            cancelOrder(sellOrder.getId());
        }
        if (buyOrder.getQuantity() > 0) {
            addOrder(buyOrder);
        }
        else {
            cancelOrder(buyOrder.getId());
        }

        System.out.println("Matched order: BUY " + minQuantity + " @ " + buyOrder.getPrice() +
                " between Order#" + buyOrder.getId() + " and Order#" + sellOrder.getId());
        tradeHistory.add(new Trade(buyOrder.getId(), sellOrder.getId(), sellOrder.getPrice(), minQuantity));
    }

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

    public void printTradeHistory() {
        System.out.println("Trade history: ");
        for(Trade trade : tradeHistory) {
            System.out.println("Trade: " + trade.getQuantity() + " @ " + trade.getPrice() +  "between BUY#" + trade.getBuyOrderId() + " and "
             + "SELL#"+ trade.getSellOrderId() + " at " + trade.getTimestamp());
        }
    }

    public boolean updateOrder(long orderId, double newPrice, int newQuantity) {
        Order updateOrder = mapOrders.get(orderId);
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

            if(type.equals(OrderType.BUY)){
                while(!found){
                    Order current = buyOrders.poll();
                    if(current.getId() == orderId){
                        pq.add(newOrder);
                        found = true;
                    }
                    else {
                        pq.add(current);
                    }
                }
                while(!pq.isEmpty()){
                    buyOrders.add(pq.poll());
                }
            }
            else {
                while(!found){
                    Order current = sellOrders.poll();
                    if(current.getId() == orderId){
                        pq.add(newOrder);
                        found = true;
                    }
                    else {
                        pq.add(current);
                    }
                }
                while(!pq.isEmpty()){
                    sellOrders.add(pq.poll());
                }
            }
            System.out.println("updated order: " + newOrder.toString());
            return true;
        }
        System.out.println("couldn't update order: " + updateOrder.toString());
        return false;
    }

    public void purgeExpiredOrders() {
        while(!orderExpiration.isEmpty()) {
            if(orderExpiration.peek().getExpiry().isBefore(Instant.now())) {
                Order expiredOrder = orderExpiration.poll();
                cancelOrder(expiredOrder.getId());

                System.out.println("Expired Order " + expiredOrder);
            }
        }
    }
}

