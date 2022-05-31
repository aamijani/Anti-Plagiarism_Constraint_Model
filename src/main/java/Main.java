import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main <T extends Token> {
    private static Set<LetterToken> sigma = new HashSet<LetterToken>();
    private static LinkedList<LetterToken> input = new LinkedList<>();
    private static FiniteStateAutomaton M = new FiniteStateAutomaton(sigma);
    private static final List<ArrayList<LetterToken>> N = new ArrayList<>();
    private static final Map<LetterToken, State> Q = new HashMap<LetterToken, State>();
    private static final Map<State, Map<State, Set<LetterToken>>> a = new HashMap<>();
    private static final List<ArrayList<LetterToken>> C = new ArrayList<>();
    public static final Map<State, ArrayList<LetterToken>> w = new HashMap<>();
    public static final List<State> Q_trie = new ArrayList<>();
    public static final Map<State, ArrayList<State>> S = new HashMap<>();

    public static void main(String[] args) throws IOException {
        input = getInput();
        sigma.addAll(input);
        System.out.println(input);
        MarkovAutomaton();
        graphsToFile(M.delta);
        MaxOrderMarkovAutomaton();

    }

    private static LinkedList<LetterToken> getInput() throws IOException {
        LinkedList<LetterToken> listLetterToken = new LinkedList<>();
        String pathname = "file.txt";
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int)file.length());

        try (Scanner scanner = new Scanner(file)) {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine()).append(System.lineSeparator());
            }
            String[] words = fileContents.toString().split(" ");

            if (words.length <= 1) {
                char[] strArr = fileContents.toString().toCharArray();
                for (char c : strArr) {
                    if (!(c == ' ')) {
                        LetterToken lt = new LetterToken(Character.toString(c));
                        listLetterToken.add(lt);
                    }
                }
                listLetterToken.removeLast();
            }

            if (words.length > 1){
                for (String str : words){
                    LetterToken lt = new LetterToken(str);
                    listLetterToken.add(lt);
                }
            }

            return listLetterToken;
        }
    }


    private static  void MaxOrderMarkovAutomaton() {
        defineN();

        for (ArrayList<LetterToken> a_i : N) {
            LetterToken a1 = a_i.get(0);
            State q = Q.get(a1);
            q = separate(q, a1);
            M.delta.put(q, new HashMap<LetterToken, State>());
            w.put(q, new ArrayList<>());
            w.get(q).add(a1);

        }

        //Compute the trie of no-goods
        for (ArrayList<LetterToken> a1_L : N) {
            State q = M.q_0;
            int i = 1;
            while (M.delta.get(q).containsKey(a1_L.get(i - 1))) {
                q = M.delta.get(q).get(a1_L.get(i - 1));
                i = i + 1;

            }

            for (int j = i-1; j < 4; j++) {
                State q_prime = newState();
                Q_trie.add(q_prime);
                M.F.add(q_prime);
                M.delta.get(q).put(a1_L.get(j), q_prime);
                graphsToFile(M.delta);
                w.put(q_prime, new ArrayList<>());
                for (int k = 0; k <= j; k++) {
                    w.get(q_prime).add(a1_L.get(k));
                }
                a.get(q_prime).put(q, Collections.singleton(a1_L.get(j - 1)));
                q = q_prime;
            }
            M.F.remove(q);
        }

        for (State q : Q_trie){
            S.put(q, new ArrayList<State>());
            ArrayList<LetterToken> a1 = w.get(q);
            for (State q_prime : Q_trie) {
                ArrayList<LetterToken> a2 = w.get(q_prime);
                int n = a2.size()-a1.size();
                if (n >= 0 && !(a1.equals(a2))) {
                    List<LetterToken> substring = a2.subList((a2.size() - a1.size()), (a2.size()));
                    if (substring.equals(a1)) {
                        S.get(q).add(q_prime);
                    }
                }
            }
        }

        for (State q: M.Q){
            if (!Q_trie.contains(q) && M.delta.get(M.q_0).containsValue(q)){
                S.put(q, new ArrayList<State>());
                ArrayList<LetterToken> a1 = w.get(q);
                for (State q_prime : Q_trie) {
                    ArrayList<LetterToken> a2 = w.get(q_prime);
                    int n = a2.size() - a1.size();
                    if (n >= 0 && !(a1.equals(a2))) {
                        List<LetterToken> substring = a2.subList((a2.size() - a1.size()), (a2.size()));
                        if (substring.equals(a1)) {
                            S.get(q).add(q_prime);
                        }
                    }
                }
            }
        }

        Map<State, ArrayList<String>> w2 = new HashMap<>();
        for (State key : w.keySet()) {
            w2.put(key, new ArrayList<>());
            ArrayList<LetterToken> value = w.get(key);
            if (value != null) {
                for (LetterToken element : value) {
                    if (element != null) {
                        w2.get(key).add(element.toString());
                    }
                }
            }
        }


        Map<State, ArrayList<String>> map = sortMap(w2);
        Stack<State> sortedQ_Trie = new Stack<State>();
        for (State q: map.keySet()){
            if (Q_trie.contains(q) || M.delta.get(M.q_0).containsValue(q)){
                sortedQ_Trie.push(q);
            }
        }


        for (State q: sortedQ_Trie){
            for(LetterToken token : sigma){
                if (M.delta.get(q).containsKey(token)){
                    for (State q_prime : S.get(q)){
                        if(!M.delta.get(q_prime).containsKey(token)){
                            State toState = M.delta.get(q).get(token);
                            //M.delta.get(q).remove(token, toState);
                            M.delta.get(q_prime).put(token, toState);
                            graphsToFile(M.delta);
                        }
                    }
                }
            }
        }


        for (State q : Q_trie){
            Map<State, Set<LetterToken>> map2 = a.get(q);
            Set<LetterToken> strSet = new HashSet<>();
            for (Map.Entry<State, Set<LetterToken>> entry : map2.entrySet()) {
                strSet = entry.getValue();
            }
            LetterToken a1 = strSet.iterator().next();

            for (LetterToken a2 : sigma){
                for (ArrayList<LetterToken> substring : C){
                    if (substring.get(0).equals(a1) && substring.get(1).equals(a2)){
                        if (!M.delta.get(q).containsKey(a2)){
                            if (!M.delta.get(M.q_0).containsValue(Q.get(a2))) {
                                M.delta.get(q).put(a2, Q.get(a2));
                                graphsToFile(M.delta);
                            }
                        }
                    }
                }
            }
        }


        for(State s1 : new ArrayList<State>(M.Q)) {
            if(!M.F.contains(s1)) {
                M.Q.remove(s1);
                M.delta.remove(s1);
                for (Map.Entry<State, Map<LetterToken, State>> entry : new ArrayList<>(M.delta.entrySet())){
                    for (LetterToken s : new ArrayList<>(M.delta.get(entry.getKey()).keySet())){
                        if (M.delta.get(entry.getKey()).get(s).equals(s1) ){
                            M.delta.get(entry.getKey()).remove(s);
                            graphsToFile(M.delta);
                        }
                    }
                }
                a.remove(s1);
            }
            System.out.println(M.delta);
        }


    }

    private static void MarkovAutomaton() {
        defineC();
        State q = newState();
        a.get(q).put(M.q_0, new HashSet<>());

        for (LetterToken symbol : sigma) {
            M.delta.get(M.q_0).put(symbol, q);
            graphsToFile(M.delta);
            Q.put(symbol, q);
            a.get(q).get(M.q_0).add(symbol);
        }

        M.F.add(M.q_0);
        M.F.add(q);

        for (ArrayList<LetterToken> a1_a2 : C) {
            LetterToken a1 = a1_a2.get(0);
            LetterToken a2 = a1_a2.get(1);

            State q_1 = Q.get(a1);
            q = separate(q_1, a1);
            State q_2 = Q.get(a2);

            M.delta.get(q).put(a2, q_2);
            graphsToFile(M.delta);
            for (State q_prime : M.Q) {
                if (M.delta.get(q_prime).equals(M.delta.get(q))) {
                    Map<State, Set<LetterToken>> statesPointingToq = a.get(q);
                    for (Map.Entry<State, Set<LetterToken>> entry : statesPointingToq.entrySet()) {
                        a.get(q_prime).put(entry.getKey(), entry.getValue());

                        for (LetterToken s : entry.getValue()) {
                            M.delta.get(entry.getKey()).remove(s, q);
                            M.delta.get(entry.getKey()).put(s, q_prime);
                            graphsToFile(M.delta);
                        }

                    }
                    Q.put(a1, q_prime);
                    if (!q.toString().equals(q_prime.toString())) {
                        M.delta.put(q, new HashMap<LetterToken, State>());
                    }
                    break;
                }
            }

            System.out.println("delta:" + M.delta);
        }
        System.out.println("delta2:" + M.delta);

    }

    public static State separate(State q_1, LetterToken a1) {
        State q = newState();
        M.F.add(q);

        for (LetterToken e : sigma) {
            if (M.delta.containsKey(q_1) && M.delta.get(q_1).containsKey(e)) {
                State toState = M.delta.get(q_1).get(e);
                //M.delta.get(q_1).remove(e, toState);
                M.delta.get(q).put(e, toState);
                //graphsToFile();

            }
        }

        for (State q_prime : M.Q) {

            if ( M.delta.get(q_prime) !=null && M.delta.get(q_prime).containsKey(a1) && M.delta.get(q_prime).get(a1).equals(q_1)) {
                M.delta.get(q_prime).put(a1, q);
                //graphsToFile();
                a.get(q).put(q_prime, new HashSet<>(Collections.singletonList(a1)));
            }
        }
        Q.put(a1, q);
        return q;
    }

    public static void graphsToFile(Map<State, Map<LetterToken, State>> delta) {
        String dir = "g/";
        String pngFileName = "filename";
        long count = 0;
        try {
            count = Files.list(Paths.get(dir))
                    .filter(path -> path.getFileName().toString().startsWith(pngFileName))
                    .count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File myObj = new File("g/filename" + count + ".dot");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter("g/filename" + count + ".dot", true));
                    bw.append("digraph abc{ \n");

                    for (Map.Entry<State, Map<LetterToken, State>> entry : delta.entrySet()) {
                        for (Map.Entry<LetterToken, State> entry2 : entry.getValue().entrySet()){
                            bw.append(String.format("\"%s\" -> \"%s\" [ label=\"%s\" ]; \n", entry.getKey(),entry2.getValue(), entry2.getKey()));
                        }

                    }
                    bw.append("}");
                    bw.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public static <K,V extends Collection> Map<K,V> sortMap(Map<K,V> map){
        return map.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private static void defineN() {
        for (int i = 0; i < input.size(); i++){
            if (i+4 < input.size()){
                ArrayList<LetterToken> l1 = new ArrayList<>();l1.add(input.get(i));l1.add(input.get(i + 1));l1.add(input.get(i + 2));l1.add(input.get(i + 3));
                if (!N.contains(l1)){
                    N.add(l1);
                }
            }
        }
    }

    public static void defineC() {
        for (int i = 0; i < input.size(); i++){
            if (i+2 < input.size()){
                ArrayList<LetterToken> l1 = new ArrayList<>(); l1.add(input.get(i)); l1.add(input.get(i+1));
                if (!C.contains(l1)){
                    C.add(l1);
                }
            }
        }
    }

    private static State newState() {
        State newState = M.newState();
        a.put(newState, new HashMap<>());
        return newState;
    }


}
