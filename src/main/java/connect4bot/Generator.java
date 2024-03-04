package connect4bot;

import java.math.BigInteger;
import java.util.*;

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
//        long state = 0b110_100_010_000_000_000_000_111110_000000_000001_000000_000000_000000_000000L;
        long state = 0;
        int depth = 11;
        byte[] evals = new byte[posCount[depth]];
        long start = System.currentTimeMillis();
        generatePositions(state, 1, depth);
        for (long pos : states) {
            evals[getIndex(pos, Engine.depth(pos))] = 1;
        }
        int total = 0;
        for(byte b : evals) {
            total += b;
        }
        System.out.println(states.size());
        System.out.println(total);
        System.out.println(System.currentTimeMillis() - start);
    }

    static void updateDatabase() {

    }

    static void updateDatabase(byte[] database, String file) {

    }

    static HashSet<Long> states = new HashSet<>();

    static void generatePositions(long state, int piece, int depth) {
        if (depth == -1 || states.contains(state)) return;
        states.add(state);
        if(Engine.isWin(state, piece ^ 1)) return;
        for (int i = 0; i < 7; i++) {
            int height = (int) (state >>> 42 + i * 3 & 0b111);
            if (height != 6) {
                long move = Engine.nextState(state, piece, i, height);
                generatePositions(move, piece ^ 1, depth - 1);
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
