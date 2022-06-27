import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FiniteStateAutomaton {
    public Set<State> Q = new HashSet<>();
    public Set<String> sigma = new HashSet<>();
    public Map<State, Map<String, State>> delta = new HashMap<>();
    public State q_0 = newState();
    public Set<State> F = new HashSet<>();

    public FiniteStateAutomaton(Set<String> _sigma){
        sigma = _sigma;
    }

    public State newState() {
        State newState = new State();
        Q.add(newState);
        delta.put(newState, new HashMap<String, State>());
        return newState;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Q:" + Q);
        sb.append("\ndelta:" + delta);
        sb.append("\nF:" + F);

        return sb.toString();
    }

}
