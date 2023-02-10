import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TP2 {

    // Constantes
    private static final String COMMENT_START_TOKEN = "#";
    private static final String CARDIALITY_COMMAND  = "-c";
    private static final String BETWEENNESS_CENTRALITY_COMMAND = "-b";

    // Données
    private static Sommet[] nodes;
    private static int[] edges;
    private static int x;
    private static int maxDegree;
    private static boolean xTranslated;

    public static void main(String[] args) {
        if(args[0] == CARDIALITY_COMMAND){
            //TODO: cardialité
            completeParsing(args);
            // Affichage de la cardialité entrante pour le sommet x
        } else if (args[0] == BETWEENNESS_CENTRALITY_COMMAND){
            //TODO: betweenness
            completeParsing(args);
            // Affichage du betweenness centrality pour le sommet x
        } else {
            printError("Invalid option : " + args[0] + " doesn't exist, use -c or -b");
        }
    }

    private static void completeParsing(String[] args) {
        // Traduction du x
        if (!parseX(args[2])) {
            return;
        }

        // Parsing du fichier
        if (!parseFile(args[1])) {
            return;
        }

        if (!xTranslated) {
            printError("Invalid starting node provided: " + x + " is not a valid node number.");
            return;
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
        HashMap<Integer, Integer> translationHelper = new HashMap<>();
        int totalDataLines = 0;
        int nodesIndex = 0;
        String currentLine;

        try {
            // Initialisation du reader
            BufferedReader reader = new BufferedReader(new FileReader(pathToFile));

            while ((currentLine = reader.readLine()) != null) {
                // Si la ligne est un commentaire, on l'ignore
                if (currentLine.startsWith(COMMENT_START_TOKEN)) {
                    continue;
                }

                // Incrémentation du nombre de ligne
                totalDataLines++;

                // Récupération des tokens de la ligne
                char[] chars = currentLine.toCharArray();
                String[] tokens = {"", ""};
                int length = chars.length;
                int tokenIndex = 0;
                int index = 0;

                while (index < length) {
                    if (Character.isWhitespace(chars[index])) {
                        tokenIndex = 1;
                        index++;
                        continue;
                    }
                    tokens[tokenIndex] += chars[index];
                    index++;
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

                    // Renommage du sommet
                    translationHelper.put(first, nodesIndex);

                    // Traduction du x (si pas encore fait)
                    if (!xTranslated && x == first) {
                        x = nodesIndex;
                        xTranslated = true;
                    }

                    nodesIndex++;
                }
                if (!buffer.containsKey(second)) {
                    buffer.put(first, new ArrayList<>());

                    // Renommage du sommet
                    translationHelper.put(second, nodesIndex);

                    // Traduction du x (si pas encore fait)
                    if (!xTranslated && x == first) {
                        x = nodesIndex;
                        xTranslated = true;
                    }

                    nodesIndex++;
                }
            }

            // Fermeture du reader
            reader.close();

            // Initialisation des tableaux finaux
            nodes = new Sommet[nodesIndex];
            edges = new int[totalDataLines];

            // Variables locales
            int edgesIndex = 0;
            nodesIndex = 0;

            // Parcours des donnees pour le stockage dans les tableaux finaux
            for (Map.Entry<Integer, ArrayList<Integer>> data : buffer.entrySet()) {
                Sommet s = new Sommet(nodesIndex, data.getKey(), edgesIndex, data.getValue().size());

                nodes[nodesIndex++] = s;

                // On garde ici dans maxDegree, le degré maximum
                if(maxDegree < s.getNeighboursCount()) {
                    maxDegree = s.getNeighboursCount();
                }

                // Ajout des sommets dans la liste d'adjacence avec renommage
                for (Integer value : data.getValue()) {
                    edges[edgesIndex++] = translationHelper.get(value);
                }
            }

            return true;
        } catch (FileNotFoundException e) {
            printError("Provided file does not exist (" + pathToFile + ").");
            return false;
        } catch (NumberFormatException e) {
            printError("Invalid node number in the file.");
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
        // Indique si le sommet a été "enlevé" ou pas
        private boolean present;

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
            this.present = true; // Par défaut, le sommet "est présent" dans le graphe
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

        /**
         * Getter pour l'attribut present du sommet
         *
         * @return Un booleen indiquant si le sommet est present
         */
        public boolean getVisited() {
            return visited;
        }

        /**
         * Setter pour l'attribut present du sommet, "retire le sommet du graphe"
         */
        public void remove() {
            this.present = false;
        }
    }
}