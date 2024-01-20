package connect4bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Engine {

    static final int WORST_EVAL = -18;
    static final int BEST_EVAL = 18;
    static final int SIZE = 30_000_001;
    static long[] lowerBoundCache = new long[SIZE], upperBoundCache = new long[SIZE];
    static int[] lowerBoundValues = new int[SIZE], upperBoundValues = new int[SIZE];
    private static final HashMap<Long, Byte> lowerBounds = loadCache(lowerBoundCache, lowerBoundValues, "lower1.bin");
    private static final HashMap<Long, Byte> upperBounds = loadCache(upperBoundCache, upperBoundValues, "upper1.bin");
    private static final int[] moveOrder = {3, 2, 4, 5, 1, 6, 0};
    private static Random rng = new Random();

    public static void main(String[] args) {
        System.out.println(Main.decode(176484810408620032L));
        System.out.println(makeOptimalMove(176484810408620032L, 0, 11));
    }

    static int makeOptimalMove(long state, int piece, int movesMade) {
        ArrayList<Integer> bestMoves = new ArrayList<>();
        int maxEval = WORST_EVAL, i = 0;
        for (int col : moveOrder) {
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            if (height == 6) continue;
            long move = nextState(state, piece, col, height);
            if (isWin(move, piece)) return col;
            int eval;
            if (i == 0) eval = -evaluatePosition(move, piece ^ 1, WORST_EVAL, BEST_EVAL, movesMade + 1);
            else {
                if (maxEval < 0) {
                    eval = -evaluatePosition(move, piece ^ 1, -maxEval - 1, -maxEval + 1, movesMade + 1);
                    if (eval > maxEval) eval = -evaluatePosition(move, piece ^ 1, WORST_EVAL, BEST_EVAL, movesMade + 1);
                    else if (eval < maxEval) continue;
                }
                else {
                    eval = -evaluatePosition(move, piece ^ 1, -maxEval - 1, -maxEval, movesMade + 1);
                    if (eval > maxEval) eval = -evaluatePosition(move, piece ^ 1, WORST_EVAL, -maxEval, movesMade + 1);
                    else continue;
                }
            }
            if (eval > maxEval) {
                bestMoves.clear();
                bestMoves.add(col);
                maxEval = eval;
            }
            else if (eval == maxEval) bestMoves.add(col);
            i++;
        }
        return bestMoves.get(rng.nextInt(bestMoves.size()));
    }

    static int evaluatePosition(long state, int piece, int alpha, int beta, int movesMade) {
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
                if (isWin(move, piece)) return 21 - (movesMade >>> 1);
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
                    if(movesMade < 16) lowerBounds.put(state, (byte) Math.max(alpha, lowerBounds.getOrDefault(state, (byte) WORST_EVAL)));
                    lowerBoundCache[index] = state;
                    lowerBoundValues[index] = alpha;
                    return alpha;
                }
            }
        }
        if(movesMade < 16) upperBounds.put(state, (byte) Math.min(alpha, upperBounds.getOrDefault(state, (byte) BEST_EVAL)));
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

    static long nextState(long state, int piece, int col, int height){
        if(piece == 1) state += 1L << col * 6 + height;
        return state + (1L << 42 + col * 3);
    }

    static int countThreats(long state, int piece) {
        int threatCount = 0;
        long board = adjustBoard(state, piece);
        for (int col = 0; col < 7; col++) {
            for (int row = (int) (state >>> 42 + col * 3 & 0b111); row < 6; row++) {
                if (connected4(board + (1L << col * 7 + row))) threatCount += 1 + ((row & 1) ^ piece);
            }
        }
        return threatCount;
    }

    static long adjustBoard(long state, int piece) {
        long board = 0;
        if (piece == 1)
            for (int i = 0; i < 7; i++) board += (state >>> (6 * i) & 0b111111) << (7 * i);
        else
            for (int i = 0; i < 7; i++) board += ((state >>> (6 * i) & 0b111111) ^ ((1 << (state >> (42 + i * 3) & 0b111)) - 1)) << (7 * i);
        return board;
    }

    static boolean connected4(long board) {
        for (int i = 1; i < 9; i += 1 / i * 4 + 1) {
            long connections = board;
            for (int j = 0; j < 3; j++) connections = connections & (connections >>> i);
            if (connections != 0) return true;
        }
        return false;
    }

    static boolean isWin(long state, int piece) { return connected4(adjustBoard(state, piece)); }

    static HashMap<Long, Byte> loadCache(long[] keys, int[] values, String filename) {
        HashMap<Long, Byte> cache = new HashMap<>();
        Arrays.fill(keys, -1);
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
        return cache;
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

    static long reflectState(long state) {
        long reflected = 0;
        for (int col = 0; col < 7; col++) {
            reflected += ((state >>> col * 6 & 0b111111) << (6 - col) * 6) + ((state >>> 42 + col * 3 & 0b111) << 42 + (6 - col) * 3);
        }
        return reflected;
    }
}
