public class Edge {
    private final int start;
    private final int finish;
    private final String label;
    

    public Edge(int start, int finish, String label) {

        this.start = start;
        this.finish = finish;
        this.label = label;
        
    }

    public int getStart() {
        return start;
    }

    public int getFinish() {
        return finish;
    }

    public String getLabel() {
        return label;
    }
}
