import java.util.*;
import java.util.stream.Collectors;

class Transaction {
    int id;
    double amount;
    String merchant;
    long timestamp; // epoch seconds
    String accountId;

    public Transaction(int id, double amount, String merchant, long timestamp, String accountId) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.timestamp = timestamp;
        this.accountId = accountId;
    }
}

public class Assignment01and02 {

    // 1. Classic Two-Sum: O(n)
    public List<String> findTwoSum(List<Transaction> transactions, double target) {
        Map<Double, Transaction> seen = new HashMap<>();
        List<String> pairs = new ArrayList<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (seen.containsKey(complement)) {
                pairs.add("(" + seen.get(complement).id + ", " + t.id + ")");
            }
            seen.put(t.amount, t);
        }
        return pairs;
    }

    // 2. Two-Sum with 1-Hour Window: O(n)
    public List<String> findTwoSumWithWindow(List<Transaction> transactions, double target) {
        // Map amount to a list of transactions (since multiple tx can have same amount)
        Map<Double, List<Transaction>> map = new HashMap<>();
        List<String> results = new ArrayList<>();
        long oneHourInSec = 3600;

        for (Transaction t1 : transactions) {
            double complement = target - t1.amount;
            if (map.containsKey(complement)) {
                for (Transaction t2 : map.get(complement)) {
                    if (Math.abs(t1.timestamp - t2.timestamp) <= oneHourInSec) {
                        results.add("Match: " + t1.id + " & " + t2.id);
                    }
                }
            }
            map.computeIfAbsent(t1.amount, k -> new ArrayList<>()).add(t1);
        }
        return results;
    }

    // 3. Duplicate Detection: Same amount/merchant, different account
    public void detectDuplicates(List<Transaction> transactions) {
        // Composite Key: "amount:merchant"
        Map<String, List<Transaction>> groups = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.amount + ":" + t.merchant;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        groups.forEach((key, list) -> {
            if (list.size() > 1) {
                Set<String> accounts = list.stream().map(t -> t.accountId).collect(Collectors.toSet());
                if (accounts.size() > 1) {
                    System.out.println("Duplicate Alert for " + key + " across accounts: " + accounts);
                }
            }
        });
    }

    // 4. K-Sum: Recursive approach (Generic)
    public void findKSum(List<Transaction> txs, int k, double target, int start, List<Transaction> current, List<List<Transaction>> results) {
        if (k == 2) {
            // Base case: use two-sum logic for efficiency
            Map<Double, Transaction> map = new HashMap<>();
            for (int i = start; i < txs.size(); i++) {
                double complement = target - txs.get(i).amount;
                if (map.containsKey(complement)) {
                    List<Transaction> match = new ArrayList<>(current);
                    match.add(map.get(complement));
                    match.add(txs.get(i));
                    results.add(match);
                }
                map.put(txs.get(i).amount, txs.get(i));
            }
            return;
        }

        for (int i = start; i < txs.size() - k + 1; i++) {
            current.add(txs.get(i));
            findKSum(txs, k - 1, target - txs.get(i).amount, i + 1, current, results);
            current.remove(current.size() - 1); // Backtrack
        }
    }
}