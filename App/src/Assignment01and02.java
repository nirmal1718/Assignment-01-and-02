import java.util.*;

class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

public class MultiLevelCacheSystem {
    // Configuration Constants
    private final int L1_CAPACITY = 10000;
    private final int L2_CAPACITY = 100000;
    private final int PROMOTION_THRESHOLD = 3;

    // Cache Tiers
    private final LinkedHashMap<String, VideoData> l1Cache; // In-memory
    private final LinkedHashMap<String, String> l2Cache;    // SSD-backed (Mapping ID to FilePath)
    private final Map<String, Integer> accessTracker;       // Tracks frequency for promotion

    // Metrics
    private double l1Hits = 0, l2Hits = 0, l3Hits = 0, totalRequests = 0;

    public MultiLevelCacheSystem() {
        // LinkedHashMap with accessOrder = true for LRU eviction
        this.l1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > L1_CAPACITY;
            }
        };

        this.l2Cache = new LinkedHashMap<>(L2_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > L2_CAPACITY;
            }
        };

        this.accessTracker = new HashMap<>();
    }

    public VideoData getVideo(String videoId) {
        totalRequests++;

        // 1. Check L1 Cache (Memory)
        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            System.out.println("-> L1 Cache HIT (0.5ms)");
            return l1Cache.get(videoId);
        }

        // 2. Check L2 Cache (SSD)
        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            System.out.println("-> L1 Cache MISS (0.5ms)");
            System.out.println("-> L2 Cache HIT (5ms)");

            VideoData data = fetchFromSSD(videoId);
            updateAccessAndPromote(videoId, data);
            return data;
        }

        // 3. Check L3 (Database)
        l3Hits++;
        System.out.println("-> L1 Cache MISS");
        System.out.println("-> L2 Cache MISS");
        System.out.println("-> L3 Database HIT (150ms)");

        VideoData data = fetchFromDatabase(videoId);

        // New data always enters L2 first
        l2Cache.put(videoId, "SSD_PATH_" + videoId);
        accessTracker.put(videoId, 1);

        return data;
    }

    private void updateAccessAndPromote(String videoId, VideoData data) {
        int count = accessTracker.getOrDefault(videoId, 0) + 1;
        accessTracker.put(videoId, count);

        // Promotion Logic: L2 -> L1
        if (count >= PROMOTION_THRESHOLD) {
            System.out.println("-> Promoted to L1");
            l1Cache.put(videoId, data);
            l2Cache.remove(videoId);
        }
    }

    private VideoData fetchFromSSD(String videoId) {
        return new VideoData(videoId, "Content from SSD");
    }

    private VideoData fetchFromDatabase(String videoId) {
        return new VideoData(videoId, "Content from DB");
    }

    public void invalidate(String videoId) {
        l1Cache.remove(videoId);
        l2Cache.remove(videoId);
        accessTracker.remove(videoId);
        System.out.println("Invalidated: " + videoId);
    }

    public void getStatistics() {
        double avgTime = (l1Hits * 0.5 + l2Hits * 5.0 + l3Hits * 150.0) / totalRequests;
        System.out.println("\n--- Cache Statistics ---");
        System.out.printf("L1 Hit Rate: %.1f%%\n", (l1Hits / totalRequests) * 100);
        System.out.printf("L2 Hit Rate: %.1f%%\n", (l2Hits / totalRequests) * 100);
        System.out.printf("L3 Hit Rate: %.1f%%\n", (l3Hits / totalRequests) * 100);
        System.out.printf("Overall Avg Latency: %.2fms\n", avgTime);
    }

    public static void main(String[] args) {
        MultiLevelCacheSystem netflixCache = new MultiLevelCacheSystem();

        // Scenario 1: First access (DB Hit)
        netflixCache.getVideo("video_123");

        // Scenario 2: Multiple accesses to trigger promotion
        netflixCache.getVideo("video_123"); // Hit L2
        netflixCache.getVideo("video_123"); // Hit L2 -> Promotes to L1

        // Scenario 3: Accessing promoted video
        netflixCache.getVideo("video_123"); // Hit L1

        netflixCache.getStatistics();
    }
}