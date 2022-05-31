public class State {
    private static int stateCount = 0;

    private int stateNum;

    public State(){
        this.stateNum = stateCount++;
    }

    public String toString(){
        return "state " + stateNum;
    }
}
