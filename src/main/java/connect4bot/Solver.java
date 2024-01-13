package connect4bot;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static connect4bot.Main.*;

public class Solver {
    public static void main(String[] args) {
        Arrays.fill(lowerBoundCache, -1);
        Arrays.fill(upperBoundCache, -1);
        String p1 =     "       \n" +
                        "   0   \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "  01   \n" +
                        "1 101  \n";

        String p2 =     "0      \n" +
                        "1      \n" +
                        "0      \n" +
                        "1      \n" +
                        "0      \n" +
                        "1101010\n";
        long state = encode(p1);
        System.out.println(state);
        System.out.println(decode(reflectPosition(state)));
        loadCaches();
        int movesMade = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
        System.gc();
        long start = System.currentTimeMillis();
        int eval = evaluatePosition(state, (movesMade & 1) ^ 1, WORST_EVAL, BEST_EVAL, movesMade);
        System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        System.out.println("Time: " + (System.currentTimeMillis() - start));
        System.out.println("Positions evaluated: " + positionsEvaluated);
        System.out.println("Eval: " + eval);
        if (eval == 0) System.out.println("Draw");
        else if (eval > 0) {
            int movesToWin = 22 - eval - (movesMade >>> 1);
            System.out.println("Player " + ((movesMade & 1) + 1) + " wins in " + movesToWin + " moves");
        } else {
            int movesToWin = 22 + eval - (movesMade + 1 >>> 1);
            System.out.println("Player " + (2 - (movesMade & 1)) + " wins in " + movesToWin + " moves");
        }
//        updateDatabase();
    }

    static long reflectPosition(long state) {
        long reflected = 0;
        for (int col = 0; col < 7; col++) {
            reflected += ((state >>> col * 6 & 0b111111) << (6 - col) * 6) + ((state >>> 42 + col * 3 & 0b111) << 42 + (6 - col) * 3);
        }
        return reflected;
    }

    static final int WORST_EVAL = -21;
    static final int BEST_EVAL = 21;
    static long positionsEvaluated = 0;
    static final int SIZE = 50_000_017;
    static long[] lowerBoundCache = new long[SIZE], upperBoundCache = new long[SIZE];
    static int[] lowerBoundValue = new int[SIZE], upperBoundValue = new int[SIZE];

