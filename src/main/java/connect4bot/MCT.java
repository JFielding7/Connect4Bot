package connect4bot;

import java.util.*;

import static connect4bot.Main.decode;
import static connect4bot.Main.encode;

public class MCT {
    private static final double EXPLORATION_CONSTANT = 2;
    private static final int[] moveOrder = {3, 2, 4, 5, 1, 6, 0};
    static int simulationsRan;
    static int nodesCreated;
    static final int MAX_DEPTH = 15;
    static String interestingLine = "   1 1 \n" +
            "   1 0 \n" +
            "   0 1 \n" +
            "  01 0 \n" +
            " 110 1 \n" +
            "001100 \n";
    static String failedPos =   "   0   \n" +
                                "0  1   \n" +
                                "0 10 1 \n" +
                                "1 01 0 \n" +
                                "0110 1 \n" +
                                "010110 \n";
    public static void main(String[] args) {
        String board2 =  "000 000\n" +
                        "110 101\n" +
                        "0110010\n" +
                        "1001101\n" +
                        "1010011\n" +
                        "1101101\n";
        String board  = "       \n" +
                        "       \n" +
                        "   0   \n" +
                        "   1   \n" +
                        "1  0   \n" +
                        "10 10  \n";
//        System.out.println(decode(193654784032768000L));
//        System.out.println(decode(14108933213138944L));
        long pos = encode(board);
        System.out.println(pos);
        int depth = 2 * board.length() - board.replace("1", "").length() - board.replace("0", "").length();
        long start = System.currentTimeMillis();
        MCT node = new MCT(pos, depth);
        for (int i = 0; i < 5000000; i++) selectionSimulation(node);
        System.out.println(node.wins / node.simulations);
        System.out.println(Arrays.toString(MCT.moveOrder(pos, 6)));
        System.out.println(System.currentTimeMillis() - start);
    }

    long state;
    double wins;
    int simulations, piece, depth;
    ArrayList<MCT> children;

    MCT(long state, int depth) {
        nodesCreated++;
        this.state = state;
        this.depth = depth;
        this.piece = (depth & 1) ^ 1;
    }

    static HashMap<Long, MCT> cache = new HashMap<>();
    static int SIMULATIONS = 2000;

    static int heuristicEvaluation(long state, int depth) {
        MCT node = new MCT(state, depth);
        for (int i = 0; i < 1000; i++) selectionSimulation(node);
        if ((depth & 1) == 0) return node.wins / node.simulations > .5 ? 1 : -1;
        return node.wins / node.simulations > .5 ? -1 : 1;
    }

    static long[] moveOrder(long state, int depth) {
        if (state == 175640385489731584L) return new long[]{175675569861820480L};
        if (state == 13827458236420096L) return new long[]{157942647386017792L};
        if (depth > 0) {
            ArrayList<Long> a = nextStates(state, (depth & 1) ^ 1);
            long[] order = new long[a.size()];
            int i = 0;
            for(long pos : a) order[i++] = pos;
            return order;
        }
//        System.out.println(decode(state));
//        System.out.println(depth);
        MCT root = cache.containsKey(state) ? cache.get(state) : new MCT(state, depth);
        for (int i = root.simulations; i < 3000; i++) selectionSimulation(root);
        if ((depth & 1) == 0) root.children.sort(Comparator.comparing(a -> -Math.floor(a.wins / a.simulations / .55)));
        else root.children.sort(Comparator.comparing(a -> Math.floor(a.wins / a.simulations / .55)));
        long[] order = new long[root.children.size()];
        int i = 0;
        for (MCT child : root.children) {
            order[i++] = child.state;
        }
//        System.out.println(Main.decode(order[0]));
//        System.out.println(Arrays.toString(order));
//        System.out.println(root.children.get(0).simulations);
        return order;
    }

    static double selectionSimulation(MCT node) {
        cache.put(node.state, node);
        simulationsRan++;
        node.simulations++;
        int oppPiece = node.piece ^ 1;
        if (Main.isWin(node.state, oppPiece)) {
            node.wins += oppPiece;
            return oppPiece;
        }
        if (node.depth == 42) {
            node.wins += 0.5;
            return 0.5;
        }
        if (node.children == null) {
            node.children = getChildren(node);
            double result = randomSimulation(node.children.get(0));
            node.wins += result;
            return result;
        }
        double result = selectionSimulation(selectChild(node));
        node.wins += result;
        return result;
    }

    static MCT selectChild(MCT node) {
        MCT selected = null;
        double maxVal = -1;
        for (MCT child : node.children) {
            if (child.simulations == 0) return child;
            double val = (node.piece == 1 ? child.wins : child.simulations - child.wins) / child.simulations + Math.sqrt(EXPLORATION_CONSTANT * Math.log(node.simulations) / child.simulations);
            if (val > maxVal) {
                maxVal = val;
                selected = child;
            }
        }
        return selected;
    }

    static double randomSimulation(MCT node) {
        simulationsRan++;
        node.simulations++;
        long state = node.state;
        int piece = node.piece ^ 1, depth = node.depth;
        Random rand = new Random();
        while (depth++ < 42) {
            if (Main.isWin(state, piece)) {
                node.wins += piece;
                return piece;
            }
            piece ^= 1;
            ArrayList<Long> moves = nextStates(state, piece);
            state = moves.get(rand.nextInt(moves.size()));
        }
        if (Main.isWin(state, 0)) return 0;
        node.wins += 0.5;
        return 0.5;
    }

    static ArrayList<MCT> getChildren(MCT node) {
        ArrayList<MCT> children = new ArrayList<>();
        for (int i : moveOrder) {
            int height = (int) (node.state >>> 42 + i * 3 & 0b111);
            if (height < 6) {
                long move = nextState(node.state, node.piece, i, height);
                MCT child = cache.get(move);
                if (child == null) {
                    child = new MCT(move, node.depth + 1);
                    cache.put(move, child);
                }
                children.add(child);
            }
        }
//        for (long state : nextStates(node.state, node.piece)) children.add(new MCT(state, node.depth + 1));
        return children;
    }

    static long nextState(long state, int piece, int col, int height) {
        if(piece == 1) state += 1L << col * 6 + height;
        return state + (1L << 42 + col * 3);
    }

    static ArrayList<Long> nextStates(long state, int piece) {
        ArrayList<Long> moves = new ArrayList<>();
        for (int i : Main.moveOrder) {
            int height = (int) (state >>> 42 + i * 3 & 0b111);
            long move = nextState(state, piece, i, height);
            if (height < 6) moves.add(move);
        }
        return moves;
    }
}
