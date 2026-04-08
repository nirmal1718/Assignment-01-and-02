import java.util.*;

class TrieNode {
    // Map character to child node for space efficiency
    Map<Character, TrieNode> children = new HashMap<>();

    // Pre-computed top 10 queries passing through this node
    // Using a list of a custom inner class for ranking
    List<QueryInfo> topTen = new ArrayList<>();
}

class QueryInfo implements Comparable<QueryInfo> {
    String query;
    int frequency;

    QueryInfo(String query, int frequency) {
        this.query = query;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(QueryInfo other) {
        // Sort by frequency descending, then alphabetically ascending
        if (this.frequency != other.frequency) {
            return Integer.compare(other.frequency, this.frequency);
        }
        return this.query.compareTo(other.query);
    }
}

public class Assignment01and02 {
    private final TrieNode root;
    private final Map<String, Integer> globalFreq;

    public AutocompleteSystem() {
        this.root = new TrieNode();
        this.globalFreq = new HashMap<>();
    }

    /**
     * Updates the frequency of a query and refreshes the Trie path.
     * Time Complexity: O(L * K log K) where L is query length and K is 10.
     */
    public void updateFrequency(String query, int delta) {
        int newFreq = globalFreq.getOrDefault(query, 0) + delta;
        globalFreq.put(query, newFreq);

        TrieNode curr = root;
        for (char c : query.toCharArray()) {
            curr.children.putIfAbsent(c, new TrieNode());
            curr = curr.children.get(c);
            updateTopTen(curr, query, newFreq);
        }
    }

    private void updateTopTen(TrieNode node, String query, int freq) {
        // Remove the query if it already exists in the top ten (to update its freq)
        node.topTen.removeIf(info -> info.query.equals(query));

        // Add the updated info
        node.topTen.add(new QueryInfo(query, freq));

        // Sort and prune to keep only Top 10
        Collections.sort(node.topTen);
        if (node.topTen.size() > 10) {
            node.topTen.remove(node.topTen.size() - 1);
        }
    }

    /**
     * Returns top 10 suggestions for a prefix.
     * Time Complexity: O(L) where L is prefix length.
     */
    public List<String> search(String prefix) {
        TrieNode curr = root;
        for (char c : prefix.toCharArray()) {
            if (!curr.children.containsKey(c)) {
                return Collections.emptyList();
            }
            curr = curr.children.get(c);
        }

        List<String> results = new ArrayList<>();
        for (QueryInfo info : curr.topTen) {
            results.add(info.query);
        }
        return results;
    }

    public static void main(String[] args) {
        Assignment01and02 ac = new AutocompleteSystem();

        ac.updateFrequency("java tutorial", 1234567);
        ac.updateFrequency("javascript", 987654);
        ac.updateFrequency("java download", 456789);

        System.out.println("Search 'jav': " + ac.search("jav"));

        // Trending update
        ac.updateFrequency("java 21 features", 2000000);
        System.out.println("Search 'jav' after trend: " + ac.search("jav"));
    }
}