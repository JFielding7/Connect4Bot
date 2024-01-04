package connect4bot;

import java.util.*;

public class Main {

    static int[][] CONNECTION_SHIFTS = {
            {63, 111, 153}, {71, 111, 113, 161}, {79, 111, 113, 115, 169}, {87, 111, 113, 115, 183}, {95, 113, 115, 191}, {103, 115, 199},
            {63, 65, 117, 155}, {71, 73, 117, 119, 153, 163}, {79, 81, 117, 119, 121, 161, 171, 183}, {87, 89, 117, 119, 121, 169, 181, 191}, {95, 97, 119, 121, 189, 199}, {103, 105, 121, 197},
            {63, 65, 67, 123, 157}, {71, 73, 75, 123, 125, 155, 165, 183}, {79, 81, 83, 123, 125, 127, 153, 163, 173, 181, 191}, {87, 89, 91, 123, 125, 127, 161, 171, 179, 189, 199}, {95, 97, 99, 125, 127, 169, 187, 197}, {103, 105, 107, 127, 195},
            {63, 65, 67, 69, 129, 159, 183}, {71, 73, 75, 77, 129, 131, 157, 167, 181, 191}, {79, 81, 83, 85, 129, 131, 133, 155, 165, 175, 179, 189, 199}, {87, 89, 91, 93, 129, 131, 133, 153, 163, 173, 177, 187, 197}, {95, 97, 99, 101, 131, 133, 161, 171, 185, 195}, {103, 105, 107, 109, 133, 169, 193},
            {65, 67, 69, 135, 181}, {73, 75, 77, 135, 137, 159, 179, 189}, {81, 83, 85, 135, 137, 139, 157, 167, 177, 187, 197}, {89, 91, 93, 135, 137, 139, 155, 165, 175, 185, 195}, {97, 99, 101, 137, 139, 163, 173, 193}, {105, 107, 109, 139, 171},
            {67, 69, 141, 179}, {75, 77, 141, 143, 177, 187}, {83, 85, 141, 143, 145, 159, 185, 195}, {91, 93, 141, 143, 145, 157, 167, 193}, {99, 101, 143, 145, 165, 175}, {107, 109, 145, 173},
            {69, 147, 177}, {77, 147, 149, 185}, {85, 147, 149, 151, 193}, {93, 147, 149, 151, 159}, {101, 149, 151, 167}, {109, 151, 175}};

    static int[][][] SHIFTS = {
           {{0}, {8}, {16}, {24}, {32}, {40},
           {0, 2}, {8, 10}, {16, 18}, {24, 26}, {32, 34}, {40, 42},
           {0, 2, 4}, {8, 10, 12}, {16, 18, 20}, {24, 26, 28}, {32, 34, 36}, {40, 42, 44},
           {0, 2, 4, 6}, {8, 10, 12, 14}, {16, 18, 20, 22}, {24, 26, 28, 30}, {32, 34, 36, 38}, {40, 42, 44, 46},
           {2, 4, 6}, {10, 12, 14}, {18, 20, 22}, {26, 28, 30}, {34, 36, 38}, {42, 44, 46},
           {4, 6}, {12, 14}, {20, 22}, {28, 30}, {36, 38}, {44, 46},
           {6}, {14}, {22}, {30}, {38}, {46}},

           {{0}, {0, 2}, {0, 2, 4}, {0, 2, 4}, {2, 4}, {4},
           {6}, {6, 8}, {6, 8, 10}, {6, 8, 10}, {8, 10}, {10},
           {12}, {12, 14}, {12, 14, 16}, {12, 14, 16}, {14, 16}, {16},
           {18}, {18, 20}, {18, 20, 22}, {18, 20, 22}, {20, 22}, {22},
           {24}, {24, 26}, {24, 26, 28}, {24, 26, 28}, {26, 28}, {28},
           {30}, {30, 32}, {30, 32, 34}, {30, 32, 34}, {32, 34}, {34},
           {36}, {36, 38}, {36, 38, 40}, {36, 38, 40}, {38, 40}, {40}},

           {{0}, {8}, {16}, {}, {}, {},
           {2}, {0, 10}, {8, 18}, {16}, {}, {},
           {4}, {2, 12}, {0, 10, 20}, {8, 18}, {16}, {},
           {6}, {4, 14}, {2, 12, 22}, {0, 10, 20}, {8, 18}, {16},
           {}, {6}, {4, 14}, {2, 12, 22}, {10, 20}, {18},
           {}, {}, {6}, {4, 14}, {12, 22}, {20},
           {}, {}, {}, {6}, {14}, {22}},

           {{}, {}, {}, {6}, {14}, {22},
           {}, {}, {6}, {4, 14}, {12, 22}, {20},
           {}, {6}, {4, 14}, {2, 12, 22}, {10, 20}, {18},
           {6}, {4, 14}, {2, 12, 22}, {0, 10, 20}, {8, 18}, {16},
           {4}, {2, 12}, {0, 10, 20}, {8, 18}, {16}, {},
           {2}, {0, 10}, {8, 18}, {16}, {}, {},
           {0}, {8}, {16}, {}, {}, {}}};

