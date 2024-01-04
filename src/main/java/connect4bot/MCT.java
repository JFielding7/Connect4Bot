package connect4bot;

import java.util.ArrayList;
import java.util.Random;

public class MCT {

    Node root;
    private static final double EXPLORATION_CONSTANT = 2;

    public MCT(long state, int depth) {
        root = new Node(state, depth);
    }

    public static void main(String[] args) {
        String board =  "       \n" +
                        "   1   \n" +
                        "   0   \n" +
                        "   1   \n" +
                        "   0 1 \n" +
                        "   1100\n";
        long pos = Main.encode(board);
        System.out.println(Node.notImmediateLosses(pos, 0).size());
        int depth = 2 * board.length() - board.replace("1", "").length() - board.replace("0", "").length();
        Node node = new Node(pos, depth);
        int trials = 20_000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < trials; i++)
            Node.selectionSimulation(node);
        System.out.println(node.wins / trials);
        System.out.println(System.currentTimeMillis() - start);
    }

    private static class Node {
        long state;
        double wins;
        int simulations, piece, depth;
        ArrayList<Node> children;

        Node(long state, int depth) {
            this.state = state;
            this.depth = depth;
            this.piece = (depth & 1) ^ 1;
        }

        static double selectionSimulation(Node node) {
            node.simulations++;
            if (node.children == null) {
                node.children = getChildren(node);
                if (node.children.isEmpty()) return node.piece ^ 1;
                return randomSimulation(node);
            }
            if (node.children.isEmpty()) return node.piece ^ 1;
            Node selected = node.children.get(0);
            double maxVal = -1;
            for (Node child : node.children) {
                if (child.simulations == 0) {
                    selected = child;
                    break;
                }
                double val;
                if (node.piece == 1) val = child.wins / child.simulations + Math.sqrt(EXPLORATION_CONSTANT * Math.log(node.simulations) / child.simulations);
                else val = (child.simulations - child.wins) / child.simulations + Math.sqrt(EXPLORATION_CONSTANT * Math.log(node.simulations) / child.simulations);
                if (val > maxVal) {
                    maxVal = val;
                    selected = child;
                }
            }
            double result = selectionSimulation(selected);
            node.wins += result;
            return result;
        }

        static double randomSimulation(Node node) {
            long state = node.state;
            int piece = node.piece, depth = node.depth;
            Random rand = new Random();
            while (depth++ < 42) {
                ArrayList<Long> moves = notImmediateLosses(state, piece);
                if (moves.isEmpty()) return piece ^ 1;
                state = moves.get(rand.nextInt(moves.size()));
                piece ^= 1;
            }
            return 0.5;
        }

        static ArrayList<Node> getChildren(Node node) {
            ArrayList<Node> children = new ArrayList<>();
            for (long state : notImmediateLosses(node.state, node.piece)) {
                children.add(new Node(state, node.depth + 1));
            }
            return children;
        }

        static long nextState(long state, int piece, int col, int height){
            if(piece == 1) state += 1L << col * 6 + height;
            return state + (1L << 42 + col * 3);
        }

        static int[] moveOrder = {3, 2, 4, 5, 1, 6, 0};

        static ArrayList<Long> notImmediateLosses(long state, int piece) {
            ArrayList<Long> moves = new ArrayList<>(), forcedMoves = new ArrayList<>();
            int oppPiece = piece ^ 1;
            for (int i : moveOrder) {
                int height = (int) (state >>> 42 + i * 3 & 0b111);
                long move = nextState(state, piece, i, height);
                if (height < 6) {
                    if (height == 5 || !Main.isWin(nextState(move, oppPiece, i, height + 1), oppPiece)) moves.add(move);
                    if (Main.isWin(nextState(state, oppPiece, i, height), oppPiece)) {
                        if (forcedMoves.size() == 1) return new ArrayList<>();
                        forcedMoves.add(move);
                    }
                }
            }
            return forcedMoves.size() == 1 ? forcedMoves : moves;
        }
    }
}
