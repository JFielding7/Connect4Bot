package connect4bot;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        System.out.println(decode(2814749767376896L));
        long start = System.currentTimeMillis();
        String board =  "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n";
        long state = encode(board);
        solvePositions(0, 1, 5, 0);
        ArrayList<Long> leaves = new ArrayList<>();
//        long[] moves = {3096224743837696L, 848822976663552L, 1153242562002161665L, 9007199256051712L, 162415459625336833L, 180179169483685952L, 109951162777920L, 2849934139457600L, 2603643534573760L, 1170975485551706113L, 18581746509418497L, 2323861805786464257L, 1188954699689099265L, 4573968371810432L, 146375785056305153L, 18304669579218945L, 1152965485071958019L, 146652860912766977L, 288516250248675329L, 288269959644053568L, 36345456384544768L, 79164837199938L, 2603643534577728L, 325455441821761L, 162415459608563713L, 2264993953218565L, 144467031796748416L, 432349964421562369L, 288516249174937601L, 1155182169233031169L, 290486576159391745L, 306249172724482049L, 1171221776156327937L, 38562071842918400L, 17592186044421L, 2357352929951936L, 2572857209262080L, 162169170077679617L, 1152934698746380291L, 36314670075740161L, 1170944699209351171L, 1155459177444016129L, 2308099344512843777L, 2572857209258048L, 1315055490312437761L, 144189954866544705L, 36345456367767616L, 18119951642525760L, 162446245950918656L, 6759797488091137L, 356241767399489L, 164385784462311425L, 20552071346655232L, 288239173318475777L, 2324139020138844160L, 148623185750261761L, 18858823439626240L, 22522396183625729L, 20305780758544385L, 2305851874026192897L, 18612532851773440L, 1153242562002157633L, 36037593128763393L, 48378511622210L, 848822976651265L, 2849934139461632L, 1297076276175044609L, 144405460219330561L, 180148383174885377L, 2572857209000000L, 1441156279878811649L, 432380750747140160L, 180148383158108161L, 1299292891616641025L, 2603643534839808L, 2603643534577792L, 148623185749999617L, 288547037647994944L, 144467031796744384L, 2305851805306716163L, 144220741192122560L, 2572857208999937L, 1153027126442590272L, 20305780758544448L, 1153242562002161728L, 38315781205000256L, 288547037647998976L, 1153211844396056577L, 144405459145592834L, 162199956403257408L, 1155212955558608897L, 1173192100993302529L, 602532372025408L, 36345456384540736L, 1173222887318880320L, 2308376421443047424L, 2542070883680257L, 180179169500463168L, 1153211775676579843L, 144682536075792385L, 18027592649015301L, 18335455904796673L, 4789472651116545L, 2305913515396825152L, 144189955940286528L, 1297045489849466881L, 1153211775676583938L, 144467032870486080L, 20582857688748032L, 306249173781446657L, 144189955940286465L, 36099165779918912L, 1153242630721634305L, 162169169003937857L, 162138382695137281L, 54078379934089280L, 18089165300170817L, 18366242247151616L, 356241767403521L, 3458769049306005505L, 162138382678360067L, 288793328252620800L, 18119951625748800L, 1157429502280990721L, 48378511622149L, 109951162777792L, 18089165316947969L, 18119951625748672L, 306279959050059840L, 109951162777729L, 162169170077679680L, 1152965485071958082L, 1153488852606783489L, 1297045557495201793L, 146652860913025025L, 18304669579218946L, 18366242247147584L, 2306159806001451008L, 325455441821699L, 571746046447617L, 2295780278796291L, 54047593608511489L, 36099165796696128L, 162199955346292800L, 1155212886839132225L, 18089165300170881L, 325455441825794L, 1155489963769335872L, 633318697603200L, 325455441821762L, 144713323475107904L, 387028092977472L, 20829148276600832L, 20552071346651137L, 144959613006000128L, 20305780742029313L, 18027592649015299L, 290486575085649921L, 18612532834996288L, 146683647238344768L, 1297076276175044672L, 2323892729534218304L, 1171006271877283904L, 144436245471170561L, 325455441825793L, 20274994416451586L, 2305851942745669633L, 2572857208995905L, 2295780278796353L, 144128382215389187L, 288300745969631296L, 6790583813668928L, 4820258976436224L, 146437357707460672L, 144220742265864256L, 36314670042189825L, 146406571381882944L, 1171221776139554817L, 144159168540966978L, 79164837199875L, 148653972075839552L, 6790583813931008L, 38315781221515328L, 36068379454341184L, 1152996340117012481L, 2264993953218563L, 2306406096606072832L, 54324670538715136L, 180148384215072769L, 2449962732775014401L, 7036874418552832L, 290517362484969536L, 146683647238606848L, 2449962596409802753L, 20552071346393089L, 79164837200001L, 1155459177443758081L, 1297076343820779521L, 144436245471166529L, 2450239809705218048L, 356241767403584L, 20274994432966657L, 2603643534835776L, 7036874418294784L, 36591747005943808L, 1152934767465857025L, 18581746509422593L, 3096224743829504L, 38315781238292544L, 4512395720654849L, 1153488852606787585L, 18058378974593090L, 38284994879422465L, 879609302241280L, 1171252562465132608L, 18612532851769408L, 144682536075796481L, 1170944767928827905L, 1170975554254405633L, 1155182100513816578L, 36068379454341121L, 109951162777665L, 20582857672232960L, 1125899906863104L, 848822976655360L, 18366242230374464L, 387028092977344L, 146652860913029120L, 4512395720654850L, 146683647238602816L, 2542070883422210L, 1152965553791434753L, 294669116243971L, 2295780278796354L, 3096224744087552L, 2326566604374208L, 1152996271397535809L, 2323861943208640513L, 288269958570311745L, 2819147813625857L, 48378511622147L, 162446245934141504L, 2308130130838421568L, 1170944699226128385L, 140737488355648L, 290486574012170241L, 387028092981376L, 2305882729071247361L, 146406571381882881L, 18335455904796736L, 36068379437563969L, 18858823439634432L, 288269960717795329L, 18335455904792641L, 144159169614708737L, 4573968371548352L};
//        System.out.println(moves.length);
        //        int movesMade = 3;
