package connect4bot;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;

import static connect4bot.Generator.*;
import static connect4bot.Main1.encode;
import static connect4bot.Solver.*;

public class Data {
    public static void main(String[] args) {
        Arrays.fill(lowerBoundCache, -1);
        Arrays.fill(upperBoundCache, -1);
        Generator.loadCaches();
        String p1 =     "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n";
        long state = encode(p1);
        System.out.println(state);
        int movesMade = 2 * p1.length() - p1.replace("1", "").length() - p1.replace("0", "").length();
        int piece = (movesMade & 1) ^ 1;
//        System.out.println(evaluatePosition(state, piece, WORST_EVAL, BEST_EVAL, movesMade));
//        System.out.println(decode(bestMoves(state, piece, movesMade).get(0)));
        generateLines(state, piece, movesMade, 4);
//        System.out.println(evaluatePosition(state, piece, WORST_EVAL, BEST_EVAL, movesMade));
//        Generator.updateDatabase();
        System.exit(0);
        readTimes();
        System.out.println(states.size());
        System.out.println(states.contains(162173567067226115L));
        int count = 0;
        for (long pos : states) {
            long start = System.currentTimeMillis();
            bestMoves(pos, 0, 7);
            System.out.println(count++);
            System.out.println(pos);
            System.out.println(System.currentTimeMillis() - start);
        }
        Solver.filterSymmetricalPositions();
//        Generator.updateDatabase();
    }

    static void generate() {
        try (BufferedReader in = new BufferedReader(new FileReader("states"))) {
            String line = in.readLine();
            while (line != null) {
                long state = Long.parseLong(line);
                for (long move : bestMoves(state, 0, 7)) {
                    for (long next : nextStates(move, 1))
                        bestMoves(next, 0, 9);
                }
                System.out.println(state);
                line = in.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static HashSet<Long> states = new HashSet<>();

    static void readTimes() {
        int count = 0;
        try (BufferedReader in = new BufferedReader(new FileReader("times"))) {
            String line = in.readLine();
            while (line != null) {
                long state = Long.parseLong(in.readLine());
                if (Integer.parseInt(in.readLine()) > 20000) {
                    count++;
                    states.addAll(nextStates(bestMoves(state, 0, 5).get(0), 1));
                }
                line = in.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(count);
    }
}
