
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Grammar {
    private final Set<String> letters;
    private final Set<String> nonterminal;
    private final Set<Production> productionRules;
    private Set<String> lhsL;
    private Set<String> lhsR;
    private Set<String> epsilonValues;
    private Set<String> vertical;
        
    public Grammar() {
        this.letters = new HashSet<>();
        this.productionRules = new HashSet<>();
        this.lhsL = new HashSet<>();
        this.lhsR = new HashSet<>();
        this.epsilonValues = new HashSet<>();
        this.vertical = new HashSet<>();
        this.nonterminal = new HashSet<>();
    }

    public boolean isVertical(String letter) {
        return vertical.contains(letter);
    }

    public void addNonTerminal(String letter) {
        nonterminal.add(letter);
    }

    public boolean isNonTerminal(String letter) {
        return vertical.contains(letter);
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
            if (parts[1].endsWith("_i") && parts[2].endsWith("_i")) {
                vertical.add(parts[2]);
            } else {
                if (parts[1].endsWith("_i") && !parts[2].endsWith("_i")) {
                    vertical.add(parts[1]);
                }
            }
        } else { 
            if (parts.length == 2) {
                lhsL.add(parts[1]);
                productionRules.add(new Production(parts[1], parts[0]));
                addLetters(parts[0]);
                addLetters(parts[1]);
            } else {
                productionRules.add(new Production(parts[0]));
                addLetters(parts[0]);
                epsilonValues.add(parts[0]);
            }
        }
    }

    public void addNewRules() {
        boolean f = true;
        while (f) {
            f = false;
            for (Production production : new HashSet<>(productionRules)) {
                if ((!epsilonValues.contains(production.getRHS()) && epsilonValues.contains(production.getLHS())) || (!epsilonValues.contains(production.getRHS()) && epsilonValues.contains(production.getLHSR()) && epsilonValues.contains(production.getLHSL()))) {
                    f = true;
                    epsilonValues.add(production.getRHS());
                    break;
                }
                if ((!epsilonValues.contains(production.getRHS()) || (production.getRHS().equals(production.getLHSL()))) && epsilonValues.contains(production.getLHSL())) {
                    productionRules.add(new Production(production.getLHSR(), production.getRHS()));
                }
                if ((!epsilonValues.contains(production.getRHS()) || (production.getRHS().equals(production.getLHSR()))) && epsilonValues.contains(production.getLHSR())) {
                    productionRules.add(new Production(production.getLHSL(), production.getRHS()));
                }
            }
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
            if (Objects.equals(production.getLHS(), lhs)) {
                return production.getRHS();
            }
        }
        return null;
    }

    public boolean isLhsR(String lhsR) {
        
        return lhsR.contains(lhsR);
    }

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

        Production(String rhs) {
            this.lhs = null;
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
