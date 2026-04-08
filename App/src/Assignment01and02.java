import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;

public class assignment01and02 {

    private final Map<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();
    private static final long MAX_LIMIT = 1000;
    private static final long REFILL_INTERVAL_SEC = 3600; // 1 hour

    /**
     * Internal class representing a client's bucket state.
     * Uses atomic operations to ensure thread safety under high concurrency.
     */
    class TokenBucket {
        final long capacity;
        final AtomicLong tokens;
        final AtomicLong lastRefillTimestamp;

        TokenBucket(long capacity) {
            this.capacity = capacity;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTimestamp = new AtomicLong(Instant.now().getEpochSecond());
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = Instant.now().getEpochSecond();
            long lastRefill = lastRefillTimestamp.get();

            if (now > lastRefill) {
                // If the reset interval (1 hour) has passed, reset tokens to full
                if (now - lastRefill >= REFILL_INTERVAL_SEC) {
                    tokens.set(capacity);
                    lastRefillTimestamp.set(now);
                }
            }
        }
    }

    /**
     * Primary check for API access
     */
    public String checkRateLimit(String clientId) {
        TokenBucket bucket = clientBuckets.computeIfAbsent(clientId, k -> new TokenBucket(MAX_LIMIT));

        if (bucket.tryConsume()) {
            return String.format("Allowed (%d requests remaining)", bucket.tokens.get());
        } else {
            long nextReset = bucket.lastRefillTimestamp.get() + REFILL_INTERVAL_SEC;
            long retryAfter = nextReset - Instant.now().getEpochSecond();
            return String.format("Denied (0 requests remaining, retry after %ds)", Math.max(0, retryAfter));
        }
    }

    /**
     * Returns the current status of a client's limit
     */
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) return "Client not initialized.";

        long used = MAX_LIMIT - bucket.tokens.get();
        long resetAt = bucket.lastRefillTimestamp.get() + REFILL_INTERVAL_SEC;

        return String.format("{used: %d, limit: %d, reset: %d}", used, MAX_LIMIT, resetAt);
    }

    public static void main(String[] args) {
        RateLimiterManager limiter = new RateLimiterManager();

        // Example Usage
        System.out.println(limiter.checkRateLimit("abc123")); // Allowed (999 remaining)
        System.out.println(limiter.checkRateLimit("abc123")); // Allowed (998 remaining)
        System.out.println(limiter.getRateLimitStatus("abc123"));
    }
}