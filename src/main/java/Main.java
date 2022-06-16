import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main<T extends Token> {
    //KALIKIMAKA
    //can you can a can as a canner can can a can

    private static Set<LetterToken> sigma = new HashSet<LetterToken>();

    private static FiniteStateAutomaton M = new FiniteStateAutomaton(sigma);
    private static final List<ArrayList<LetterToken>> N = new ArrayList<>();
    private static final Map<LetterToken, State> Q = new HashMap<>();
    private static final Map<State, Map<State, Set<LetterToken>>> a = new HashMap<>();
    private static final List<ArrayList<LetterToken>> C = new ArrayList<>();
    public static final Map<State, ArrayList<LetterToken>> w = new HashMap<>();
    public static final List<State> Q_trie = new ArrayList<>();
    private static LinkedList<LetterToken> input = new LinkedList<>();
    public static final Map<State, ArrayList<State>> S = new HashMap<>();
    public static int type = 1000;

    public static void main(String[] args) throws IOException {
        input = getInput();
        sigma.addAll(input);
        defineN();
        defineC();

        MarkovAutomaton();
        MaxOrderMarkovAutomaton();

        int n = 0;
        System.out.println("Do you want to produce a sequence from the automaton?");
        Scanner sc=new Scanner(System.in);
        String str= sc.nextLine();
        if (str.equals("y") || str.equals("Y")){
            System.out.println("Enter the length of output: ");
            n = sc.nextInt();
        }

        String s = "";
        Map<LetterToken, State> set = M.delta.get(M.q_0);
        for (int i = 0; i < n; i++){
            Random rand = new Random();
            int randIndex = rand.nextInt(set.size());
            LetterToken key = (LetterToken) set.keySet().toArray()[randIndex];
            State value = set.get(key);
            s = s.concat(key.toString());
            if (type == 0){
                s = s.concat(" ");
            }
            set = M.delta.get(value);
        }
        System.out.println(s);
    }

    private static LinkedList<LetterToken> getInput() throws IOException {
        LinkedList<LetterToken> listLetterToken = new LinkedList<>();
        String pathname = "input.txt";
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine()).append(System.lineSeparator());
            }
            String[] words = fileContents.toString().split("\\s+");
            if (words.length <= 1) {
                type = 0;
                char[] strArr = fileContents.toString().toCharArray();
                for (char c : strArr) {
                    if (!(c == ' ')) {
                        LetterToken lt = new LetterToken(Character.toString(c));
                        listLetterToken.add(lt);
                    }
                }
                listLetterToken.removeLast();
            }
            type = 1;
            if (words.length > 1) {
                for (String str : words) {
                    LetterToken lt = new LetterToken(str);
                    listLetterToken.add(lt);
                }
            }
            return listLetterToken;
        }
    }


    public static <K, V extends Collection> Map<K, V> sortMap(Map<K, V> map) {
        return map.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }


    private static void MaxOrderMarkovAutomaton() {
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

            for (int j = i - 1; j < 4; j++) {
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

        for (State q : Q_trie) {
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

        for (State q : M.Q) {
            if (!Q_trie.contains(q) && M.delta.get(M.q_0).containsValue(q)) {
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
        for (State q : map.keySet()) {
            if (Q_trie.contains(q) || M.delta.get(M.q_0).containsValue(q)) {
                sortedQ_Trie.push(q);
            }
        }


        for (State q : sortedQ_Trie) {
            for (LetterToken a : sigma) {
                if (M.delta.get(q).containsKey(a)) {
                    for (State q_prime : S.get(q)) {
                        if (!M.delta.get(q_prime).containsKey(a)) {
                            State toState = M.delta.get(q).get(a);
                            M.delta.get(q_prime).put(a, toState);
                            graphsToFile(M.delta);

                        }
                    }
                }
            }
        }


        for (State q : Q_trie) {
            Map<State, Set<LetterToken>> map2 = a.get(q);
            Set<LetterToken> strSet = new HashSet<>();
            for (Map.Entry<State, Set<LetterToken>> entry : map2.entrySet()) {
                strSet = entry.getValue();
            }
            LetterToken a1 = strSet.iterator().next();

            for (LetterToken a2 : sigma) {
                for (ArrayList<LetterToken> substring : C) {
                    if (substring.get(0).equals(a1) && substring.get(1).equals(a2)) {
                        if (!M.delta.get(q).containsKey(a2)) {
                            if (!M.delta.get(M.q_0).containsValue(Q.get(a2))) {
                                M.delta.get(q).put(a2, Q.get(a2));
                                graphsToFile(M.delta);
                            }
                        }
                    }
                }
            }
        }


        // Q <- Q U (Q_trie n F)
        for (State s1 : new ArrayList<State>(M.Q)) {
            if (!M.F.contains(s1)) {
                M.Q.remove(s1);
                M.delta.remove(s1);
                for (Map.Entry<State, Map<LetterToken, State>> entry : new ArrayList<>(M.delta.entrySet())) {
                    for (LetterToken s : new ArrayList<>(M.delta.get(entry.getKey()).keySet())) {
                        if (M.delta.get(entry.getKey()).get(s).equals(s1)) {
                            M.delta.get(entry.getKey()).remove(s);
                            graphsToFile(M.delta);
                        }
                    }
                }
                a.remove(s1);
            }
        }
    }

    private static void MarkovAutomaton() {
        defineC();
        State q = newState();                                              //line 2
        a.get(q).put(M.q_0, new HashSet<>());

        for (LetterToken symbol : sigma) {                                 //line 4
            M.delta.get(M.q_0).put(symbol, q);                             //line 5
            graphsToFile(M.delta);

            Q.put(symbol, q);                                              //line 6
            a.get(q).get(M.q_0).add(symbol);
        }

        M.F.add(M.q_0);
        M.F.add(q);                                                        //line 7

        for (ArrayList<LetterToken> a1_a2 : C) {                           //line 8
            LetterToken a1 = a1_a2.get(0);
            LetterToken a2 = a1_a2.get(1);

            State q_1 = Q.get(a1);                                         //line 9
            q = separate(q_1, a1);                                         //line 10
            State q_2 = Q.get(a2);                                         //line 11

            M.delta.get(q).put(a2, q_2);                                   //line 12
            graphsToFile(M.delta);
            // if exists q'inQ s.t. q and q' are equivalent...
            for (State q_prime : M.Q) {                                    //line 13
                if (M.delta.get(q_prime).equals(M.delta.get(q))) {         //line 14
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
        }
    }

    public static void graphsToFile(Map<State, Map<LetterToken, State>> delta) {
        String dir = "GraphViz/";
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
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter("g/filename" + count + ".dot", true));
                    bw.append("digraph abc{ \n");

                    for (Map.Entry<State, Map<LetterToken, State>> entry : delta.entrySet()) {
                        for (Map.Entry<LetterToken, State> entry2 : entry.getValue().entrySet()) {
                            bw.append(String.format("\"%s\" -> \"%s\" [ label=\"%s\" ]; \n", entry.getKey(), entry2.getValue(), entry2.getKey()));
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

    public static State separate(State q_1, LetterToken a1) {
        State q = newState();                                                   //line 17
        M.F.add(q);                                                             //line 18

        for (LetterToken a : sigma) {                                           //line 19
            if (M.delta.containsKey(q_1) && M.delta.get(q_1).containsKey(a)) {
                State toState = M.delta.get(q_1).get(a);                        //line 20
                M.delta.get(q_1).remove(a, toState);
                M.delta.get(q).put(a, toState);

            }
        }

        for (State q_prime : M.Q) {                                             //line 21
            if (M.delta.get(q_prime).containsKey(a1) && M.delta.get(q_prime).get(a1).equals(q_1)) {
                M.delta.get(q_prime).put(a1, q);                                //line 22
                a.get(q).put(q_prime, new HashSet<>(Arrays.asList(a1)));
            }
        }
        Q.put(a1, q);                                                           //line 23
        return q;
    }

    private static void defineN() {
        for (int i = 0; i < input.size(); i++) {
            if (i + 4 < input.size() + 1) {
                ArrayList<LetterToken> l1 = new ArrayList<>();
                l1.add(input.get(i));
                l1.add(input.get(i + 1));
                l1.add(input.get(i + 2));
                l1.add(input.get(i + 3));
                if (!N.contains(l1)) {
                    N.add(l1);
                }
            }
        }
    }

    public static void defineC() {
        for (int i = 0; i < input.size(); i++) {
            if (i + 2 < input.size()) {
                ArrayList<LetterToken> l1 = new ArrayList<>();
                l1.add(input.get(i));
                l1.add(input.get(i + 1));
                if (!C.contains(l1)) {
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
