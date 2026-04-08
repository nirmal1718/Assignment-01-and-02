import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime; // System.currentTimeMillis() + TTL

    DNSEntry(String domain, String ipAddress, long ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

public class Assignment01and02 {
    private final int capacity;
    private final Map<String, DNSEntry> cache;
    private final LinkedList<String> lruOrder;
    private final ReentrantLock lock = new ReentrantLock();

    // Metrics
    private long hits = 0;
    private long misses = 0;
    private long totalLookupTimeNs = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.lruOrder = new LinkedList<>();

        // Background thread to clean expired entries every 10 seconds
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(this::cleanupExpired, 10, 10, TimeUnit.SECONDS);
    }

    public String resolve(String domain) {
        long startTime = System.nanoTime();
        lock.lock();
        try {
            if (cache.containsKey(domain)) {
                DNSEntry entry = cache.get(domain);

                if (!entry.isExpired()) {
                    // Cache HIT
                    hits++;
                    updateLRU(domain);
                    recordTime(startTime);
                    System.out.println("resolve(\"" + domain + "\") -> Cache HIT -> " + entry.ipAddress);
                    return entry.ipAddress;
                } else {
                    // Cache EXPIRED
                    System.out.print("resolve(\"" + domain + "\") -> Cache EXPIRED -> ");
                    removeEntry(domain);
                }
            } else {
                // Cache MISS
                System.out.print("resolve(\"" + domain + "\") -> Cache MISS -> ");
            }

            // Simulate Upstream Query (100ms delay)
            misses++;
            String ip = queryUpstream(domain);
            put(domain, ip, 300); // Default 300s TTL
            recordTime(startTime);
            System.out.println("Query upstream -> " + ip);
            return ip;

        } finally {
            lock.unlock();
        }
    }

    private void put(String domain, String ip, long ttl) {
        if (cache.size() >= capacity) {
            String oldest = lruOrder.removeLast();
            cache.remove(oldest);
        }
        DNSEntry newEntry = new DNSEntry(domain, ip, ttl);
        cache.put(domain, newEntry);
        lruOrder.addFirst(domain);
    }

    private void updateLRU(String domain) {
        lruOrder.remove(domain);
        lruOrder.addFirst(domain);
    }

    private void removeEntry(String domain) {
        cache.remove(domain);
        lruOrder.remove(domain);
    }

    private String queryUpstream(String domain) {
        // Mock upstream DNS resolution
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        return "172.217.14." + (new Random().nextInt(255));
    }

    private void recordTime(long startNs) {
        totalLookupTimeNs += (System.nanoTime() - startNs);
    }

    public void cleanupExpired() {
        lock.lock();
        try {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            lruOrder.removeIf(domain -> !cache.containsKey(domain));
        } finally {
            lock.unlock();
        }
    }

    public void getCacheStats() {
        double hitRate = (hits + misses == 0) ? 0 : (double) hits / (hits + misses) * 100;
        double avgTimeMs = (hits + misses == 0) ? 0 : (totalLookupTimeNs / 1_000_000.0) / (hits + misses);
        System.out.printf("Stats -> Hit Rate: %.1f%%, Avg Lookup Time: %.2fms%n", hitRate, avgTimeMs);
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCache dns = new DNSCache(5);

        dns.resolve("google.com"); // Miss
        dns.resolve("google.com"); // Hit
        dns.resolve("openai.com"); // Miss
        dns.getCacheStats();
    }
}