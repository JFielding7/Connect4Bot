package connect4bot;

import java.io.*;
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

    static int[] colPriority = {5, 3, 1, 0, 2, 4, 6};

    static int x = 0;
    static int[] depths = new int[43];

    static void loadCache(HashSet<Long> cache, String file) {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line = in.readLine();
            while (line != null) {
                cache.add(Long.parseLong(line));
                line = in.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
//        System.out.println(eval(0, new long[8], 1, -1, 1, 0));
//        System.out.println(x);
//        System.out.println(decode(7905688004349874817L));
//        System.out.println(configIndex(7905688004349874817L));
//        System.exit(0);
        String p3 =  "   0   \n" +
                     "  01   \n" +
                     "  100  \n" +
                     "  011  \n" +
                     " 01010 \n" +
                     "110110 \n";

        String p1 =  "       \n" +
                     "       \n" +
                     "       \n" +
                     "       \n" +
                     "       \n" +
                     "       \n";
        loadCache(wins, "wins.txt");
        loadCache(losses, "losses.txt");
        loadCache(alphaVals, "alpha.txt");
        loadCache(betaVals, "beta.txt");
        loadCache(wins, "wins2.txt");
        loadCache(losses, "losses2.txt");
        loadCache(alphaVals, "alpha2.txt");
        loadCache(betaVals, "beta2.txt");
        System.out.println(alphaVals.size());
        long pos = encode(p1);
        System.out.println(pos);
        System.out.println(wins.contains(pos));
//        System.out.println(Arrays.toString(heuristic(3, 0, 0, 0, 0, 0, 0, 0)));
//        System.out.println(decode(1254287681036484608L));
//        System.out.println(decode(1398402870186082304L));
        Arrays.fill(cache, -1);
        Arrays.fill(upper, -1);
        Arrays.fill(lower, -1);
        int moves = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
//        loadAlphaValues();
        long time = System.currentTimeMillis();
        System.out.println(evaluatePosition(encode(p1), (moves & 1) ^ 1, -1, 1, moves));
//        try {
//            winsPW = new PrintWriter(" ");
//            lossesPW = new PrintWriter(" ");
//            alphaPW = new PrintWriter(" ");
//            betaPW = new PrintWriter(" ");
//            System.out.println(evaluatePosition(encode(p1), 0, -1, 1, moves));
//            winsPW.close();
//            lossesPW.close();
//            alphaPW.close();
//            betaPW.close();
//        }
//        catch (IOException ignored) {}
        System.out.println(System.currentTimeMillis() - time);
//        saveAlphaValues(encode(p1), 1 - (moves & 1), moves);
        System.out.println(x);
        System.out.println(Arrays.toString(depths));
        System.out.println(Arrays.toString(frequency));
//        int count = 0;
//        int total = 0;
//        int[] sizes = new int[7];
//        for (HashSet<Integer> arr : bestMoves) {
//            if (!arr.isEmpty()) {
//                count++;
//                total += arr.size();
//                sizes[arr.size()]++;
//            }
//        }
//        System.out.println(total + " " + count);
//        System.out.println(Arrays.toString(sizes));
    }
    //[0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 14, 12, 31, 103, 221, 609, 1132, 2817, 5540, 11675, 23435, 42597, 79814, 131994, 224193, 342296, 517426, 737723, 1023747, 1392726, 1775082, 2311850, 2631548, 3280662, 3365916, 4151838, 3792212, 4506456, 3420493, 3707670, 2216615, 2020585, 1331068]
    //[0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 14, 12, 39, 141, 345, 956, 1721, 4035, 7471, 15056, 28431, 50391, 91604, 147889, 249264, 373266, 566003, 793653, 1103867, 1480473, 1881857, 2423621, 2749868, 3406374, 3488409, 4280799, 3908931, 4622723, 3514615, 3790147, 2277260, 2089721, 1389495]
    //[8, 11, 7, 6, 5, 0, 0]
    static HashMap<Long, Integer> alphaCache = new HashMap<>();

    static void saveAlphaValues(long state, int piece, int depth) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter("alphaValues.txt")) {
            evaluatePosition(state, piece, -1, 1, depth);
            for (long pos : alphaCache.keySet()) {
                out.println(pos + " " + alphaCache.get(pos));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void loadAlphaValues() {
        try (BufferedReader in = new BufferedReader(new FileReader("alphaValues.txt"))) {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String[] data = line.split(" ");
                alphaCache.put(Long.parseLong(data[0]), Integer.parseInt(data[1]));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    static HashMap<Long, Integer> states = new HashMap<>();
    static int[] moveOrder = {3, 2, 4, 5, 1, 6, 0};

    static int SIZE = 0;

    static long[] cache = new long[SIZE];
    static int[] evals = new int[SIZE];
    static long[] upper = new long[SIZE];
    static long[] lower = new long[SIZE];
    static long[][][] heuristic = new long[2][7][6];
    static HashSet<Long> wins = new HashSet<>(), losses = new HashSet<>(), alphaVals = new HashSet<>(), betaVals = new HashSet<>();
    static PrintWriter winsPW, lossesPW, alphaPW, betaPW;

    static int evaluatePosition(long state, int piece, int alpha, int beta, int movesMade){
        x++;
        if (movesMade == 42) return 0;
        int index = (int) (state % SIZE);
        if (cache[index] == state) return evals[index];
        if (lower[index] == state) alpha = 0;
        if (upper[index] == state) beta = 0;
        if (wins.contains(state)) return 1;
        if (losses.contains(state)) return -1;
        if (alphaVals.contains(state)) alpha = 0;
        if (betaVals.contains(state)) beta = 0;
        ArrayList<Integer> order = new ArrayList<>();
        int[] scores = new int[7];
        int i = -1;
        for (int col : moveOrder) {
            int height = (int) ((state >> 42 + col * 3) & 0b111);
            if (height != 6) {
                i++;
                long move = nextState(state, piece, col, height);
                if (isWin(move, piece)) return 1;
                int moveIndex = (int) (move % SIZE);
                if (cache[moveIndex] == move) alpha = Math.max(alpha, -evals[moveIndex]);
                if (upper[moveIndex] == move) alpha = Math.max(alpha, 0);
                if (alpha >= beta) {
//                    if (movesMade < 16) {
//                        if (alpha == 1) winsPW.println(state);
//                        else alphaPW.println(state);
//                    }
                    return alpha;
                }
                order.add(col);
                scores[col] = countThreats(move, piece);
//                scores[col] = heuristic[piece][col][height];
            }
        }
        i = -1;
        order.sort((a, b) -> scores[b] - scores[a]);
        ArrayList<int[]> failedMoves = new ArrayList<>();
        for (int col : order) {
            int height = (int) (state >>> 42 + col * 3 & 0b111);
            if (height != 6) {
                i++;
                long move = nextState(state, piece, col, height);
                int eval = -evaluatePosition(move, piece ^ 1, -beta, -alpha, movesMade + 1);
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    heuristic[piece][col][height] += failedMoves.size();
                    for (int[] failedMove : failedMoves) heuristic[piece][failedMove[0]][failedMove[1]]--;
                    if (movesMade < 16) frequency[i]++;
                    if (alpha == 1) {
//                        if (movesMade < 16) winsPW.println(state);
                        cache[index] = state;
                        evals[index] = 1;
                    }
                    else {
//                        if (movesMade < 16) alphaPW.println(state);
                        lower[index] = state;
                    }
                    return alpha;
                }
                failedMoves.add(new int[]{col, height});
            }
        }
        if (alpha == -1) {
//            if (movesMade < 16) lossesPW.println(state);
            cache[index] = state;
            evals[index] = -1;
        }
        else {
//            if (movesMade < 16) betaPW.println(state);
            upper[index] = state;
        }
        return alpha;
    }

    static ArrayList<HashSet<Integer>> bestMoves = new ArrayList<>();

    static int configIndex(long state) {
        int index = 0;
        for (int i = 0; i < 7; i++) {
            index += (int) (Math.round(Math.pow(7, i)) * (state >>> 42 + i * 3 & 0b111));
        }
        return index;
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