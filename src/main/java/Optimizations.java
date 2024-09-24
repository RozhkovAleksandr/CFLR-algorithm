public class Optimizations {

    private final boolean opt1;
    private final boolean opt2;
    private final boolean opt3;
    private final boolean opt4;
    private final boolean opt5;
    public final int number;

    public Optimizations(String optim) {
        number = Integer.parseInt(optim);
        switch (optim) {
            case "5":
                this.opt1 = true;
                this.opt2 = false;
                this.opt3 = false;
                this.opt4 = false;
                this.opt5 = true;
                break;
            case "4":
                this.opt1 = true;
                this.opt2 = false;
                this.opt3 = false;
                this.opt4 = true;
                this.opt5 = false;
                break;
            case "3":
                this.opt1 = true;
                this.opt2 = false;
                this.opt3 = true;
                this.opt4 = false;
                this.opt5 = false;
                break;
            case "2":
                this.opt1 = true;
                this.opt2 = true;
                this.opt3 = false;
                this.opt4 = false;
                this.opt5 = false;
                break;
            case "1":
                this.opt1 = true;
                this.opt2 = false;
                this.opt3 = false;
                this.opt4 = false;
                this.opt5 = false;
                break;
            default: 
                this.opt1 = false;
                this.opt2 = false;
                this.opt3 = false;
                this.opt4 = false;
                this.opt5 = false;
                break;
        }
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