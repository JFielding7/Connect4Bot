package connect4bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    public static void m(String[] args) {
        String pos = """
                0110001
                1101111
                1011000
                1101000
                1010000
                0001000""";
        String pos2 = """
                1111111
                1110111
                0000111
                1011111
                0101111
                1110111""";
        System.out.println(checkWin(encode(pos), 0, 28));
        System.out.println(checkWin(encode(pos), 1, 17));
        long start = System.currentTimeMillis();
        long posNum = encode(pos);
        System.out.println(posNum);
        for (long i = 0; i < 10000000; i++) {
            // boolean win = checkWin(new Random().nextLong(Long.MAX_VALUE), 1, (int) i % 42);
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    static int x = 0;

    public static void main(String[] args) {
        String pos = "   0   \n" +
                     " 101 1 \n" +
                     " 110 1 \n" +
                     " 001 0 \n" +
                     "1100 1 \n" +
                     "0111000\n";
//        System.out.println(decode(1254287681036484608L));
//        System.out.println(decode(1398402870186082304L));
        int moves = 2 * pos.length() - pos.replace("1", "").length() - pos.replace("0", "").length();
        long grid = encode(pos);
        // System.out.println(evaluatePosition(encode(pos), 1 - (moves & 1), -1, 1, moves));
//        nextMoves(grid, 0).stream().sorted().forEach((state) -> {
//            System.out.println(state + ": " + evaluatePosition(state, 1, -1, 1, 12));
//        });
//        System.out.println("done");
//        nextMoves(1254287681036484608L, 1).forEach((state) -> {
//            System.out.println(state + ": " + evaluatePosition(state, 1, -1, 1, 13));
//        });
        long time = System.currentTimeMillis();
        System.out.println(evaluatePosition(encode(pos), 1 - (moves & 1), -1, 1, moves));
        System.out.println(System.currentTimeMillis() - time);
        System.out.println(x);
        System.exit(1);
//        String pos = "  10110\n" +
//                     "  00110\n" +
//                     "  11001\n" +
//                     " 101110\n" +
//                     "0010010\n" +
//                     "0101110\n";
//        String pos = "       \n" +
//                     "  11   \n" +
//                     "  10   \n" +
//                     "  01   \n" +
//                     "  10   \n" +
//                     "  01 0 \n";
        //System.out.println(decode(3005827301904965064L));
        moves = 2 * pos.length() - pos.replace("1", "").length() - pos.replace("0", "").length();
        // System.out.println(encode(pos));
        System.out.println(evaluatePosition(encode(pos), 1 - (moves & 1), -1, 1, moves));
//        System.out.println(evaluatePosition(1254287681036484608L, 1, -1, 1, 12));
        System.out.println(x);
        // 1256539480858558464
//        states.forEach((s, v) -> System.out.println(s + " " + v));
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
        for(int i = 0; i < 7; i++){
            if(!cols[i].isEmpty()) pos += (Long.parseLong(cols[i], 2) << 36 - i * 6);
            pos += ((long) cols[i].length() << 60 - 3 * i);
        }
        return pos;
    }

    static String decode(long pos){
        long[] heights = new long[7];
        for(int i = 0; i < 7; i++){
            heights[i] = (pos >> 42 + 3 * i) & 7;
        }
        String board = "";
        for(int i = 5; i > -1; i--) {
            String row = "\n";
            for (int j = 6; j > -1; j--) {
                row += heights[j] <= i ? " " : (pos >> 6 * j + i) & 1;
            }
            board += row;
        }
        return board;
    }

    public static boolean checkWin(long state, int piece, int lastMove) {
        int row = lastMove % 6, col = lastMove / 6;
        return connectionCount(state, piece, lastMove, row, col, 0, 1, 6) + connectionCount(state, piece, lastMove, row, col, 0, -1, -6) >= 3 ||
                connectionCount(state, piece, lastMove, row, col, -1, 0, -1) >= 3 ||
                connectionCount(state, piece, lastMove, row, col, 1, 1, 7) + connectionCount(state, piece, lastMove, row, col, -1, -1, -7) >= 3 ||
                connectionCount(state, piece, lastMove, row, col, 1, -1, -5) + connectionCount(state, piece, lastMove, row, col, -1, 1, 5) >= 3;
    }

    static int connectionCount(long state, int piece, int lastMove, int row, int col, int dr, int dc, int dp) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;
        int pos = lastMove + dp;
        while (c > -1 && c < 7  && r > -1 && r < ((state >> (42 + 3 * c)) & 7) && ((state >> pos) & 1) == piece) {
            count++;
            r += dr;
            c += dc;
            pos += dp;
        }
        return count;
    }

    static long nextState(long state, int piece, int col, int height){
        if(piece == 1) state += 1L << col * 6 + height;
        return state + (1L << 42 + col * 3);
    }

    static HashMap<Long, Integer> states = new HashMap<>();
    static int[] priority = {3, 2, 4, 1, 5, 0, 6};

    static int evaluatePosition(long state, int piece, int bestEval1, int bestEval2, int movesMade){
        x++;
        if(states.containsKey(state)) {
            return states.get(state);
        }
        if(movesMade == 42) return 0;
        ArrayList<Long> moves = new ArrayList<>();
        for(int i = 0; i < 7; i++){
            int c = priority[i];
            int height = (int) ((state >> 42 + c * 3) & 7);
            if(height != 6) {
                long move = nextState(state, piece, c, height);
                if(checkWin(move, piece, c * 6 + height)) {
                    int eval = piece == 1 ? 1 : -1;
                    states.put(state, eval);
                    return eval;
                }
                moves.add(move);
            }
        }
        if(piece == 1) {
            int maxEval = -1;
            for (long move : moves) {
                int eval = evaluatePosition(move, 0, bestEval1, bestEval2, movesMade + 1);
                maxEval = Math.max(maxEval, eval);
                bestEval1 = Math.max(bestEval1, eval);
                if(bestEval1 == 1) break;
            }
            states.put(state, maxEval);
            return maxEval;
        }
        else{
            int minEval = 1;;
            for (long move : moves) {
                int eval = evaluatePosition(move, 1, bestEval1, bestEval2, movesMade + 1);
                minEval = Math.min(minEval, eval);
                bestEval2 = Math.min(bestEval2, eval);
                if(bestEval2 == -1) break;
            }
            states.put(state, minEval);
            return minEval;
        }
    }

//    static ArrayList<Long> nextMoves(long state, int piece){
//        ArrayList<Long> moves = new ArrayList<>();
//        for(int c = 0; c < 7; c++){
//            if(((state >> 42 + c * 3) & 7) != 6) moves.add(nextState(state, piece, c));
//        }
//        return moves;
//    }

    static long flipTurn(long grid) {
        int[] heights = new int[7];
        for (int i = 0; i < 7; i++) {
            heights[i] = (int) (grid >> (42 + 3 * i) & 7);
        }
        long flippedGrid = 0L;
        for (int i = 0; i < 7; i++) {
            int height = heights[i];
            if (height == 0) continue;
            int col = (int) (grid >> (i * 6) & 63) ^ ((1 << (height)) - 1);
            flippedGrid += ((long) col) << i * 6;
        }
        flippedGrid = flippedGrid + (grid >> 42 << 42);
        return flippedGrid;
    }




}