    static void combine() {
        int[][] shifts = new int[42][];
        int[] offsets = {63, 111, 153, 177};
        for(int i = 0; i < 42; i++) {
            HashSet<Integer> spots = new HashSet<>();
            for(int j = 0; j < 4; j++) {
                for (int spot : SHIFTS[j][i]) {
                    spots.add(spot + offsets[j]);
                }
            }
            shifts[i] = new int[spots.size()];
            int j = 0;
            for(Integer spot : spots) {
                shifts[i][j++] = spot;
            }
            Arrays.sort(shifts[i]);
        }
        System.out.println(shifts.length);
        for(int[] a : shifts) {
            System.out.print(Arrays.toString(a).replace("[", "{").replace("]", "}") + ", ");
        }
//        System.out.println();
    }

    static Random rand = new Random(42);

    static HashSet<Long> wins = new HashSet<>();
    static int[] colPriority = {5, 3, 1, 0, 2, 4, 6};

    static int eval(long state, long[] threats, int piece, int alpha, int beta, int movesMade) {
        x++;
        if(x % 10000000 == 0) {
            System.out.println(x);
            System.out.println(Arrays.toString(frequency));
            System.out.println(states.size());
        }
        if (movesMade == 42) return 0;
        if(states.containsKey(state)) return states.get(state);
        ArrayList<long[]> moveOrder = new ArrayList<>();
        long[][] nextThreats = new long[7][8];
        for(int col = 0; col < 7; col++) {
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            if (height == 6) continue;
            long next = state + (1L << 42 + col * 3);
            int playerOffset = 4, opponentOffset = 0;
            if(piece == 1) {
                next += 1L << col * 6 + height;
                playerOffset = 0;
                opponentOffset = 4;
            }
            long[] moveThreats = Arrays.copyOf(threats, 8);
            long heuristic = 0;
            for (int i = 0; i < 4; i++) {
                for (int shift : SHIFTS[i][col * 6 + height]) {
                    long oppConnect = threats[i + opponentOffset] >>> shift & 0b11;
                    if (oppConnect != 0) {
                        moveThreats[i + opponentOffset] -= oppConnect << shift;
                        if (oppConnect == 3) heuristic++;
                    }
                    else {
                        int connections = (int) (moveThreats[i + playerOffset] >>> shift & 0b11);
                        if (connections == 3) {
                            return 1;
                        }
                        if (connections == 2) heuristic++;
                        moveThreats[i + playerOffset] += 1L << shift;
                    }
                }
            }
            nextThreats[col] = moveThreats;
            moveOrder.add(new long[]{heuristic, next, col});
        }
        moveOrder.sort((a, b) -> 1 == 1 ? colPriority[(int) a[2]] - colPriority[(int) b[2]] : (int) b[0] - (int) a[0]);
        int maxEval = -1;
        int idx = 0;
        for(long[] move : moveOrder) {
            int eval = -eval(move[1], nextThreats[(int) move[2]], piece ^ 1, -beta, -alpha, movesMade + 1);
            alpha = Math.max(alpha, eval);
            maxEval = Math.max(maxEval, eval);
            if (alpha >= beta) {
                frequency[idx]++;
                if(alpha == 1) states.put(state, alpha);
                return alpha;
            }
            idx++;
        }
        states.put(state, maxEval);
        return maxEval;
    }

    static int x = 0;

    public static void main(String[] args) {
//        System.out.println(eval(0, new long[8], 1, -1, 1, 0));
//        System.out.println(x);
//        System.exit(0);
        String p3 =  "   0   \n" +
                     "  01   \n" +
                     "  100  \n" +
                     "  011  \n" +
                     " 01010 \n" +
                     "110110 \n";

        String p1 =  "   0   \n" +
                     "   1   \n" +
                     "   0   \n" +
                     "   1   \n" +
                     "   0   \n" +
                     "   1   \n";
//        System.exit(0);
//        System.out.println(Arrays.toString(heuristic(3, 0, 0, 0, 0, 0, 0, 0)));
//        System.out.println(decode(1254287681036484608L));
//        System.out.println(decode(1398402870186082304L));
        int moves = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
        long time = System.currentTimeMillis();
//        generatePositions(0, 1, 7);
        System.out.println(evaluatePosition(encode(p1), 1 - (moves & 1), -1, 1, moves));
        System.out.println(System.currentTimeMillis() - time);
        System.out.println(x);
        System.out.println(Arrays.toString(frequency));
    }

    static int[] frequency = new int[7];

    static HashSet<Long> seen = new HashSet<>();

