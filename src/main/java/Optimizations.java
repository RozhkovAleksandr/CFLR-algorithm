public class Optimizations {

    private final boolean opt1;
    private final boolean opt2;
    private final boolean opt3;
    private final boolean opt4;
    private final boolean opt5;

    public Optimizations(boolean opt1, boolean opt2, boolean opt3, boolean opt4, boolean opt5) {
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.opt3 = opt3;
        this.opt4 = opt4;
        this.opt5 = opt5;
    }

    public boolean isOpt1() {
        return opt1;
    }

    public boolean isOpt2() {
        return opt2;
    }

    public boolean isOpt3() {
        return opt3;
    }

    public boolean isOpt4() {
        return opt4;
    }

    public boolean isOpt5() {
        return opt5;
    }
    
}