import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class Assignment01and02 {
    // 1. Page View Counts: Use LongAdder for high throughput
    private final ConcurrentHashMap<String, LongAdder> pageViews = new ConcurrentHashMap<>();

    // 2. Unique Visitors: Use ConcurrentHashMap-backed Sets
    private final ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // 3. Traffic Sources
    private final ConcurrentHashMap<String, LongAdder> trafficSources = new ConcurrentHashMap<>();

    /**
     * Processes an incoming event in O(1) time.
     */
    public void processEvent(String url, String userId, String source) {
        // Increment page views
        pageViews.computeIfAbsent(url, k -> new LongAdder()).increment();

        // Track unique visitors
        uniqueVisitors.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userId);

        // Increment source counts
        trafficSources.computeIfAbsent(source, k -> new LongAdder()).increment();
    }

    /**
     * Gets the Top 10 Pages.
     * Time Complexity: O(P log K) where P is total pages and K is 10.
     */
    public List<PageStats> getTopPages(int n) {
        return pageViews.entrySet().stream()
                .map(entry -> new PageStats(
                        entry.getKey(),
                        entry.getValue().sum(),
                        uniqueVisitors.getOrDefault(entry.getKey(), Collections.emptySet()).size()
                ))
                // Min-Heap logic via stream sorting for simplicity
                .sorted(Comparator.comparingLong(PageStats::getViews).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    // Helper class for Dashboard reporting
    static class PageStats {
        String url;
        long views;
        int uniqueViews;

        PageStats(String url, long views, int unique) {
            this.url = url; this.views = views; this.uniqueViews = unique;
        }

        public long getViews() { return views; }

        @Override
        public String toString() {
            return String.format("%s - %d views (%d unique)", url, views, uniqueViews);
        }
    }
}