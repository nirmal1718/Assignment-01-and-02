import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Assignment01and02 {
    // Stores Product ID -> Current Stock
    private final ConcurrentHashMap<String, AtomicInteger> inventory = new ConcurrentHashMap<>();

    // Stores Product ID -> Queue of User IDs (FIFO Waiting List)
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> waitingLists = new ConcurrentHashMap<>();

    /**
     * Initializes a product in the system.
     */
    public void addProduct(String productId, int initialStock) {
        inventory.put(productId, new AtomicInteger(initialStock));
        waitingLists.put(productId, new ConcurrentLinkedQueue<>());
    }

    /**
     * Checks current stock in O(1)
     */
    public int checkStock(String productId) {
        AtomicInteger stock = inventory.get(productId);
        return (stock != null) ? stock.get() : 0;
    }

    /**
     * Processes purchase with atomic operations to prevent overselling.
     */
    public String purchaseItem(String productId, long userId) {
        AtomicInteger stock = inventory.get(productId);

        if (stock == null) return "Product not found.";

        // Atomic decrement and get: equivalent to check-and-set in one CPU cycle
        // We only proceed if the value was > 0 before decrementing
        while (true) {
            int currentStock = stock.get();
            if (currentStock <= 0) {
                addToWaitingList(productId, userId);
                int position = getWaitingListPosition(productId, userId);
                return "Added to waiting list, position #" + position;
            }

            // compareAndSet ensures no other thread changed the stock between our get() and set()
            if (stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success, " + (currentStock - 1) + " units remaining";
            }
        }
    }

    private void addToWaitingList(String productId, long userId) {
        ConcurrentLinkedQueue<Long> queue = waitingLists.get(productId);
        if (!queue.contains(userId)) {
            queue.add(userId);
        }
    }

    private int getWaitingListPosition(String productId, long userId) {
        ConcurrentLinkedQueue<Long> queue = waitingLists.get(productId);
        int pos = 1;
        for (Long id : queue) {
            if (id == userId) return pos;
            pos++;
        }
        return pos;
    }

    public static void main(String[] args) {
        FlashSaleManager manager = new FlashSaleManager();
        String product = "IPHONE15_256GB";
        manager.addProduct(product, 2); // Small stock for demo

        System.out.println(manager.purchaseItem(product, 12345)); // Success
        System.out.println(manager.purchaseItem(product, 67890)); // Success
        System.out.println(manager.purchaseItem(product, 99999)); // Waiting List
    }
}