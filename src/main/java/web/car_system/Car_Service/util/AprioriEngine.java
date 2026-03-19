package web.car_system.Car_Service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Apriori Algorithm Engine for finding association rules
 * Discovers patterns like: "Users who viewed Car A also viewed Car B"
 */
@Component
@Slf4j
public class AprioriEngine {
    
    /**
     * Generate association rules from transaction data
     * 
     * @param transactions List of transactions, where each transaction is a set of car IDs
     * @param minSupport Minimum support threshold (0.0 - 1.0)
     * @param minConfidence Minimum confidence threshold (0.0 - 1.0)
     * @return List of association rules
     */
    public List<AssociationRule> generateRules(
            List<Set<Integer>> transactions,
            double minSupport,
            double minConfidence) {
        
        if (transactions == null || transactions.isEmpty()) {
            log.warn("No transactions provided for Apriori algorithm");
            return Collections.emptyList();
        }
        
        log.info("Starting Apriori algorithm with {} transactions, minSupport={}, minConfidence={}", 
                transactions.size(), minSupport, minConfidence);
        
        // Step 1: Find frequent itemsets (pairs of cars that appear together)
        Map<Set<Integer>, Integer> frequentPairs = findFrequentPairs(transactions, minSupport);
        
        log.info("Found {} frequent pairs", frequentPairs.size());
        
        // Step 2: Generate association rules from frequent itemsets
        List<AssociationRule> rules = generateAssociationRules(
                frequentPairs, 
                transactions.size(), 
                minConfidence
        );
        
        log.info("Generated {} association rules", rules.size());
        
        return rules;
    }
    
    /**
     * Find frequent pairs (2-itemsets) that meet minimum support
     */
    private Map<Set<Integer>, Integer> findFrequentPairs(
            List<Set<Integer>> transactions, 
            double minSupport) {
        
        Map<Set<Integer>, Integer> pairCounts = new HashMap<>();
        int totalTransactions = transactions.size();
        int minSupportCount = (int) Math.ceil(minSupport * totalTransactions);
        
        // Count all pairs
        for (Set<Integer> transaction : transactions) {
            if (transaction.size() < 2) continue;
            
            List<Integer> items = new ArrayList<>(transaction);
            for (int i = 0; i < items.size(); i++) {
                for (int j = i + 1; j < items.size(); j++) {
                    Set<Integer> pair = new HashSet<>(Arrays.asList(items.get(i), items.get(j)));
                    pairCounts.merge(pair, 1, Integer::sum);
                }
            }
        }
        
        // Filter by minimum support
        return pairCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= minSupportCount)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * Generate association rules from frequent itemsets
     */
    private List<AssociationRule> generateAssociationRules(
            Map<Set<Integer>, Integer> frequentPairs,
            int totalTransactions,
            double minConfidence) {
        
        List<AssociationRule> rules = new ArrayList<>();
        
        // Calculate support for individual items (needed for confidence calculation)
        Map<Integer, Integer> itemCounts = new HashMap<>();
        for (Map.Entry<Set<Integer>, Integer> entry : frequentPairs.entrySet()) {
            for (Integer item : entry.getKey()) {
                itemCounts.merge(item, entry.getValue(), Integer::sum);
            }
        }
        
        // Generate rules from each frequent pair
        for (Map.Entry<Set<Integer>, Integer> entry : frequentPairs.entrySet()) {
            Set<Integer> pair = entry.getKey();
            int pairSupport = entry.getValue();
            
            if (pair.size() != 2) continue;
            
            Iterator<Integer> iterator = pair.iterator();
            Integer itemA = iterator.next();
            Integer itemB = iterator.next();
            
            // Generate rule: A -> B
            double confidenceAtoB = (double) pairSupport / itemCounts.getOrDefault(itemA, 1);
            if (confidenceAtoB >= minConfidence) {
                double support = (double) pairSupport / totalTransactions;
                double lift = confidenceAtoB / ((double) itemCounts.getOrDefault(itemB, 1) / totalTransactions);
                
                rules.add(new AssociationRule(itemA, itemB, confidenceAtoB, support, lift));
            }
            
            // Generate rule: B -> A
            double confidenceBtoA = (double) pairSupport / itemCounts.getOrDefault(itemB, 1);
            if (confidenceBtoA >= minConfidence) {
                double support = (double) pairSupport / totalTransactions;
                double lift = confidenceBtoA / ((double) itemCounts.getOrDefault(itemA, 1) / totalTransactions);
                
                rules.add(new AssociationRule(itemB, itemA, confidenceBtoA, support, lift));
            }
        }
        
        return rules;
    }
    
    /**
     * Association Rule POJO
     */
    public static class AssociationRule {
        private final Integer antecedent;  // IF this car
        private final Integer consequent;  // THEN recommend this car
        private final double confidence;
        private final double support;
        private final double lift;
        
        public AssociationRule(Integer antecedent, Integer consequent, 
                              double confidence, double support, double lift) {
            this.antecedent = antecedent;
            this.consequent = consequent;
            this.confidence = confidence;
            this.support = support;
            this.lift = lift;
        }
        
        public Integer getAntecedent() { return antecedent; }
        public Integer getConsequent() { return consequent; }
        public double getConfidence() { return confidence; }
        public double getSupport() { return support; }
        public double getLift() { return lift; }
        
        @Override
        public String toString() {
            return String.format("Rule{%d -> %d, conf=%.2f, supp=%.2f, lift=%.2f}",
                    antecedent, consequent, confidence, support, lift);
        }
    }
}
