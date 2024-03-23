package connect4bot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static connect4bot.Engine.reflectState;
import static connect4bot.Solver.*;

public class Generator {

    static final int DEPTH = 15;
    static int[][][] combosCache = new int[DEPTH + 1][8][7];
    static final int[][] nCr = pascalsTriangle(DEPTH);
    static final int[][] comboSums = getComboSums(DEPTH);
    static final int[] posCount = getPositionCounts(DEPTH);

    private static int[] getPositionCounts(int n) {
        int[] counts = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            counts[i] = (int) positionsCount(i);
        }
        return counts;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        String board =  "   0   \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "   1   \n";
        long state = encode(board);
        int moves = board.length() - board.replaceAll("[01]", "").length();
        int piece = (moves & 1) ^ 1;
        System.out.println("Perfect Games: " + countPerfectGames(state, piece, moves));
        System.out.println("Perfect Positions: " + perfectGameCounts.size());
        System.out.println("Positions Evaluated: " + Solver.positions);
        System.out.println("Time: " + (System.currentTimeMillis() - start));
    }

    static HashMap<Long, Long> perfectGameCounts = new HashMap<>();

    static long countPerfectGames() {
        return countPerfectGames(0, 1, 0);
    }

    static long countPerfectGames(long state, int piece, int moves) {
        if (isWin(state, piece ^ 1)) return 1L;
        if (perfectGameCounts.containsKey(state)) return perfectGameCounts.get(state);
        long count = 0;
        for (long move : bestMoves(state, piece, moves)) {
            count += countPerfectGames(move, piece ^ 1, moves + 1);
        }
        perfectGameCounts.put(state, count);
        return count;
    }

    static ArrayList<Long> bestMoves(long state, int piece, int movesMade) {
        ArrayList<Long> bestMoves = new ArrayList<>();
        int maxEval = WORST_EVAL;
        for (int j = 0; j < 7; j++) {
            int col = MOVE_ORDER >>> j * 4 & 0b1111;
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            if (height == 6) continue;
            long move = nextState(state, piece, col, height);
            int eval;
            if (isWin(move, piece)) eval = 21 - (movesMade >>> 1);
            else eval = -evaluatePosition(move, piece ^ 1, WORST_EVAL, BEST_EVAL, movesMade + 1);
            if (eval > maxEval) {
                bestMoves.clear();
                bestMoves.add(move);
                maxEval = eval;
            }
            else if (eval == maxEval && !bestMoves.contains(reflectState(move))) bestMoves.add(move);
        }
        return bestMoves;
    }

    static long encode(String board) {
        String[] cols = new String[7];
        Arrays.fill(cols, "");
        for (String row : board.split("\n")) {
            for (int i = 0; i < 7 & i < row.length(); i++) {
                cols[i] += row.charAt(i) == ' ' ? "" : row.charAt(i);
            }
        }
        long pos = 0;
        for (int i = 0; i < 7; i++) {
            if (!cols[i].isEmpty()) pos += (Long.parseLong(cols[i], 2) << 36 - i * 6);
            pos += ((long) cols[i].length() << 60 - 3 * i);
        }
        return pos;
    }

    static String decode(long pos) {
        long[] heights = new long[7];
        for (int i = 0; i < 7; i++) {
            heights[i] = (pos >> 42 + 3 * i) & 7;
        }
        String board = "";
        for (int i = 5; i > -1; i--) {
            String row = "\n";
            for (int j = 6; j > -1; j--) {
                row += heights[j] <= i ? " " : (pos >> 6 * j + i) & 1;
            }
            board += row;
        }
        return board;
    }

    static byte[] compileDatabase(String file, int size, byte defaultVal) {
        byte[] database = new byte[posCount[size]];
        Arrays.fill(database, defaultVal);
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] bytes = in.readAllBytes();
            for (int i = 0; i < bytes.length; i += 9) {
                long state = 0;
                for (int j = i; j < i + 8; j++) state += (long) (bytes[j] & 255) << (j - i << 3);
                byte bound = bytes[i + 8];
                int depth = Engine.depth(state);
                database[getIndex(state, depth)] = bound;
                database[getIndex(reflectState(state), depth)] = bound;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(database[getIndex(0, 0)]);
        return database;
    }

    static void updateDatabase() {
        updateDatabase(Solver.lowerBoundDatabase, "/home/jpfielding/Connect4Bot/lowerBoundDatabase.bin");
        updateDatabase(Solver.upperBoundDatabase, "/home/jpfielding/Connect4Bot/upperBoundDatabase.bin");
    }

    static void updateDatabase(byte[] database, String file) {
        try(FileOutputStream out = new FileOutputStream(file)) {
            out.write(database);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static HashSet<Long> states = new HashSet<>();
    static int count = 0;

    static void solvePositions(long state, int piece, int depth, int movesMade) {
        if (depth == -1 || states.contains(state) || states.contains(reflectState(state))) return;
        states.add(state);
        if(Engine.isWin(state, piece ^ 1)) return;
//        System.out.println(decode(state));
        int eval = Solver.evaluatePosition(state, piece, WORST_EVAL, Solver.BEST_EVAL, movesMade);
//        System.out.println("Eval: " + eval);
//	    System.out.println("Positions: " + ++count);
        int index = getIndex(state, movesMade);
        Solver.lowerBoundDatabase[index] = (byte) Math.max(eval, Solver.lowerBoundDatabase[index]);
        index = getIndex(reflectState(state), movesMade);
        Solver.lowerBoundDatabase[index] = (byte) Math.max(eval, Solver.lowerBoundDatabase[index]);
        for (int i = 0; i < 7; i++) {
            int height = (int) (state >>> 42 + i * 3 & 0b111);
            if (height != 6) {
                long move = Engine.nextState(state, piece, i, height);
                solvePositions(move, piece ^ 1, depth - 1, movesMade + 1);
            }
        }
    }

    static int getIndex(long state, int pieces) {
        if (pieces == 0) return 0;
        int index = posCount[pieces - 1];
        int combos = nCr[pieces][pieces >>> 1];
        int p2Pieces = pieces >>> 1, p1Pieces = pieces - p2Pieces;
        for (int col = 0; col < 7 && pieces > 0; col++) {
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            index += (comboSums[6 - col][pieces] - comboSums[6 - col][pieces - height]) * combos;
            for (int row = 0; row < height && p1Pieces > 0 && p2Pieces > 0; row++) {
                if ((state >>> col * 6 + row & 1) == 1) {
                    index += nCr[pieces - row - 1][p1Pieces];
                    p1Pieces--;
                }
                else p2Pieces--;
            }
            pieces -= height;
        }
        return index;
    }

    static int[][] pascalsTriangle(int n) {
        int[][] pascalsTriangle = new int[n + 1][];
        pascalsTriangle[0] = new int[]{1};
        for (int i = 1; i <= n; i++) {
            int[] prevRow = pascalsTriangle[i - 1];
            int[] row = pascalsTriangle[i] = new int[i + 1];
            row[0] = 1;
            row[i] = 1;
            for (int j = 1; j < i; j++) {
                row[j] = prevRow[j - 1] + prevRow[j];
            }
        }
        return pascalsTriangle;
    }

    static BigInteger posCount(int pieces) {
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i <= pieces; i++) {
            sum = sum.add(BigInteger.valueOf(combos(i, 0, 0)).multiply(fact(i)).divide(fact(i >> 1)).divide(fact((i + 1 >> 1))));
        }
        return sum;
    }

    static long positionsCount(int pieces) {
        long sum = 0;
        for (int i = 0; i <= pieces; i++) {
            sum += combos(i) * nCr[i][i >>> 1];
        }
        return sum;
    }

    static int[][] getComboSums(int n) {
        int[][] comboSums = new int[8][n + 1];
        for (int cols = 0; cols <= 7; cols++) {
            int[] sums = comboSums[cols] = new int[n + 1];
            sums[0] = 1;
            for (int pieces = 1; pieces <= n; pieces++) {
                sums[pieces] = (int) combos(pieces, cols) + sums[pieces - 1];
            }
        }
        return comboSums;
    }

    static long combos(int pieces) {
        return combos(pieces, 7, 0);
    }

    static long combos(int pieces, int cols) {
        return combos(pieces, cols, 0);
    }

    static long combos(int pieces, int cols, int height) {
        if (pieces == 0) return 1;
        if (cols == 0 && pieces > 0) return 0;
        if (combosCache[pieces][cols][height] != 0) return combosCache[pieces][cols][height];
        long total = combos(pieces, cols - 1, 0);
        if (height < 6) total += combos(pieces - 1, cols, height + 1);
        combosCache[pieces][cols][height] = (int) total;
        return total;
    }

    static long factorial(int n) {
        return n < 2 ? 1 : n * factorial(n - 1);
    }

    static BigInteger fact(int n) {
        return n < 2 ? BigInteger.ONE : fact(n - 1).multiply(BigInteger.valueOf(n));
    }
}
