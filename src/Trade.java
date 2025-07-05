import java.time.Instant;
import java.util.Comparator;
import java.util.TreeSet;

public class Trade {

    private final long buyOrderId;
    private final long sellOrderId;
    private final double price;
    private int quantity;
    private final Instant timestamp;

    public Trade(long buyOrderId, long sellOrderId, double price, int quantity) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }

    public long getBuyOrderId() {
        return buyOrderId;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public double[] medianSlidingWindow(int[] nums, int k) {
        double[] ans = new double[nums.length - k + 1];
        TreeSet<Integer> low = new TreeSet<>((a, b) -> a == b ? Integer.compare(a, b) : Integer.compare(b, a)); // Max-heap
        TreeSet<Integer> high = new TreeSet<>(); // Min-heap
        int j = 0;

        for (int i = 0; i < nums.length; i++) {
            // Insert number
            if (low.isEmpty() || nums[i] <= low.first()) {
                low.add(nums[i]);
            } else {
                high.add(nums[i]);
            }

            // Rebalance sizes
            while (low.size() > high.size() + 1) {
                high.add(low.pollFirst());
            }
            while (high.size() > low.size()) {
                low.add(high.pollFirst());
            }

            // When window is fully formed
            if (i >= k - 1) {
                // Compute median
                if (k % 2 == 0) {
                    ans[j++] = ((double) low.first() + high.first()) / 2.0;
                } else {
                    ans[j++] = low.first();
                }

                // Remove the element going out of the window
                int toRemove = nums[i - k + 1];
                if (low.contains(toRemove)) {
                    low.remove(toRemove);
                } else {
                    high.remove(toRemove);
                }

                // Rebalance again after removal
                while (low.size() > high.size() + 1) {
                    high.add(low.pollFirst());
                }
                while (high.size() > low.size()) {
                    low.add(high.pollFirst());
                }
            }
        }

        return ans;
    }

}

//nums = [1, 3, -1, -3, 5, 3, 6, 7]
//k = 3
//
//Output:
//[1.0, -1.0, -1.0, 3.0, 5.0, 6.0]
