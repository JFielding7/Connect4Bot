package connect4bot;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static connect4bot.Main.*;

public class Solver {

    static final int WORST_EVAL = -21;
    static final int BEST_EVAL = 21;
    static long positionsEvaluated = 0;
    static final int SIZE = 55_000_027;
    static long[] lowerBoundCache = new long[SIZE], upperBoundCache = new long[SIZE];
    static int[] lowerBoundValues = new int[SIZE], upperBoundValues = new int[SIZE];
    static int collisions = 0;

    public static void main(String[] args) {
        Arrays.fill(lowerBoundCache, -1);
        Arrays.fill(upperBoundCache, -1);
        String p1 =     "       \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "   10  \n";

        String p2 =     "0      \n" +
                        "1      \n" +
                        "0      \n" +
                        "1      \n" +
                        "0      \n" +
                        "1101010\n";
        long state = encode(p1);
        System.out.println(state);
        System.out.println(depth(state));
        loadCaches();
        int movesMade = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
        System.gc();
        long start = System.currentTimeMillis();
        int eval = evaluatePosition(state, (movesMade & 1) ^ 1, WORST_EVAL, BEST_EVAL, movesMade);
        System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        System.out.println(collisions);
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

    static int evaluatePosition(long state, int piece, int alpha, int beta, int movesMade) {
        positionsEvaluated++;
        if (movesMade == 42) return 0;
        int index = (int) (state % SIZE);
        beta = Math.min(beta, 21 - (movesMade >>> 1));
        alpha = Math.max(alpha, (movesMade + 1 >>> 1) - 21);
        if (lowerBoundCache[index] == state) alpha = Math.max(alpha, lowerBoundValues[index]);
        if (upperBoundCache[index] == state) {
            beta = Math.min(beta, upperBoundValues[index]);
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
                if (upperBoundCache[moveIndex] == move) alpha = Math.max(alpha, -upperBoundValues[moveIndex]);
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
                    if (lowerBoundCache[index] != -1) collisions++;
                    lowerBoundCache[index] = state;
                    lowerBoundValues[index] = alpha;
                    return alpha;
                }
            }
        }
        if(movesMade < 16) upperBounds.put(state, (byte) Math.min(alpha, upperBounds.getOrDefault(state, (byte) BEST_EVAL)));
        if (upperBoundCache[index] != -1) collisions++;
        upperBoundCache[index] = state;
        upperBoundValues[index] = alpha;
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

    static long reflectState(long state) {
        long reflected = 0;
        for (int col = 0; col < 7; col++) {
            reflected += ((state >>> col * 6 & 0b111111) << (6 - col) * 6) + ((state >>> 42 + col * 3 & 0b111) << 42 + (6 - col) * 3);
        }
        return reflected;
    }

    static HashMap<Long, Byte> upperBounds = new HashMap<>(), lowerBounds = new HashMap<>();

    static void updateDatabase() {
//        filterSymmetricalPositions();
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

    static void loadCaches() {
        loadCache(upperBoundCache, upperBoundValues, upperBounds, "upper.bin");
        loadCache(lowerBoundCache, lowerBoundValues, lowerBounds, "lower.bin");
    }

    static void loadCache(long[] keys, int[] values, HashMap<Long, Byte> cache, String filename) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(filename));
            System.out.println(bytes.length / 9);
            for (int i = 0; i < bytes.length; i += 9) {
                long state = 0;
                for (int j = i; j < i + 8; j++) state += (long) (bytes[j] & 255) << (j - i << 3);
                byte bound = bytes[i + 8];
                cache.put(state, bound);
                cacheState(state, bound, keys, values);
                cacheState(reflectState(state), bound, keys, values);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void cacheState(long state, int bound, long[] keys, int[] values) {
        int index = (int) (state % SIZE);
        if (keys[index] == -1 || depth(state) <= depth(keys[index])) {
            keys[index] = state;
            values[index] = bound;
        }
    }

    static int depth(long state) {
        int depth = 0;
        for (int i = 42; i < 63; i+=3) {
            depth += (int) (state >>> i & 0b111);
        }
        return depth;
    }

    static void filterSymmetricalPositions() {
        System.out.println("before");
        System.out.println(upperBounds.size());
        System.out.println(lowerBounds.size());
        upperBounds = filterSymmetricalPositions(upperBounds, false);
        lowerBounds = filterSymmetricalPositions(lowerBounds, true);
        System.out.println("after");
        System.out.println(upperBounds.size());
        System.out.println(lowerBounds.size());
    }

    static HashMap<Long, Byte> filterSymmetricalPositions(HashMap<Long, Byte> cache, boolean flag) {
        HashMap<Long, Byte> updatedCache = new HashMap<>();
        for (long state : cache.keySet()) {
            long reflected = reflectState(state);
            if (!updatedCache.containsKey(reflected) || (cache.get(state) > updatedCache.get(reflected) == flag)) {
                updatedCache.put(state, cache.get(state));
            }
        }
        return updatedCache;
    }

    static void reflectPositions() {
        for (int i = 0; i < 2; i++) {
            HashMap<Long, Byte> reflectedPositions = new HashMap<>(), positions = i == 1 ? lowerBounds : upperBounds;
            positions.forEach((state, bound) -> reflectedPositions.put(reflectState(state), bound));
            positions.putAll(reflectedPositions);
        }
        System.out.println(lowerBounds.size());
        System.out.println(upperBounds.size());
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
}
