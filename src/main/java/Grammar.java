
import java.util.*;

public class Grammar {
    private final Set<String> letters;
    private final Set<Production> productionRules;
        
    public Grammar() {
        this.letters = new HashSet<>();
        this.productionRules = new HashSet<>();
    }

    public void addProductionRules(String lhs, String rhs) {
        productionRules.add(new Production(lhs, rhs));
        addLetters(rhs);
    }

    public void addLetters(String l) {
           letters.add(l); 
    }
    
    public Set<String> getLetters() {
        return letters;
    }

    public Set<Production> getProductionRules() {
        return productionRules;
    }

    public String getRHSByLHS(String lhs) {
        for (Production production : productionRules) {
            if (production.getLHS().equals(lhs)) {
                return production.getRHS();
            }
        }
        return null;
    }

    public Production getProductionRulesByLHS(String lhs) {
            for (Production production : productionRules) {
                if (production.getLHS().equals(lhs)) {
                    return production;
                }
            }
            return null;
        }

    public class Production {
        String lhs;
        String rhs;

        Production(String lhs, String rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public String getLHS() {
            return lhs;
        }
        
        public String getRHS() {
            return rhs;
        }
    }
}
