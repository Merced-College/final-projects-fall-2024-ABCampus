//Adam Barakat (wrote all of it)
//CPSC-39
//Final Project
//

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Spellcast {
    private static final int GRID_SIZE = 5; //the grid is 5x5
    private static boolean doubleScore = false;
    private static boolean swapped = false;
    private static boolean allowSwap; //if the player can use a letter swap
    private static char[][] board; //double array for the board

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter 25 letters (5x5) (row by row, left to right): ");//player enters the board here
        String input = sc.next().toLowerCase();

        if (input.length() != 25) {
            System.out.println("Invalid input. Please enter exactly 25 letters.");
            return;
        }

        System.out.print("Is there 2x? Y/N");//sometimes the board doesn't contain a 2x letter
        char decision = sc.next().toUpperCase().charAt(0);

        int xcoord = 0;
        int ycoord = 0;

        if (decision == 'Y') {
            System.out.print("Enter row coordinate of 2x letter: ");//the 2x letter is a letter in a specific position, the player enters it here
            xcoord = sc.nextInt();
            System.out.print("Enter column coordinate of 2x letter: ");
            ycoord = sc.nextInt();
        }

        System.out.println("Swap? (Y/N)");//sometimes the player can afford to swap a letter, a powerful tool
        char usingSwap = sc.next().toUpperCase().charAt(0);
        allowSwap = (usingSwap == 'Y');

        board = new char[GRID_SIZE][GRID_SIZE];//grid creation
        int index = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                board[i][j] = input.charAt(index++);
            }
        }

        System.out.println("The 5x5 board is:");
        displayBoard(board);
        System.out.println("double letter: " + board[xcoord][ycoord]);//confirm the double letter

        //path to wordlist, change accordingly
        Set<String> dictionary = loadDictionary("wordlist.txt");

        Map<Character, Integer> letterScores = new HashMap<>();//letters have values based on how common t5hey are
        letterScores.put('a', 1); letterScores.put('e', 1); letterScores.put('i', 1); letterScores.put('o', 1);
        letterScores.put('n', 2); letterScores.put('r', 2); letterScores.put('s', 2); letterScores.put('t', 2);
        letterScores.put('d', 3); letterScores.put('g', 3); letterScores.put('l', 3);
        letterScores.put('b', 4); letterScores.put('h', 4); letterScores.put('p', 4); letterScores.put('m', 4); letterScores.put('u', 4); letterScores.put('y', 4);
        letterScores.put('c', 5); letterScores.put('f', 5); letterScores.put('v', 5); letterScores.put('w', 5);
        letterScores.put('k', 6);
        letterScores.put('j', 7); letterScores.put('x', 7);
        letterScores.put('q', 8); letterScores.put('z', 8);

        List<WordEntry> foundWords = new ArrayList<>();

        for (String word : dictionary) {//checks each dictionary word on the board
            if (canFormWord(board, word, xcoord, ycoord)) {
                double score = calculateScore(board, word, letterScores);
                if (doubleScore) {//double letter = double score
                    score *= 2;
                }
                if (word.length() >= 6) {//long word bonus is 10
                    score += 10;
                }
                String swapString = swapped ? " YES" : " no";
                foundWords.add(new WordEntry(word, (int)score, swapString));
            }
        }

        foundWords.sort((a, b) -> Integer.compare(b.score, a.score));//sorts words found by score

        int numToShow = 1000;
        System.out.println("Words found on the board (sorted by score):");
        int i = 0;
        for (WordEntry entry : foundWords) {//displays found words/scores
            System.out.println(entry.word + " - " + entry.score + " points" + entry.swapUsed);
            i++;
            if (i > numToShow) {
                break;
            }
        }
    }

    private static Set<String> loadDictionary(String filename) {//loads dictionary with buffered reader ie. each line = 1 word
        Set<String> dictionary = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String word;
            int count = 0;
            while ((word = br.readLine()) != null) {
                if (!word.isEmpty()) {
                    dictionary.add(word.toLowerCase());
                    count++;
                }
            }
            System.out.println("Words loaded: " + count);
        } catch (IOException e) {
            System.out.println("bad wordlist");//Most bad wordlist path issues will end up here
        }
        return dictionary;
    }

    private static void displayBoard(char[][] board) {//displays the board (for reference)
        for (int i = 0; i < GRID_SIZE; ++i) {
            for (int j = 0; j < GRID_SIZE; ++j) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
//performs a depth first search for the words
    private static boolean dfs(char[][] board, String word, int x, int y, int index, boolean[][] visited, boolean swapUsed) {
        swapped = false;//resets the swap each call, if swapping isn't allowed, it's as if it's already used
        if (!allowSwap) {
            swapUsed = true;
        }
        if (index == word.length()) return true;//all matched = done
        if (x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE || visited[x][y]) return false;//makes sure you're in the boundries and non-duplicate letters

        if (board[x][y] != word.charAt(index) && swapUsed) return false;//if we cant swap and there is no match, word = fail

        boolean usedSwap = board[x][y] != word.charAt(index);

        visited[x][y] = true;//marks visited letters
        int[] dx = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dy = {-1, 0, 1, 1, 1, 0, -1, -1};

        for (int dir = 0; dir < 8; dir++) {//each letter can be branched out in 8 directions, this is how all 8 are explored
            if (dfs(board, word, x + dx[dir], y + dy[dir], index + 1, visited, swapUsed || usedSwap)) {
                swapped = swapUsed || usedSwap;
                return true;
            }
        }

        visited[x][y] = false;
        return false;
    }

    private static boolean canFormWord(char[][] board, String word, int xcoord, int ycoord) {//checks if a word can be formed using dfs, including doubling
        boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; ++i) {
            for (int j = 0; j < GRID_SIZE; ++j) {
                if (dfs(board, word, i, j, 0, visited, false)) {
                    if (i == xcoord && j == ycoord) {
                        doubleScore = true;
                    } else {
                        doubleScore = false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static int calculateScore(char[][] board, String word, Map<Character, Integer> letterScores) {//calculate score based on letter value
        int score = 0;
        for (char c : word.toCharArray()) {
            Integer val = letterScores.get(c);
            if (val != null) {
                score += val;
            }
        }
        return score;
    }

    private static class WordEntry {//struct class for holding word info
        String word;
        int score;
        String swapUsed;
        WordEntry(String w, int s, String su) {
            word = w;
            score = s;
            swapUsed = su;
        }
    }
}
