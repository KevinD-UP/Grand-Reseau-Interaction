import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TP3 {

    // Constantes
    private static final String WATTS_STROGATZ_COMMAND = "-w";
    private static final String Kleinberg_COMMAND      = "-k";

    public static void main(String[] args) {
        if (args[0].equals(WATTS_STROGATZ_COMMAND)) {

            // Récupération des arguments de la commande
            String outputFilename = args[1];
            int n = Integer.parseInt(args[2]);
            int k = Integer.parseInt(args[3]);
            double p = Double.parseDouble(args[4]);
            int origine = Integer.parseInt(args[5]);
            int cible = Integer.parseInt(args[6]);

            // Ici le graphe est représenté par une matrice d'adjacence.
            int[][] graph = generateWattzStrogatzGraph(n, k, p);

            // Ecriture des fichiers .txt et .dot
            writeGraphToFileTxt(graph, outputFilename);
            writeGraphToFileDot(graph, outputFilename);

            // Routage glouton
            int[] path = greedyRouting(graph, origine, cible);
            if (path != null) {
                for (int node : path) {
                    System.out.print(node + " ");
                }
                System.out.println();
                System.out.println(path.length);
            } else {
                System.out.println("Glouton coincé, échec !");
            }
        } else if (args[0].equals(Kleinberg_COMMAND)){
            // TODO: Kleinberg
            System.out.println("Todo: Kleiberg");
        } else {
            printError("Invalid option : " + args[0] + " doesn't exist, use -w or -k");
        }
    }

    /**
     * Génère un graphe représentant un anneau de Wattz-Strogatz
     * @param n nombre de sommet
     * @param k k sommets situés avant et après un sommet n
     * @param p pourcentage de rebranchement
     * @return un graphe sous la forme d'une matrice d'adjacence.
     */
    public static int[][] generateWattzStrogatzGraph(int n, int k, double p) {
        int[][] graph = new int[n][n];
        Random rand = new Random();

        // Initialisation du graph
        for (int i = 0; i < n; i++) {
            for (int j = 1; j <= k; j++) {
                int neighbor = (i + j) % n;
                graph[i][neighbor] = 1;
                graph[neighbor][i] = 1;
            }
        }

        // Rebranchement avec probabilité p
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (graph[i][j] == 1 && rand.nextDouble() < p) {
                    // Rebranchement de l'arête i-j
                    List<Integer> candidates = new ArrayList<>();
                    for (int l = 0; l < n; l++) {
                        if (l != i && l != j && graph[i][l] == 0) {
                            candidates.add(l);
                        }
                    }
                    Collections.shuffle(candidates, rand);
                    int newNeighbor = candidates.get(0);
                    graph[i][j] = 0;
                    graph[j][i] = 0;
                    graph[i][newNeighbor] = 1;
                    graph[newNeighbor][i] = 1;
                }
            }
        }

        return graph;
    }

    /**
     * Effectue un routage glouton
     * @param graph graphe représenté par une matrice d'adjacence
     * @param origin sommet d'origine
     * @param cible sommet de destination
     * @return null si le routage échoue, un routage possible sinon
     */
    public static int[] greedyRouting(int[][] graph, int origin, int cible) {
        int n = graph.length;
        int[] coordinates = new int[n];
        for (int i = 0; i < n; i++) {
            coordinates[i] = i;
        }
        int current = origin;
        int[] path = new int[n];
        int length = 0;
        while (current != cible) {
            int bestNeighbor = -1;
            int bestDistance = Integer.MAX_VALUE;
            for (int neighbor : getNeighbors(graph, current)) {
                int distance = Math.abs(coordinates[neighbor] - coordinates[cible]);
                if (distance < bestDistance) {
                    bestNeighbor = neighbor;
                    bestDistance = distance;
                }
            }
            if (bestNeighbor == -1) {
                return null; // Echec du routage glouton
            }
            path[length++] = bestNeighbor;
            current = bestNeighbor;
        }
        int[] result = new int[length];
        System.arraycopy(path, 0, result, 0, length);
        return result;
    }

    /**
     * Donne la liste des voisins d'un sommet
     * @param graph graphe représenté par une matrice d'adjacence
     * @param vertex un sommet
     * @return une liste contenant les voisins d'un sommet
     */
    public static List<Integer> getNeighbors(int[][] graph, int vertex) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < graph.length; i++) {
            if (graph[vertex][i] == 1) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    /**
     * Ecrit le graphe dans un fichier en suivant le format de Stanford avec extension .txt
     * @param graph graphe représenté par une matrice d'adjacence
     * @param filename nom du fichier en sortie
     */
    public static void writeGraphToFileTxt(int[][] graph, String filename) {
        try {
            FileWriter writer = new FileWriter(filename + ".txt");
            for (int i = 0; i < graph.length; i++) {
                for (int j = 0; j < graph[i].length; j++) {
                    if (graph[i][j] == 1) {
                        writer.write(i + " " + j + "\n");
                    }
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ecrit le graphe dans un fichier en suivant le format de Stanford avec extension .dot
     * @param graph graphe représenté par une matrice d'adjacence
     * @param fileName nom du fichier en sortie
     */
    public static void writeGraphToFileDot(int[][] graph, String fileName) {
        try {
            FileWriter writer = new FileWriter(fileName+".dot");
            writer.write("graph " + fileName + " {\n");
            for (int i = 0; i < graph.length; i++) {
                for (int j = i + 1; j < graph.length; j++) {
                    if(graph[i][j] == 1) {
                        writer.write("    " + i + " -- " + j + ";\n");
                    }
                }
            }
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to file: " + e.getMessage());
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

}