    static void generatePositions(long position, int piece, int depth) {
        x++;
        if (depth == -1 || seen.contains(position)) return;
        seen.add(position);
        for(int i = 0; i < 7; i++) {
            int height = (int) (position >>> 42 + i * 3 & 0b111);
            if(height < 6) {
                long next = nextState(position, piece, i, height);
                generatePositions(next, piece ^ 1, depth - 1);
            }
        }
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

    static boolean isWin(long state, int piece) {
        long board = 0;
        if (piece == 1) {
            for (int i = 0; i < 7; i++) board += (state >>> (6 * i) & 0b111111) << (7 * i);
        }
        else {
            for (int i = 0; i < 7; i++) board += ((state >>> (6 * i) & 0b111111) ^ ((1 << (state >> (42 + i * 3) & 0b111)) - 1)) << (7 * i);
        }
        // check horizontal win
        long horizontal = board & (board >>> 7);
        if ((horizontal & (horizontal >>> 14)) != 0) return true;
        // check vertical win
        long vertical = board & (board >>> 1);
        if ((vertical & (vertical >>> 2)) != 0) return true;
        // check positive-sloped diagonal
        long posDiag = board & (board >>> 6);
        if ((posDiag & (posDiag >>> 12)) != 0) return true;
        // check negative-sloped diagonal
        long negDiag = board & (board >>> 8);
        return (negDiag & (negDiag >>> 16)) != 0;
    }

    static HashMap<Long, Integer> states = new HashMap<>();
    static byte[] moveOrder = {3, 2, 4, 5, 1, 6, 0};

    static int SIZE = 100_000_007;

    static long[] cache = new long[SIZE];
    static int[] evals = new int[SIZE];
    static long[] upper = new long[SIZE];
    static long[] lower = new long[SIZE];

    static int evaluatePosition(long state, int piece, int alpha, int beta, int movesMade){
        x++;
        if (movesMade == 42) return 0;
        int index = (int) (state % SIZE);
        if (cache[index] == state) return evals[index];
        if (lower[index] == state) alpha = 0;
        if (upper[index] == state) beta = 0;
        for (int col : moveOrder) {
            int height = (int) ((state >> 42 + col * 3) & 0b111);
            if (height != 6) {
                long move = nextState(state, piece, col, height);
                // if(piece == 0 && checkWin(move, piece, col * 6 + height)) return 1;
                if (isWin(move, piece)) return 1;
//                if(checkWin(move, piece, col * 6 + height)) return 1;
                int moveIndex = (int) (move % SIZE);
                if (cache[moveIndex] == move) alpha = Math.max(alpha, -evals[moveIndex]);
                if (upper[moveIndex] == move) alpha = Math.max(alpha, 0);
                if (alpha >= beta) return alpha;
            }
        }
        int i = -1;
        for (int col : moveOrder) {
            int height = (int) ((state >> 42 + col * 3) & 0b111);
            if (height != 6) {
                i++;
                long move = nextState(state, piece, col, height);
                int eval = -evaluatePosition(move, piece ^ 1, -beta, -alpha, movesMade + 1);
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    if(movesMade < 15) frequency[i]++;
                    if (alpha == 1) {
                        cache[index] = state;
                        evals[index] = 1;
                    }
                    else lower[index] = state;
                    return alpha;
                }
            }
        }
        if (alpha == -1) {
            cache[index] = state;
            evals[index] = -1;
        }
        else upper[index] = state;
        return alpha;
    }

//    static void updateCache(long state, boolean isUpper, int eval) {
//        int index = (int) (state % SIZE);
//        if (eval != 0) {
//            cache[index] = state;
//            evals[index] = eval;
//        }
//        else {
//            boundCache[index] = state;
//            upper[index] = isUpper;
//            bound[index] = eval;
//        }
//    }

    static int[][] shiftGeneration() {
        int[][] shifts = new int[42][];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                ArrayList<Integer> s = new ArrayList<>();
                for(int r1 = r - 3, c1 = c + 3; r1 <= r && c1 >= c; r1++, c1--) {
                    if(r1 > -1 && r1 < 3 && c1 > 2 && c1 < 7) {
                        s.add(2 * (4 * r1 + (6 - c1)));
                    }
                }
                int[] shift = new int[s.size()];
                int i = 0;
                for(Integer n : s) {
                    shift[i++] = n;
                }
                shifts[6 * c + r] = shift;
            }
        }
        return shifts;
    }

//    static long[] heuristic(int col, long position, long[] connections) {
//        long value = 0;
//        int row = (int) (position >> 42 + col * 3 & 0b111);
//        long[] nextConnections = Arrays.copyOf(connections, 4);
//        for(int i = 0; i < 4; i++) {
//            for(int shift : SHIFTS[i][col * 6 + row]) {
//                nextConnections[i]
//            }
//        }
//        return new long[]{value, position, h1, v1, h2, v2};
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