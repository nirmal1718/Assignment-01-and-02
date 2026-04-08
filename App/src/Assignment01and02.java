import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Assignment01and02 {
    // Thread-safe maps for high concurrency (1000+ checks/sec)
    private final Map<String, Integer> userDatabase = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> attemptTracker = new ConcurrentHashMap<>();

    // FIX: Constructor must match the class name "Assignment01and02"
    public Assignment01and02() {
        // Pre-populating some data for the scenario
        userDatabase.put("john_doe", 101);
        userDatabase.put("alex_pro", 102);
    }

    /**
     * Checks availability in O(1) time.
     */
    public boolean checkAvailability(String username) {
        String normalized = username.toLowerCase(); // Ensure case-insensitivity

        // Increment attempt counter
        attemptTracker.computeIfAbsent(normalized, k -> new AtomicInteger(0))
                .incrementAndGet();

        // Key-value mapping lookup
        return !userDatabase.containsKey(normalized);
    }

    /**
     * Suggests alternatives by appending numbers or modifying characters.
     */
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int suffix = 1;

        while (suggestions.size() < 3) {
            String candidate = username + suffix;
            if (!userDatabase.containsKey(candidate.toLowerCase())) {
                suggestions.add(candidate);
            }
            suffix++;
        }

        // Logic to add a dot variation
        if (username.length() > 1) {
            String dotVariation = username.substring(0, 1) + "." + username.substring(1);
            if (!userDatabase.containsKey(dotVariation.toLowerCase())) {
                suggestions.add(dotVariation);
            }
        }

        return suggestions;
    }

    /**
     * Returns the most frequently attempted username.
     */
    public String getMostAttempted() {
        return attemptTracker.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().get()))
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    public static void main(String[] args) {
        // FIX: Instantiate the correct class name
        Assignment01and02 sys = new Assignment01and02();

        System.out.println("Is 'john_doe' available? " + sys.checkAvailability("john_doe"));
        System.out.println("Is 'jane_smith' available? " + sys.checkAvailability("jane_smith"));

        if (!sys.checkAvailability("john_doe")) {
            System.out.println("Suggestions for 'john_doe': " + sys.suggestAlternatives("john_doe"));
        }

        System.out.println("Most attempted: " + sys.getMostAttempted());
    }
}