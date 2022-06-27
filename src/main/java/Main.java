import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main<T extends Token> {
    private static Set<String> sigma = new HashSet<String>();
    private static FiniteStateAutomaton M = new FiniteStateAutomaton(sigma);
    private static final List<ArrayList<String>> N = new ArrayList<>();
    private static final Map<String, State> Q = new HashMap<>();
    private static final Map<State, Map<State, Set<String>>> a = new HashMap<>();
    private static final List<ArrayList<String>> C = new ArrayList<>();
    public static final Map<State, ArrayList<String>> w = new HashMap<>();
    public static final List<State> Q_trie = new ArrayList<>();
    private static LinkedList<String> input = new LinkedList<>();
    public static final Map<State, ArrayList<State>> S = new HashMap<>();
    public static int type = 1000;

    public static void main(String[] args) throws IOException {
        input = getInput();
        sigma.addAll(input);
        defineN();
        defineC();

        MarkovAutomaton();
        LinkedList<State> statestoremove1 = new LinkedList<>();
        for (State s : M.delta.keySet()){
            if (M.delta.get(s).isEmpty()){
                statestoremove1.add(s);
                System.out.println("yes");
                for (State s1 : M.delta.keySet()){
                    if (M.delta.get(s1).containsValue(s)){
                        statestoremove1.remove(s);
                        System.out.println("yes 1");
                    }
                }
            }
        }
        System.out.println(M.delta);
        for (State s : statestoremove1) {
            M.delta.remove(s);
            System.out.println("removed state "+ s);
        }

        graphsToFile(M.delta);
        MaxOrderMarkovAutomaton();
        LinkedList<State> statestoremove2 = new LinkedList<>();
        for (State s : M.delta.keySet()){
            if (M.delta.get(s).isEmpty()){
                statestoremove2.add(s);
                System.out.println("yes");
                for (State s1 : M.delta.keySet()){
                    if (M.delta.get(s1).containsValue(s)){
                        statestoremove2.remove(s);
                        System.out.println("yes 1");
                    }
                }
            }
        }
        System.out.println(M.delta);
        for (State s : statestoremove2) {
            M.delta.remove(s);
            System.out.println("removed state "+ s);
        }
        graphsToFile(M.delta);

        CreateSubsequence();
    }

    private static void CreateSubsequence() {
        int n = 0;
        System.out.println("Would you like to produce a non-plagiaristic subsequence from your input? [y/n]");
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();
        if (str.equals("y") || str.equals("Y")) {
            System.out.println("Enter your preferred length of the subsequence: ");
            n = sc.nextInt();
        }
        System.out.println();
        String s = "";

        Map<String, State> set = M.delta.get(M.q_0);
        int i = 0;
        while (i < n) {
            Random rand = new Random();
            if (set.size() <= 0) {
                set = M.delta.get(M.q_0);
                s = "";
                i = 0;
                continue;
            }
            int randIndex = rand.nextInt(set.size());
            String key = (String) set.keySet().toArray()[randIndex];
            State value = set.get(key);
            s = s.concat(key.toString());
            if (type == 1) {
                s = s.concat(" ");
            }
            set = M.delta.get(value);
            i++;
        }

        if (type == 1){
            s = s.substring(0, 1).toUpperCase() + s.substring(1);
            s = s.substring(0,s.length() - 1);
            s = s.concat(".");
        }
        System.out.println(s);
    }

    private static LinkedList<String> getInput() throws IOException {
        LinkedList<String> listLetterToken = new LinkedList<>();
        String pathname = "input.txt";
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine()).append(System.lineSeparator());
            }
            String[] words = fileContents.toString().replaceAll("[^A-Za-z\\s]", "").replaceAll("\\s{2,}", " ").toLowerCase().split("\\s+");
            if (words.length <= 1) {
                type = 0;
                char[] strArr = fileContents.toString().toCharArray();
                for (char c : strArr) {
                    if (!(c == ' ')) {
                        String lt = new String(Character.toString(c));
                        listLetterToken.add(lt);
                    }
                }
                listLetterToken.removeLast();
            }
            if (words.length > 1) {
                type = 1;
                for (String str : words) {
                    String lt = new String(str);
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
        for (ArrayList<String> a_i : N) {
            String a1 = a_i.get(0);
            State q = Q.get(a1);
            q = separate(q, a1);
            M.delta.put(q, new HashMap<String, State>());
            w.put(q, new ArrayList<>());
            w.get(q).add(a1);
        }

        //Compute the trie of no-goods
        for (ArrayList<String> a1_L : N) {
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
            ArrayList<String> a1 = w.get(q);
            for (State q_prime : Q_trie) {
                ArrayList<String> a2 = w.get(q_prime);
                int n = a2.size() - a1.size();
                if (n >= 0 && !(a1.equals(a2))) {
                    List<String> substring = a2.subList((a2.size() - a1.size()), (a2.size()));
                    if (substring.equals(a1)) {
                        S.get(q).add(q_prime);
                    }
                }
            }
        }

        for (State q : M.Q) {
            if (!Q_trie.contains(q) && M.delta.get(M.q_0).containsValue(q)) {
                S.put(q, new ArrayList<State>());
                ArrayList<String> a1 = w.get(q);
                for (State q_prime : Q_trie) {
                    ArrayList<String> a2 = w.get(q_prime);
                    if (a2 != null && a1 != null) {
                        int n = a2.size() - a1.size();
                        if (n >= 0 && !(a1.equals(a2))) {
                            List<String> substring = a2.subList((a2.size() - a1.size()), (a2.size()));
                            if (substring.equals(a1)) {
                                S.get(q).add(q_prime);
                            }
                        }
                    }
                }
            }
        }


        Map<State, ArrayList<String>> w2 = new HashMap<>();
        for (State key : w.keySet()) {
            w2.put(key, new ArrayList<>());
            ArrayList<String> value = w.get(key);
            if (value != null) {
                for (String element : value) {
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
            for (String a : sigma) {
                if (M.delta.get(q).containsKey(a)) {
                    for (State q_prime : S.get(q)) {
                        if (!M.delta.get(q_prime).containsKey(a)) {
                            State toState = M.delta.get(q).get(a);
                            M.delta.get(q_prime).put(a, toState);

                        }
                    }
                }
            }
        }


        for (State q : Q_trie) {
            Map<State, Set<String>> map2 = a.get(q);
            Set<String> strSet = new HashSet<>();
            for (Map.Entry<State, Set<String>> entry : map2.entrySet()) {
                strSet = entry.getValue();
            }
            String a1 = strSet.iterator().next();

            for (String a2 : sigma) {
                for (ArrayList<String> substring : C) {
                    if (substring.get(0).equals(a1) && substring.get(1).equals(a2)) {
                        if (!M.delta.get(q).containsKey(a2)) {
                            if (!M.delta.get(M.q_0).containsValue(Q.get(a2))) {
                                M.delta.get(q).put(a2, Q.get(a2));
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
                for (Map.Entry<State, Map<String, State>> entry : new ArrayList<>(M.delta.entrySet())) {
                    for (String s : new ArrayList<>(M.delta.get(entry.getKey()).keySet())) {
                        if (M.delta.get(entry.getKey()).get(s).equals(s1)) {
                            M.delta.get(entry.getKey()).remove(s);
                        }
                    }
                }
                a.remove(s1);
            }
        }
    }

    private static void MarkovAutomaton() {
        State q = newState();                                              //line 2
        a.get(q).put(M.q_0, new HashSet<>());

        for (String symbol : sigma) {                                      //line 4
            M.delta.get(M.q_0).put(symbol, q);                             //line 5

            Q.put(symbol, q);                                              //line 6
            a.get(q).get(M.q_0).add(symbol);
        }
        M.F.add(M.q_0);
        M.F.add(q);                                                        //line 7

        for (ArrayList<String> a1_a2 : C) {                                //line 8
            String a1 = a1_a2.get(0);
            String a2 = a1_a2.get(1);

            State q_1 = Q.get(a1);                                         //line 9
            q = separate(q_1, a1);                                         //line 10
            State q_2 = Q.get(a2);                                         //line 11

            M.delta.get(q).put(a2, q_2);                                   //line 12
            // if exists q'inQ s.t. q and q' are equivalent...
            for (State q_prime : M.Q) {                                    //line 13
                if (M.delta.get(q_prime).equals(M.delta.get(q))) {         //line 14
                    Map<State, Set<String>> statesPointingToq = a.get(q);
                    for (Map.Entry<State, Set<String>> entry : statesPointingToq.entrySet()) {
                        a.get(q_prime).put(entry.getKey(), entry.getValue());

                        for (String s : entry.getValue()) {
                            M.delta.get(entry.getKey()).remove(s, q);
                            M.delta.get(entry.getKey()).put(s, q_prime);
                        }
                    }

                    Q.put(a1, q_prime);
                    if (!q.toString().equals(q_prime.toString())) {
                        M.delta.put(q, new HashMap<String, State>());
                    }
                    break;
                }
            }
        }
    }

    public static void graphsToFile(Map<State, Map<String, State>> delta) {
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
            File myObj = new File("GraphViz/filename" + count + ".dot");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter("GraphViz/filename" + count + ".dot", true));
                    bw.append("digraph abc{ \n");
                    bw.append("node [shape = circle, ordering=out];");

                    State newState = new State();

                    bw.append(String.format("\"%s\" [ label= \"\", shape = none ]; \n", newState));
                    bw.append(String.format("\"%s\" [ label= \"\" ]; \n", M.q_0));
                    bw.append(String.format("\"%s\" -> \"%s\"  [ label=\"\" ]; \n", newState,M.q_0));

                    Set<String> printedNodes = new HashSet<>();
                    for (Map.Entry<State, Map<String, State>> entry : delta.entrySet()) {
                        if(!printedNodes.contains(entry.getKey().toString())) {
                            if (entry.getKey() != M.q_0) {
                                bw.append(String.format("\"%s\" [ label= \"\" ]; \n", entry.getKey()));
                                printedNodes.add(entry.getKey().toString());
                            }
                        }
                        for (Map.Entry<String, State> entry2 : entry.getValue().entrySet()) {
                            if(!printedNodes.contains(entry2.getValue().toString())){
                                if (entry.getKey() != M.q_0) {
                                    bw.append(String.format("\"%s\" [ label= \"\" ]; \n", entry2.getValue()));
                                    printedNodes.add(entry2.getValue().toString());
                                }
                            }
                        }
                    }


                    for (Map.Entry<State, Map<String, State>> entry : delta.entrySet()) {
                        for (Map.Entry<String, State> entry2 : entry.getValue().entrySet()){
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

    public static State separate(State q_1, String a1) {
        State q = newState();                                                   //line 17
        M.F.add(q);                                                             //line 18

        for (String a : sigma) {                                           //line 19
            if (M.delta.containsKey(q_1) && M.delta.get(q_1).containsKey(a)) {
                State toState = M.delta.get(q_1).get(a);                        //line 20
                M.delta.get(q_1).remove(a, toState);
                M.delta.get(q).put(a, toState);

            }
        }

        for (State q_prime : M.Q) {                                             //line 21
            if (M.delta.containsKey(q_prime) && M.delta.get(q_prime).containsKey(a1) && M.delta.get(q_prime).get(a1).equals(q_1)) {
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
                ArrayList<String> l1 = new ArrayList<>();
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
                ArrayList<String> l1 = new ArrayList<>();
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
