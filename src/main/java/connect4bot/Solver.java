package connect4bot;

import java.util.Arrays;

import static connect4bot.Main.*;

public class Solver {
    public static void main(String[] args) {
        String p1 =     "       \n" +
                        "   0   \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "   1   \n" +
                        "  10   \n";
        String p2 =     "0      \n" +
                        "1      \n" +
                        "0      \n" +
                        "1      \n" +
                        "0      \n" +
                        "1101010\n";
        long state = encode(p1);
        int movesMade = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
        long start = System.currentTimeMillis();
        int eval = evaluatePosition(state, (movesMade & 1) ^ 1, WORST_EVAL, BEST_EVAL, movesMade);
        System.out.println("Time: " + (System.currentTimeMillis() - start));
        System.out.println("Positions evaluated: " + positionsEvaluated);
        System.out.println("Eval: " + eval);
       if (eval == 0) System.out.println("Draw");
       else if (eval > 0) {
           int movesToWin = 22 - eval - (movesMade >>> 1);
           System.out.println(((movesMade & 1) == 0 ? "Red" : "Yellow") + " wins in " + movesToWin + " moves");
       }
       else {
           int movesToWin = 22 + eval - (movesMade + 1 >>> 1);
           System.out.println(((movesMade & 1) == 1 ? "Red" : "Yellow") + " wins in " + movesToWin + " moves");
       }
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
        if (lowerBoundCache[index] == state) alpha = Math.max(alpha, lowerBoundValue[index]);
        if (upperBoundCache[index] == state) beta = Math.min(beta, upperBoundValue[index]);
        if (alpha >= beta) return alpha;
        int[] threats = new int[7];
        int[] order = Arrays.copyOf(moveOrder, 7);
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
                    lowerBoundCache[index] = state;
                    lowerBoundValue[index] = alpha;
                    return alpha;
                }
            }
        }
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
}
