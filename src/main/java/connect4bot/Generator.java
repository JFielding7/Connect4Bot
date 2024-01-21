package connect4bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static connect4bot.Main.*;
import static connect4bot.Solver.*;

public class Generator {

    public static void main(String[] args) {
        Arrays.fill(lowerBoundCache, -1);
        Arrays.fill(upperBoundCache, -1);
        String p1 =     "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "   1   \n";
        System.out.println(decode(2608041581351040L));
        System.gc();
        loadCaches();
        System.gc();
        System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        long state = encode(p1);
        int movesMade = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
        int piece = (movesMade & 1) ^ 1;
        long start = System.currentTimeMillis();
//        System.out.println(bestMoves(state, piece, movesMade));
//        System.out.println(decode(bestMoves(state, piece, movesMade).get(0)));
//        System.out.println("Eval: " + Solver.evaluatePosition(state, piece, WORST_EVAL, BEST_EVAL, movesMade));
        generateLines(state, piece, movesMade, 3);
        System.out.println("Time: " + (System.currentTimeMillis() - start));
        System.out.println(optimalStates.size());
        int count = 0;
        for (long pos : optimalStates) {
            if (count == 0) System.out.println(decode(pos));
            count++;
        }
        System.out.println(positionsEvaluated);
//        updateDatabase();
//        long maxTime = 0;
//        long totalTime = 0;
//        for (long pos : optimalStates) {
//            if (count == count) {
//                System.out.println(decode(pos));
//                long s = System.currentTimeMillis();
//                bestMoves(pos, 0, 7);
//                long time = System.currentTimeMillis() - s;
//                System.out.println(time);
//                maxTime = Math.max(maxTime, time);
//                totalTime += time;
//            }
//            System.out.println(++count);
//            System.out.println(cache.size());
//            System.out.println();
//        }
//        System.out.println(count);
//        System.out.println(maxTime);
//        System.out.println(totalTime / 268);
    }

    static HashMap<Long, Byte> cache = new HashMap<>();
    static HashSet<Long> optimalStates = new HashSet<>();
    static int lines = 0;

    static void generateLines(long state, int piece, int movesMade, int depthRemaining) {
        if (depthRemaining == 0 || cache.containsKey(state) || cache.containsKey(reflectState(state))) return;
        int oppPiece = piece ^ 1;
        long time = System.currentTimeMillis();
        System.out.println(decode(state));
        ArrayList<Long> bestMoves = bestMoves(state, piece, movesMade);
        time = System.currentTimeMillis() - time;
        if (depthRemaining == 1) {
            System.out.println(lines++);
            System.out.println(state);
            System.out.println(time);
            for (long move : bestMoves) {
                for (long nextState : nextStates(move, oppPiece)) {
                    if (!optimalStates.contains(nextState) && !optimalStates.contains(reflectState(nextState))) optimalStates.add(nextState);
                }
            }
        }
        else {
            for (long move : bestMoves) {
                for (long nextState : nextStates(move, oppPiece)) {
                    generateLines(nextState, piece, movesMade + 2, depthRemaining - 1);
                }
            }
        }
    }

    static ArrayList<Long> bestMoves(long state, int piece, int movesMade) {
        ArrayList<Long> bestMoves = new ArrayList<>();
        int maxEval = WORST_EVAL;
        int i = 0;
        for (long move : nextStates(state, piece)) {
            if (isWin(move, piece)) {
                bestMoves.clear();
                bestMoves.add(move);
                cache.put(state, (byte) (21 - (movesMade >>> 1)));
                return bestMoves;
            }
            int eval;
            if (cache.containsKey(move)) eval = -cache.get(move);
            else if (cache.containsKey(reflectState(move))) eval = -cache.get(reflectState(move));
            else if (i == 0) eval = -Solver.evaluatePosition(move, piece ^ 1, WORST_EVAL, BEST_EVAL, movesMade + 1);
            else {
                if (maxEval < 0) {
                    eval = -Solver.evaluatePosition(move, piece ^ 1, -maxEval - 1, -maxEval + 1, movesMade + 1);
                    if (eval > maxEval) eval = -Solver.evaluatePosition(move, piece ^ 1, WORST_EVAL, BEST_EVAL, movesMade + 1);
                    else if (eval < maxEval) continue;
                }
                else {
                    eval = -Solver.evaluatePosition(move, piece ^ 1, -maxEval - 1, -maxEval, movesMade + 1);
                    if (eval > maxEval) eval = -Solver.evaluatePosition(move, piece ^ 1, WORST_EVAL, -maxEval, movesMade + 1);
                    else continue;
                }
            }
            if (eval > maxEval) {
                bestMoves.clear();
                bestMoves.add(move);
                maxEval = eval;
            }
            else if (eval == maxEval) bestMoves.add(move);
            cache.put(move, (byte) -eval);
            i++;
        }
        cache.put(state, (byte) maxEval);
        return bestMoves;
    }

    static ArrayList<Long> nextStates(long state, int piece) {
        ArrayList<Long> moves = new ArrayList<>();
        for (int i : moveOrder) {
            int height = (int) (state >>> 42 + i * 3 & 0b111);
            if (height < 6) moves.add(nextState(state, piece, i, height));
        }
        return moves;
    }

    static void compileCache() {
        try {
            byte[] bytes = Files.readAllBytes(Path.of("cache.bin"));
            for (int i = 0; i < bytes.length; i += 9) {
                long state = 0;
                for (int j = i; j < i + 8; j++) state += (long) (bytes[j] & 255) << (j - i << 3);
                cache.put(state, bytes[i + 8]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void loadCaches() {
        loadCache(upperBoundCache, upperBoundValues, upperBounds, "upperBounds.bin");
        loadCache(lowerBoundCache, lowerBoundValues, lowerBounds, "lowerBounds.bin");
    }

    static void updateDatabase() {
        Solver.filterSymmetricalPositions();
        Solver.updateDatabase(upperBounds, "upperBounds.bin");
        Solver.updateDatabase(lowerBounds, "lowerBounds.bin");
//        Solver.updateDatabase(cache, "cache.bin");
    }
}
