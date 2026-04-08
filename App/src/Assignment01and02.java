import java.util.*;

public class Assignment01and02 {
    // Maps an n-gram to a set of Document IDs that contain it
    private final Map<String, Set<String>> ngramIndex = new HashMap<>();
    // Stores the total n-gram count for each document to calculate percentage
    private final Map<String, Integer> documentSizeMap = new HashMap<>();
    private final int N = 5; // Using 5-grams as suggested

    /**
     * Indexes a document into the system.
     */
    public void indexDocument(String docId, String content) {
        List<String> ngrams = extractNgrams(content);
        documentSizeMap.put(docId, ngrams.size());

        for (String gram : ngrams) {
            ngramIndex.computeIfAbsent(gram, k -> new HashSet<>()).add(docId);
        }
    }

    /**
     * Analyzes a new document against the indexed database.
     */
    public void analyzeDocument(String newDocContent) {
        List<String> newNgrams = extractNgrams(newDocContent);
        int totalNgrams = newNgrams.size();

        // Count matches per existing document
        Map<String, Integer> matchCounts = new HashMap<>();

        for (String gram : newNgrams) {
            if (ngramIndex.containsKey(gram)) {
                for (String existingDocId : ngramIndex.get(gram)) {
                    matchCounts.put(existingDocId, matchCounts.getOrDefault(existingDocId, 0) + 1);
                }
            }
        }

        System.out.println("Extracted " + totalNgrams + " n-grams");

        // Calculate and report similarity
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String docId = entry.getKey();
            int matches = entry.getValue();
            double similarity = (matches / (double) totalNgrams) * 100;

            String status = similarity > 50 ? "PLAGIARISM DETECTED" : (similarity > 10 ? "suspicious" : "clean");
            System.out.printf("→ Found %d matching n-grams with \"%s\"\n", matches, docId);
            System.out.printf("→ Similarity: %.1f%% (%s)\n", similarity, status);
        }
    }

    /**
     * Helper to break text into sequences of N words.
     */
    private List<String> extractNgrams(String text) {
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(j < N - 1 ? " " : "");
            }
            ngrams.add(sb.toString());
        }
        return ngrams;
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Indexing some sample "database" documents
        detector.indexDocument("essay_089.txt", "the quick brown fox jumps over the lazy dog often");
        detector.indexDocument("essay_092.txt", "the quick brown fox jumps over a very sleepy dog tonight");

        // Analyzing a new submission
        String newSubmission = "the quick brown fox jumps over the lazy dog and runs away";
        detector.analyzeDocument(newSubmission);
    }
}