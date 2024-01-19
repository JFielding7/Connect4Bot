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
    static final int SIZE = 30_000_001;
    static long[] lowerBoundCache = new long[SIZE], upperBoundCache = new long[SIZE];
    static int[] lowerBoundValues = new int[SIZE], upperBoundValues = new int[SIZE];
    static int collisions = 0;
    static int wins = 0;
    static int oddMoves = 0;
    static int evenMoves = 0;
    //    0
    //  01
    //  10
    //0 11 1
    //1 10 0
    //0101100

    public static void main(String[] args) {
        Arrays.fill(lowerBoundCache, -1);
        Arrays.fill(upperBoundCache, -1);
        String p1 =     "   0   \n" +
                        "  01   \n" +
                        "  10   \n" +
                        "0 11 1 \n" +
                        "1 10 0 \n" +
                        "0101100\n";

        String p2 =     "   0   \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "   1   \n";
        Arrays.fill(heuristicLowerBounds, -1);
        Arrays.fill(heuristicUpperBounds, -1);
//        System.out.println(heuristicEvaluation(encode(p1), 1, -42, 42, 20));
//        System.exit(0);
        long state = encode(p1);
        System.out.println(state);
        System.out.println(depth(state));
//        loadCaches();
        int movesMade = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
        System.gc();
        long start = System.currentTimeMillis();
        int eval = evaluatePosition(state, (movesMade & 1) ^ 1, -1, 1, movesMade);
        System.out.println(Arrays.toString(frequency));
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
        System.out.println(wins);
        System.out.println(oddMoves);
        System.out.println(evenMoves);
//        updateDatabase();
    }

    static int evaluatePosition(long state, int piece, int alpha, int beta, int movesMade) {
        positionsEvaluated++;
        if (movesMade == 42) return 0;
        int index = (int) (state % SIZE);
        beta = Math.min(beta, 21 - (movesMade >>> 1));
        alpha = Math.max(alpha, (movesMade + 1 >>> 1) - 21);
        if (movesMade < 16 && lowerBounds.containsKey(state)) alpha = Math.max(alpha, lowerBounds.get(state));
        if (movesMade < 16 && upperBounds.containsKey(state)) beta = Math.min(beta, upperBounds.get(state));
        if (lowerBoundCache[index] == state) alpha = Math.max(alpha, lowerBoundValues[index]);
        if (upperBoundCache[index] == state) {
            beta = Math.min(beta, upperBoundValues[index]);
            if (alpha >= beta) return beta;
        }
        if (alpha >= beta) return alpha;
        int[] threats = new int[7], order = Arrays.copyOf(moveOrder, 7);
        int forcedMoves = 0;
        long forcedMove = -1;
        for (int col : moveOrder) {
            int height = (int) ((state >> 42 + col * 3) & 0b111);
            if (height != 6) {
                long move = nextState(state, piece, col, height);
                if (isWin(nextState(state, piece ^ 1, col, height), piece ^ 1)) {
                    forcedMoves++;
                    forcedMove = move;
                }
                if (isWin(move, piece)) {
                    wins++;
                    return 21 - (movesMade >>> 1);
                }
                int moveIndex = (int) (move % SIZE);
                if (upperBoundCache[moveIndex] == move) alpha = Math.max(alpha, -upperBoundValues[moveIndex]);
                if (movesMade < 15 && upperBounds.containsKey(move)) alpha = Math.max(alpha, -upperBounds.get(move));
                if (alpha >= beta) return alpha;
                threats[col] = countThreats(move, piece);
            }
        }
        if (forcedMoves > 0) return forcedMoves > 1 ? (movesMade + 1 >>> 1) - 21 : -evaluatePosition(forcedMove, piece ^ 1, -beta, -alpha, movesMade + 1);
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
                    frequency[i - 1]++;
                    if (piece == 0) {
                        oddMoves += (height & 1) ^ 1;
                        evenMoves += height & 1;
                    }
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

    static void sortByThreats(int[] order, int[] threats) {
        for (int i = 1; i < 7; i++) {
            for (int j = i; j > 0 && threats[order[j]] > threats[order[j - 1]]; j--) {
                int temp = order[j];
                order[j] = order[j - 1];
                order[j - 1] = temp;
            }
        }
    }

    static int heuristicCacheSize = 10000000;
    static long[] heuristicUpperBounds = new long[heuristicCacheSize], heuristicLowerBounds = new long[heuristicCacheSize];
    static int[] heuristicUpperValues = new int[heuristicCacheSize], heuristicLowerValues = new int[heuristicCacheSize];

    static int heuristicEvaluation(long state, int piece, int alpha, int beta, int depthRemaining) {
        if (depthRemaining == 0) return countThreats(state, piece) - countThreats(state, piece ^ 1);
        int index = (int) (state % heuristicCacheSize);
        if (heuristicLowerBounds[index] == state) alpha = Math.max(alpha, heuristicLowerValues[index]);
        if (heuristicUpperBounds[index] == state) beta = Math.min(beta, heuristicUpperValues[index]);
        if (alpha >= beta) return alpha;
        for (int col : moveOrder) {
            int height = (int) ((state >> 42 + col * 3) & 0b111);
            if (height != 6) {
                long move = nextState(state, piece, col, height);
                if (isWin(move, piece)) return 42;
                int moveIndex = (int) (move % heuristicCacheSize);
                if (heuristicUpperBounds[moveIndex] == move) alpha = Math.max(alpha, -heuristicUpperValues[moveIndex]);
                if (alpha >= beta) return alpha;
            }
        }
        int i = 0;
        for (int col : moveOrder) {
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            if (height != 6) {
                long move = nextState(state, piece, col, height);
                int heuristic;
                if (i++ == 0) heuristic = -heuristicEvaluation(move, piece ^ 1, -beta, -alpha, depthRemaining - 1);
                else {
                    heuristic = -heuristicEvaluation(move, piece ^ 1, -alpha - 1, -alpha, depthRemaining - 1);
                    if (heuristic > alpha && heuristic < beta) heuristic = -heuristicEvaluation(move, piece ^ 1, -beta, -alpha, depthRemaining - 1);
                }
                if (depthRemaining == 21) System.out.println(col + " " + heuristic);
                alpha = Math.max(alpha, heuristic);
                if (alpha >= beta) {
                    heuristicLowerBounds[index] = state;
                    heuristicLowerValues[index] = alpha;
                    return alpha;
                }
            }
        }
        heuristicUpperBounds[index] = state;
        heuristicUpperValues[index] = alpha;
        return alpha;
    }

    static int columnsControlled(long state, int piece) {
        int controlled = 0, oppPiece = piece ^ 1;
        long pieces = adjustBoard(state, piece), oppPieces = adjustBoard(state, oppPiece);
        for (int col = 0; col < 7; col++) {
            int threatBelow = -1;
            for (int row = (int) (state >>> 42 + col * 3 & 0b111); row < 6; row++) {
                long move = 1L << col * 7 + row;
                boolean playerThreat = connected4(pieces + move), oppThreat = connected4(oppPieces + move);
                if (playerThreat && threatBelow == piece) {
                    controlled++;
                    break;
                }
                if (oppThreat && threatBelow == oppPiece) {
                    controlled--;
                    break;
                }
                if (playerThreat) {
                    if ((row & 1) == oppPiece) {
                        controlled++;
                        break;
                    }
                    threatBelow = piece;
                }
                if (oppThreat) {
                    if ((row & 1) == piece) {
                        controlled--;
                        break;
                    }
                    threatBelow = oppPiece;
                }
                if (!playerThreat && !oppThreat) threatBelow = -1;
            }
        }
        return controlled;
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
                cache.put(reflectState(state), bound);
                cacheState(reflectState(state), bound, keys, values);
                cacheState(state, bound, keys, values);
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
        byte UNDEFINED = 100;
        HashMap<Long, Byte> updatedCache = new HashMap<>();
        for (long state : cache.keySet()) {
            long reflected = reflectState(state);
            byte bound = cache.get(state), reflectedBound = updatedCache.getOrDefault(reflected, UNDEFINED);
            if (reflectedBound == UNDEFINED || (bound != reflectedBound && (bound > reflectedBound == flag))) {
                updatedCache.put(state, bound);
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