//        while (++movesMade < 39) {
//            long[] bestMoves = new long[moves.length];
//            for (int i = 0; i < moves.length; i++) {
//                bestMoves[i] = bestMoves(moves[i], (movesMade & 1) ^ 1, movesMade).get(0);
//            }
//            System.out.println(Arrays.toString(bestMoves));
//            moves = bestMoves;
//        }
//        System.out.println();
        int depth = 3;
        for (long position : states) {
            if (Engine.depth(position) == depth && Math.abs(Solver.evaluatePosition(position, (depth & 1) ^ 1, WORST_EVAL, BEST_EVAL, depth)) < 3) leaves.add(position);
        }
        System.out.println(leaves);
        System.out.println(leaves.size());
//        Collections.shuffle(leaves);
//        while (leaves.size() > 256) leaves.remove(leaves.size() - 1);
//        System.out.println(leaves.size());
//        System.out.println(leaves);
//        for (long leaf : leaves) {
//            System.out.println(decode(leaf));
//        }
//        int moves = board.length() - board.replaceAll("[01]", "").length();
//        int piece = (moves & 1) ^ 1;
//        System.out.println("Perfect Games: " + countPerfectGames(state, piece, moves));
//        System.out.println("Perfect Positions: " + perfectGameCounts.size());
//        System.out.println("Positions Evaluated: " + Solver.positions);
//        System.out.println("Time: " + (System.currentTimeMillis() - start));
//        writeCacheToFile(perfectGameCounts, "perfectPositions.bin");
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

    static void writeCacheToFile(HashMap<Long, Long> cache, String filename) {
        byte[] bytes = new byte[cache.size() << 4];
        int i = 0;
        for (long position : cache.keySet()) {
            for (int j = 0; j < 64; j+=8) {
                bytes[i++] = (byte) (position >>> j & 255);
            }
            long count = cache.get(position);
            for (int j = 0; j < 64; j+=8) {
                bytes[i++] = (byte) (count >>> j & 255);
            }
        }
        try (FileOutputStream out = new FileOutputStream(filename)) {
            out.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static HashMap<Long, Long> loadCacheFromFile(String filename) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filename));
        HashMap<Long, Long> cache = new HashMap<>();
        for (int i = 0; i < bytes.length; i+=16) {
            long position = 0;
            for (int j = i; j < i + 8; j++) {
                position += (long) (bytes[j] & 255) << (j - i << 3);
            }
            long count = 0;
            for (int j = i + 8; j < i + 16; j++) {
                count += (long) (bytes[j] & 255) << (j - i << 3);
            }
            cache.put(position, count);
        }
        return cache;
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
