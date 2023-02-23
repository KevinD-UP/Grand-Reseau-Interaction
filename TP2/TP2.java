import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TP2 {

    // Constantes
    private static final String COMMENT_START_TOKEN            = "#";
    private static final String CARDIALITY_COMMAND             = "-c";
    private static final String BETWEENNESS_CENTRALITY_COMMAND = "-b";

    // Données
    private static Sommet[] nodes;
    private static int[] edges;
    private static int x;

    public static void main(String[] args) {
        if(args[0].equals(CARDIALITY_COMMAND)){
            completeParsing(args);
            // Affichage de la cardialité entrante pour le sommet x
            System.out.println(cardialiteEntrante(x));
        } else if (args[0].equals(BETWEENNESS_CENTRALITY_COMMAND)){
            //TODO: betweenness
            completeParsing(args);
            // Affichage du betweenness centrality pour le sommet x
        } else {
            printError("Invalid option : " + args[0] + " doesn't exist, use -c or -b");
        }
    }

    /**
     * Calcule la cardialité entrante pour un sommet v
     *
     * @param v le sommet dont nous désirons la cardialité entrante.
     * @return La cardialité entrante du sommet v.
     */
    private static int cardialiteEntrante(int v) {
        int n = nodes.length;
        int[] inDegree = new int[n]; // Tableau des degrés entrant pour chaque sommet.
        boolean[] removed = new boolean[n]; // Tableau pour marquer les sommets retirés.
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(i -> inDegree[i]));

        // 1. Calculer le degré entrant de chaque sommet
        for (int edge : edges) {
            inDegree[edge]++;
        }

        // 2. Initialiser la file de priorité avec tous les sommets
        for (int i = 0; i < n; i++) {
            pq.offer(i);
        }

        // 3. Pour k de 0 à n
        for (int k = 0; k < n; k++) {
            // a. Tant qu’il existe un sommet x de degré entrant < k
            while (!pq.isEmpty() && inDegree[pq.peek()] < k) {
                int x = pq.poll();
                removed[x] = true;

                // b. décrémenter le degré entrant de tous ses voisins sortants
                for (int j = nodes[x].getFirstNeighboursIndex(); j < nodes[x].getFirstNeighboursIndex() + nodes[x].getNeighboursCount(); j++) {
                    int y = edges[j];
                    if (!removed[y]) {
                        inDegree[y]--;
                        pq.remove(y);
                        pq.offer(y);
                    }
                }

                // c. Si on enlève v on s’arrête et on retourne k − 1
                if (x == v) {
                    return k - 1;
                }
            }
        }

        // Si v n’est pas enlevé, cela signifie qu'il est dans le n-coeur entrant
        return n - 1;
    }

    /**
     * Effectue le parsing des arguments de la commande et du fichier
     *
     * @param args tout les arguments de la commande.
     */
    private static void completeParsing(String[] args) {
        // Parsing du fichier
        parseFile(args[1]);

        // Traduction du x
        parseX(args[2]);
    }

    /**
     * Parse le numéro du sommet de départ et le stocke dans la variable globale "x".
     *
     * @param arg L'argument fourni.
     */
    private static void parseX(String arg) {
        try {
            x = Integer.parseInt(arg);

            for (Sommet s : nodes) {
                if (s.getName() == x) {
                    x = s.getId();
                    return;
                }
            }

            printError("Invalid starting node provided: " + arg + " is not a valid node number.");
        } catch (NumberFormatException e) {
            printError("Invalid starting node provided: " + arg + " is not a number.");
        }
    }

    /**
     * Parse le fichier et stocke les données dans les tableaux globaux (edges et nodes).
     * Effectue également une traduction des numéros des sommets du graphe.
     *
     * @param pathToFile Le chemin d'accès au fichier à parser.
     */
    private static void parseFile(String pathToFile) {
        // On va stocker les données dans un dictionnaire avec :
        // clé = numéro de sommet
        // valeur = liste des sommets auquel le sommet clé est lié
        HashMap<Integer, ArrayList<Integer>> buffer = new HashMap<>();
        int totalDataLines = 0;
        String currentLine = "";

        try {
            // Initialisation du reader
            BufferedReader reader = new BufferedReader(new FileReader(pathToFile));

            while ((currentLine = reader.readLine()) != null) {
                // Si la ligne est un commentaire, on l'ignore
                if (currentLine.startsWith(COMMENT_START_TOKEN)) {
                    continue;
                }

                totalDataLines++;

                // Récupération des tokens de la ligne
                String[] tokens = {"", ""};
                int tokenIndex = 0;

                Matcher m = Pattern.compile("\\d+").matcher(currentLine);
                while (m.find()){
                    tokens[tokenIndex++] = m.group();
                }

                // Conversion des tokens en nombres
                int first = Integer.parseInt(tokens[0]);
                int second = Integer.parseInt(tokens[1]);

                // Stockage des données dans la map
                if (buffer.containsKey(first)) {
                    buffer.get(first).add(second);
                } else {
                    ArrayList<Integer> temp = new ArrayList<>();

                    temp.add(second);

                    buffer.put(first, temp);
                }
                if (!buffer.containsKey(second)) {
                    buffer.put(second, new ArrayList<>());
                }
            }

            // Fermeture du reader
            reader.close();

            // Initialisation des tableaux finaux
            nodes = new Sommet[buffer.keySet().size()];
            edges = new int[totalDataLines];

            // Variables locales
            HashMap<Integer, Integer> translationHelper = new HashMap<>();
            int nodesIndex = 0;
            int edgesIndex = 0;

            // Parcours des donnees pour le stockage dans les tableaux finaux
            for (Map.Entry<Integer, ArrayList<Integer>> data : buffer.entrySet()) {
                Sommet s = new Sommet(nodesIndex, data.getKey(), edgesIndex, data.getValue().size());

                translationHelper.put(data.getKey(), nodesIndex);
                nodes[nodesIndex++] = s;

                for (Integer value : data.getValue()) {
                    edges[edgesIndex++] = value;
                }
            }

            // Renommage des sommets dans la liste d'adjacence
            for (int i = 0; i < edges.length; i++) {
                edges[i] = translationHelper.get(edges[i]);
            }
        } catch (FileNotFoundException e) {
            printError("Provided file does not exist (" + pathToFile + "). "  + e.getMessage());
        } catch (NumberFormatException e) {
            printError("Invalid node number at line: not a number. "  + e.getMessage());
        } catch (IOException e) {
            printError("An unexpected error occurred during the file parsing: " + e.getMessage());
        } catch(IndexOutOfBoundsException e) {
            printError(currentLine + " is incorrect (have more than two values)" + e.getMessage());
        }
    }


    /**
     * Affiche "ERREUR" sur la sortie standard.
     * Peut également afficher des détails sur l'erreur survenue.
     *
     * @param details Les details de l'erreur (ou null si non-necessaire).
     */
    private static void printError(String details) {
        // Affichage de "ERREUR"
        System.out.print("ERREUR");

        // Affichage des details s'ils existent
        if (details != null) {
            System.out.print(": " + details + "\n");
        } else {
            System.out.println();
        }
    }

    /**
     * Représentation d'un sommet du graphe.
     */
    private static class Sommet {

        // Le numéro du sommet
        private final int id;
        // Le nom du sommet
        private final int name;
        // L'indice du premier voisin dans la liste d'adjacence
        private final int firstNeighboursIndex;
        // Le nombre de voisins
        private final int neighboursCount;

        /**
         * Construit un sommet à partir de son numéro et de son nom.
         *
         * @param id Le numéro du sommet.
         * @param name Le nom du sommet.
         * @param firstNeighboursIndex L'indice du premier voisin dans la liste d'adjacence.
         * @param neighboursCount Le nombre de voisins.
         */
        public Sommet(int id, int name, int firstNeighboursIndex, int neighboursCount) {
            this.id = id;
            this.name = name;
            this.firstNeighboursIndex = firstNeighboursIndex;
            this.neighboursCount = neighboursCount;
        }

        /**
         * Getter pour le numéro du sommet.
         *
         * @return Le numéro du sommet.
         */
        public int getId() {
            return id;
        }

        /**
         * Getter pour le nom du sommet.
         *
         * @return Le nom du sommet.
         */
        public int getName() {
            return name;
        }

        /**
         * Getter pour l'indice du premier voisin dans la liste d'adjacence.
         * 
         * @return L'indice du premier voisin dans la liste d'adjacence.
         */
        public int getFirstNeighboursIndex() {
            return firstNeighboursIndex;
        }

        /**
         * Getter pour le nombre de voisins.
         * 
         * @return Le nombre de voisins.
         */
        public int getNeighboursCount() {
            return neighboursCount;
        }

    }
}