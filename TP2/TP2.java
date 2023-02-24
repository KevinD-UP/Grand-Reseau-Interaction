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
    private static Sommet[] nodes; // Tableau des sommets
    private static int[] edges; // Tableau des arêtes
    private static int n; // Nombre de sommet
    private static int x; // Sommet considéré

    public static void main(String[] args) {
        if (args[0].equals(CARDIALITY_COMMAND)) {
            if (!completeParsing(args)) {
                return;
            }
            // Affichage de la cardialité entrante pour le sommet x
            System.out.println(cardialiteEntrante(x));
        } else if (args[0].equals(BETWEENNESS_CENTRALITY_COMMAND)){
            if (!completeParsing(args)) {
                return;
            }
            n = nodes.length;
            // Affichage du betweenness centrality pour le sommet x
            System.out.println(bet(x));
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
     * Calcule la betweenness centrality d'un sommet v.
     *
     * @param v Le sommet v.
     * @return La betweenness centrality du sommet v.
     */
    private static double bet(int v) {
        // Lancement d'un npcc ayant pour origine v (car il ne sera jamais calculé par le bet)
        int[][] dataV = npcc(v);
        int[] npccFromV = dataV[0];
        int[] distFromV = dataV[1];

        double res = 0.0;

        // Pour chaque sommet du graphe
        for (Sommet s : nodes) {
            var sId = s.getId();

            // Si s == v on ignore
            if (sId == v) {
                continue;
            }

            // Lancement du BFS avec s pour origine
            int[][] data = npcc(sId);
            int[] npccFromS = data[0];
            int[] distFromS = data[1];

            // Pour chaque sommet du graphe
            for (Sommet t : nodes) {
                var tId = t.getId();

                // Si t == v ou si s == t on ignore
                if (tId == v || sId == tId) {
                    continue;
                }

                if (npccFromS[tId] > 0) {
                    if (distFromS[tId] == distFromS[v] + distFromV[tId]) {
                        // On ajoute la centralité de v sur le trajet de s à t
                        res += (1.0 * npccFromS[v] * npccFromV[tId]) / (npccFromS[tId] * 1.0);
                    }
                }
            }
        }

        return (1.0 / ((n - 1) * (n - 2))) * res;
    }

    /**
     * Calcule le nombre de plus courts chemins entre le sommet s et tout les autres sommets.
     *
     * @param s Le sommet s.
     * @return Le nombre de plus courts chemins entre le sommet s et tout les autres sommets.
     */
    private static int[][] npcc(int s) {
        int[] npcc = new int[n];
        int[] dist = new int[n];

        Arrays.fill(npcc, 0);
        Arrays.fill(dist, Integer.MAX_VALUE);

        // Liste des sommets visités pendant le parcours
        boolean[] visited = new boolean[n];

        // Queue pour le parcours (contiendra la liste des sommets à visiter)
        Queue<Sommet> queue = new LinkedList<>();

        // On marque le sommet de départ comme visité, et on l'ajoute à la queue
        visited[s] = true;
        queue.add(nodes[s]);

        // On initialise la distance de s à lui-même et son nombre de plus court chemin
        dist[s] = 0;
        npcc[s] = 1;

        // Tant que la queue n'est pas vide
        while (!queue.isEmpty()) {
            // On récupère le dernier élément de la queue (et on trouve le sommet correspondant)
            Sommet temp = queue.poll();
            int tempId = temp.getId();

            // Pour chacun de ses voisins
            for (int i = temp.getFirstNeighboursIndex(); i < temp.getFirstNeighboursIndex() + temp.getNeighboursCount(); i++) {
                int id = edges[i];

                // Si le voisin n'a pas été visité
                if (!visited[id]) {
                    // On marque le voisin comme visité
                    visited[id] = true;
                    // On ajoute le voisin à la queue pour qu'il soit traité
                    queue.add(nodes[id]);
                    // On incrémente la distance du voisin par rapport au sommet de départ (s)
                    dist[id] = dist[tempId] + 1;
                }

                if (dist[tempId] < dist[id]) {
                    // On incrémente le nombre de plus courts chemins
                    npcc[id] += npcc[tempId];
                }
            }
        }

        return new int[][]{npcc, dist};
    }

    /**
     * Effectue le parsing des arguments de la commande et du fichier
     *
     * @param args Tous les arguments de la commande.
     * @return true si les arguments ont été parsés correctement, false sinon.
     */
    private static boolean completeParsing(String[] args) {
        // Parsing du fichier
        if (!parseFile(args[1])) {
            return false;
        }

        // Traduction du x
        return parseX(args[2]);
    }

    /**
     * Parse le numéro du sommet de départ et le stocke dans la variable globale "x".
     *
     * @param arg L'argument fourni.
     * @return true si l'argument a été parsé correctement, false sinon.
     */
    private static boolean parseX(String arg) {
        try {
            x = Integer.parseInt(arg);

            for (Sommet s : nodes) {
                if (s.getName() == x) {
                    x = s.getId();
                    return true;
                }
            }

            printError("Invalid starting node provided: " + arg + " is not a valid node number.");
            return false;
        } catch (NumberFormatException e) {
            printError("Invalid starting node provided: " + arg + " is not a number.");
            return false;
        }
    }

    /**
     * Parse le fichier et stocke les données dans les tableaux globaux (edges et nodes).
     * Effectue également une traduction des numéros des sommets du graphe.
     *
     * @param pathToFile Le chemin d'accès au fichier à parser.
     * @return true si l'argument a été parsé correctement, false sinon.
     */
    private static boolean parseFile(String pathToFile) {
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

            return true;
        } catch (FileNotFoundException e) {
            printError("Provided file does not exist (" + pathToFile + "). "  + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            printError("Invalid node number at line: not a number. "  + e.getMessage());
            return false;
        } catch (IOException e) {
            printError("An unexpected error occurred during the file parsing: " + e.getMessage());
            return false;
        } catch(IndexOutOfBoundsException e) {
            printError(currentLine + " is incorrect (have more than two values)" + e.getMessage());
            return false;
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