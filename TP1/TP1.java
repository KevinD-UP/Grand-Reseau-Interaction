import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TP1 {

    // Constantes
    private static final String COMMENT_START_TOKEN = "#";

    // Données
    private static Sommet[] nodes;
    private static int[] edges;
    private static int x;
    private static int maxDegree;

    public static void main(String[] args) {
        // Parsing du fichier
        if (!parseFile(args[0])) {
            return;
        }

        // Traduction du x
        if (!parseX(args[1])) {
            return;
        }

        // Parcours en largeur
        int[] bfsResult = bfs(x);

        // Affichage des resultats
        System.out.println(nodes.length);     // Sn -> taille du tableau des sommets / nombre de sommets
        System.out.println(edges.length);     // m -> nombre d'arête (sans compter les doublons)
        System.out.println(maxDegree);        // degré max -> le plus grand nombre de voisins
        System.out.println(bfsResult[0]);     // nombre de sommets accessibles depuis x (args[1])
        System.out.println(bfsResult[1]);     // eccentricite de x -> longueur du plus long des chemins partant de x
        System.out.println(bfsResult[2]);     // nombre de composantes connexes
    }

    /**
     * Effectue un parcours en largeur du graphe et calcule plusieurs valeurs.
     * - Nombre de sommets accessibles depuis le sommet donne en argument.
     * - Taille du plus long chemin partant de x.
     * - Nombre de composantes connexes du graphe.
     * 
     * @param x Le numéro du sommet de départ.
     * @return Un tableau de 3 cases contenant (dans l'ordre) les valeurs décrites précédemment.
     */
    public static int[] bfs(int x) {
        // Tableau des distances
        int[] distances = new int[nodes.length];

        // Queue pour le parcours (contiendra la liste des sommets à visiter)
        Queue<Sommet> queue = new LinkedList<>();

        // Compteur du nombre de sommets accessibles depuis x
        int accessiblesFromXCount = 1;

        // Longueur du plus long chemin partant de x
        int maxPathFromX = 0;

        // On marque le sommet de départ comme visité, et on l'ajoute à la queue
        nodes[x].markVisited();
        queue.add(nodes[x]);

        // Tant que la queue n'est pas vide
        while (!queue.isEmpty()) {
            // On récupère le dernier élément de la queue (et on trouve le sommet correspondant)
            Sommet temp = queue.poll();

            // Pour chacun de ses voisins
            for (int i = temp.getFirstNeighboursIndex(); i < temp.getFirstNeighboursIndex() + temp.getNeighboursCount(); i++) {
                int id = edges[i];
                // Si le sommet n'a pas été visité
                if (!nodes[id].getVisited()) {
                    // On incremente le compteur du nombre de sommet accessibles depuis x
                    accessiblesFromXCount++;
                    // On marque le sommet comme visité
                    nodes[id].markVisited();
                    // On ajoute le sommet a la queue pour qu'il soit traité
                    queue.add(nodes[id]);
                    // On incrémente la distance du sommet par rapport au sommet de départ (temp)
                    distances[id] = 1 + distances[temp.getId()];
                    // On met a jour la taille du plus long chemin si besoin
                    maxPathFromX = Math.max(maxPathFromX, distances[id]);
                }
            }
        }

        // Compteur des composantes connexes
        int bfsCount = 1;

        // On parcourt chaque sommet et on vérifie qu'il a été marqué pendant le parcours
        for (Sommet s : nodes) {
            // Si ce n'est pas le cas, on relance un parcours depuis ce sommet et on incrémente le compteur
            if (!nodes[s.getId()].getVisited()) {
                bfsCount += bfs(s.getId())[2];
            }
        }

        // Renvoi des valeurs
        return new int[]{accessiblesFromXCount, maxPathFromX, bfsCount};
    }

    /**
     * Parse le numéro du sommet de départ et le stocke dans la variable globale "x".
     *
     * @param arg L'argument fourni.
     * @return true si l'argument a été parsé correctement, false sinon.
     */
    public static boolean parseX(String arg) {
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
     * @param pathToFile Le chemin d'acces au fichier a parser.
     * @return true si le parsing s'est bien passe, false sinon.
     */
    public static boolean parseFile(String pathToFile) {
        // On va stocker les données dans un dictionnaire avec :
        // clé = numéro de sommet
        // valeur = liste des sommets auquel le sommet clé est lié
        HashMap<Integer, ArrayList<Integer>> buffer = new HashMap<>();
        int totalDataLines = 0;
        String currentLine;

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

                // On garde ici dans maxDegree, le degré maximum
                if(maxDegree < s.getNeighboursCount()) {
                    maxDegree = s.getNeighboursCount();
                }

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
            printError("Provided file does not exist (" + pathToFile + ").");
            return false;
        } catch (NumberFormatException e) {
            printError("Invalid node number at line: not a number.");
            return false;
        } catch (IOException e) {
            printError("An unexpected error occurred during the file parsing: " + e.getMessage());
            return false;
        }
    }

    /**
     * Affiche "ERREUR" sur la sortie standard.
     * Peut également afficher des détails sur l'erreur survenue.
     *
     * @param details Les details de l'erreur (ou null si non-necessaire).
     */
    public static void printError(String details) {
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
        // Indique si le sommet a déjà été visité ou pas
        private boolean visited;

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
            this.visited = false; // Par défaut, un sommet n'est pas visité
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

        /**
         * Getter pour l'attribut visited du sommet
         *
         * @return Un booleen indiquant si le sommet à déjà été visité
         */
        public boolean getVisited() {
            return visited;
        }

        /**
         * Setter pour l'attribut visited du sommet, marque le sommet comme visité
         */
        public void markVisited() {
            this.visited = true;
        }
    }
}