    static int evaluatePosition(long state, int piece, int alpha, int beta, int movesMade) {
        positionsEvaluated++;
        if (movesMade == 42) return 0;
        int index = (int) (state % SIZE);
        beta = Math.min(beta, 21 - (movesMade >>> 1));
        alpha = Math.max(alpha, (movesMade + 1 >>> 1) - 21);
        if (lowerBoundCache[index] == state) alpha = Math.max(alpha, lowerBoundValue[index]);
        if (upperBoundCache[index] == state) {
            beta = Math.min(beta, upperBoundValue[index]);
            if (alpha >= beta) return beta;
        }
        if (alpha >= beta) return alpha;
        int[] threats = new int[7], order = Arrays.copyOf(moveOrder, 7);
        for (int col : moveOrder) {
            int height = (int) ((state >> 42 + col * 3) & 0b111);
            if (height != 6) {
                long move = nextState(state, piece, col, height);
                if (isWin(move, piece)) return 21 - (movesMade >>> 1);
                int moveIndex = (int) (move % SIZE);
                if (upperBoundCache[moveIndex] == move) alpha = Math.max(alpha, -upperBoundValue[moveIndex]);
                if (alpha >= beta) return alpha;
                threats[col] = countThreats(move, piece);
            }
        }
        sortByThreats(order, threats);
        int i = 0;
        for (int col : order) {
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            if (height != 6) {
                long move = nextState(state, piece, col, height);
                int eval;
                if (i++ == 0) eval = -evaluatePosition(move, piece ^ 1, -beta, -alpha, movesMade + 1);
                else {
                    eval = -evaluatePosition(move, piece ^ 1, -alpha - 1, -alpha, movesMade + 1);
                    if (eval > alpha && eval < beta) eval = -evaluatePosition(move, piece ^ 1, -beta, -alpha, movesMade + 1);
                }
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    if(movesMade < 16) lowerBounds.put(state, (byte) Math.max(alpha, lowerBounds.getOrDefault(state, (byte) WORST_EVAL)));
                    lowerBoundCache[index] = state;
                    lowerBoundValue[index] = alpha;
                    return alpha;
                }
            }
        }
        if(movesMade < 16) upperBounds.put(state, (byte) Math.min(alpha, upperBounds.getOrDefault(state, (byte) BEST_EVAL)));
        upperBoundCache[index] = state;
        upperBoundValue[index] = alpha;
        return alpha;
    }

    private static void sortByThreats(int[] order, int[] threats) {
        for (int i = 1; i < 7; i++) {
            for (int j = i; j > 0 && threats[order[j]] > threats[order[j - 1]]; j--) {
                int temp = order[j];
                order[j] = order[j - 1];
                order[j - 1] = temp;
            }
        }
    }

    static HashMap<Long, Byte> upperBounds = new HashMap<>(), lowerBounds = new HashMap<>();

    static void loadCaches() {
        fillCache(upperBoundCache, upperBoundValue, upperBounds, "upper.bin");
        fillCache(lowerBoundCache, lowerBoundValue, lowerBounds, "lower.bin");
    }

    static void updateDatabase() {
        updateDatabase(upperBounds, "upper.bin");
        updateDatabase(lowerBounds, "lower.bin");
    }

    static void updateDatabase(HashMap<Long, Byte> cache, String filename) {
        try {
            byte[] updatedBytes = new byte[cache.size() * 9];
            int i = 0;
            for (long state : cache.keySet()) {
                for (int j = 0; j < 64; j += 8) updatedBytes[i++] = (byte) (state >>> j & 0b11111111);
                updatedBytes[i++] = cache.get(state);
            }
            try (FileOutputStream out = new FileOutputStream(filename)) {
                out.write(updatedBytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void fillCache(long[] keys, int[] values, HashMap<Long, Byte> cache, String filename) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(filename));
            System.out.println(bytes.length / 9);
            for (int i = 0; i < bytes.length; i += 9) {
                long state = 0;
                for (int j = i; j < i + 8; j++) state += (long) (bytes[j] & 255) << (j - i << 3);
                keys[(int) (state % SIZE)] = state;
                byte bound = bytes[i + 8];
                values[(int) (state % SIZE)] = bound;
                cache.put(state, bound);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void convertToBinaryFile(String name) {
        try (BufferedReader in = new BufferedReader(new FileReader(name + ".txt"));
             FileOutputStream out = new FileOutputStream(name + ".bin")) {
            String line = in.readLine();
            ArrayList<Long> states = new ArrayList<>();
            ArrayList<Byte> bounds = new ArrayList<>();
            while (line != null) {
                String[] tokens = line.split(" ");
                states.add(Long.parseLong(tokens[0]));
                bounds.add(Byte.parseByte(tokens[1]));
                line = in.readLine();
            }
            byte[] bytes = new byte[states.size() * 9];
            int index = 0;
            for (int i = 0; i < states.size(); i++) {
                long state = states.get(i);
                for (int j = 0; j < 64; j += 8) {
                    bytes[index++] = (byte) (state >>> j & 0b11111111);
                }
                bytes[index++] = bounds.get(i);
            }
            out.write(bytes);
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    static void updateCache(long[] keys, int[] values, String filename) {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            String line = in.readLine();
            int i = 0;
            while (line != null) {
                String[] tokens = line.split(" ");
                long state = Long.parseLong(tokens[0]);
                int bound = Integer.parseInt(tokens[1]);
                int index = (int) (state % SIZE);
                keys[index] = state;
                values[index] = bound;
                line = in.readLine();
                i++;
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void mergeCaches() {
        mergeCaches(true);
        mergeCaches(false);
    }

    static void mergeCaches(boolean upper) {
        String name = upper ? "upper" : "lower";
        HashMap<Long, Integer> bounds = new HashMap<>();
        try {
            for (int i = 0; i < 2; i++) {
                BufferedReader in = new BufferedReader(new FileReader(name + i + ".txt"));
                String line = in.readLine();
                while (line != null) {
                    String[] tokens = line.split(" ");
                    long state = Long.parseLong(tokens[0]);
                    int bound = Integer.parseInt(tokens[1]);
                    bounds.put(state, upper ? Math.min(bound, bounds.getOrDefault(state, BEST_EVAL)) :
                            Math.max(bound, bounds.getOrDefault(state, WORST_EVAL)));
                    line = in.readLine();
                }
            }
            try (PrintWriter pw = new PrintWriter(name + "0.txt")) {
                bounds.forEach((state, bound) -> pw.println(state + " " + bound));
            }
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
}
