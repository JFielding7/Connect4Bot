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
//        compileCache();
        int count = 0;
//        HashSet<Long> states = new HashSet<>();
//        for (long state : cache.keySet()) {
//            if (depth(state) == 1) states.addAll(nextStates(state, 1));
//        }
//        System.out.println(states.size());

//        System.exit(0);
        loadCaches();
        long state = encode(p1);
        int movesMade = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
        int piece = (movesMade & 1) ^ 1;
        long start = System.currentTimeMillis();
        generateLines(state, piece, movesMade, 3);
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("Lines: " + optimalStates.size());
        long maxTime = 0;
        long totalTime = 0;
        for (long pos : optimalStates) {
            long s = System.currentTimeMillis();
            bestMoves(pos, 0, 7);
            long time = System.currentTimeMillis() - s;
            System.out.println(time);
            maxTime = Math.max(maxTime, time);
            totalTime += time;
            System.out.println(++count);
            System.out.println(cache.size());
        }
        System.out.println(count);
        System.out.println(maxTime);
        System.out.println(totalTime / 5);
        updateDatabase();
    }

    static HashMap<Long, Byte> cache = new HashMap<>();
    static HashSet<Long> optimalStates = new HashSet<>();
    static int lines = 0;

    static void generateLines(long state, int piece, int movesMade, int depthRemaining) {
        System.out.println(decode(state));
        if (depthRemaining == 0 || cache.containsKey(state) || cache.containsKey(reflectState(state))) return;
        int oppPiece = piece ^ 1;
        ArrayList<Long> bestMoves = bestMoves(state, piece, movesMade);
        if (depthRemaining == 1) {
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
            int eval;
            if (cache.containsKey(move)) eval = -cache.get(move);
            else if (cache.containsKey(reflectState(move))) eval = -cache.get(reflectState(move));
            else {
                if (i++ == 0) eval = -Solver.evaluatePosition(move, piece ^ 1, WORST_EVAL, BEST_EVAL, movesMade + 1);
                else {
                    eval = -Solver.evaluatePosition(move, piece ^ 1, -maxEval - 1, -maxEval + 1, movesMade + 1);
                    if (eval > maxEval) eval = -Solver.evaluatePosition(move, piece ^ 1, WORST_EVAL, BEST_EVAL, movesMade + 1);
                }
            }
            if (eval > maxEval) {
                bestMoves.clear();
                bestMoves.add(move);
                maxEval = eval;
            }
            else if (eval == maxEval) bestMoves.add(move);
            cache.put(move, (byte) -eval);
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
        loadCache(upperBoundCache, upperBoundValues, upperBounds, "upper0.bin");
        loadCache(lowerBoundCache, lowerBoundValues, lowerBounds, "lower0.bin");
    }

    static void updateDatabase() {
        Solver.updateDatabase(upperBounds, "upper0.bin");
        Solver.updateDatabase(lowerBounds, "lower0.bin");
        Solver.updateDatabase(cache, "cache.bin");
    }
}
