
import java.util.HashSet;
import java.util.Set;

public class Grammar {
    private final Set<String> letters;
    private final Set<Production> productionRules;
    private Set<String> lhsL;
    private Set<String> lhsR;
        
    public Grammar() {
        this.letters = new HashSet<>();
        this.productionRules = new HashSet<>();
        this.lhsL = new HashSet<>();
        this.lhsR = new HashSet<>();
    }

    public void addProductionRules(String rules) {
        String[] parts = rules.split(" ");
        if (parts.length == 3) {
            lhsL.add(parts[1]);
            lhsR.add(parts[2]);
            String lhs = parts[1] + parts[2];
            productionRules.add(new Production(lhs, parts[1], parts[2], parts[0]));
            addLetters(parts[0]);
            addLetters(parts[1]);
            addLetters(parts[2]);
        } else {
            lhsL.add(parts[1]);
            productionRules.add(new Production(parts[1], parts[0]));
            addLetters(parts[0]);
            addLetters(parts[1]);
        }
    }

    public Set<Production> getProductions() {
        return productionRules;
    }

    public void addLetters(String l) {
        letters.add(l); 
    }
    
    public Set<String> getLetters() {
        return letters;
    }

    public String getRHSByLHS(String lhs) {
        for (Production production : productionRules) {
            if (production.getLHS().equals(lhs)) {
                return production.getRHS();
            }
        }
        return null;
    }

    public boolean isLhsR(String lhsR) {
        return lhsR.contains(lhsR);
    }

    // реализуй метод возвращение продукций. Для поиска продукций

    public class Production {
        String lhs;
        String lhsL;
        String lhsR;
        String rhs;

        Production(String lhs, String lhsL, String lhsR, String rhs) {
            this.lhs = lhs;
            this.lhsL = lhsL;
            this.lhsR = lhsR;
            this.rhs = rhs;
        }

        Production(String lhs, String rhs) {
            this.lhs = lhs;
            this.lhsL = null;
            this.lhsR = null;
            this.rhs = rhs;
        }

        public String getLHS() {
            return lhs;
        }

        public String getLHSL() {
            return lhsL;
        }

        public String getLHSR() {
            return lhsR;
        }
        
        public String getRHS() {
            return rhs;
        }
    }
}
