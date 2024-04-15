package connect4bot;

import java.util.HashSet;
import static connect4bot.Engine.*;

/**
 * Keeps track of the current state of the game
 */
public class Game {
    /**
     * Number of spots on the board
     */
    static final int SPOTS = 42;
    /**
     * Represents the result of the game
     */
    static final int WIN = 1, DRAW = 0, NOT_OVER = -1;
    /**
     * The current turn in the game
     */
    int turn = 1;
    /**
     * Number of moves made
     */
    int movesMde;
    /**
     * true if it is currently the user's turn, false otherwise
     */
    boolean isPlayerTurn;
    /**
     * The current state of the game board
     */
    long state;

    /**
     * Creates a new Game
     * @param playerStarts true if the user starts, false otherwise
     */
    public Game(boolean playerStarts) {
        isPlayerTurn = playerStarts;
    }

    /**
     * Makes the user's move
     * @param col The index of the column the user plays in
     */
    public void makePlayerTurn(int col) {
        isPlayerTurn = false;
        playMove(col);
    }

    /**
     * Makes the computer's move
     * @return The index of the column which the computer plays in
     */
    public int makeComputerMove() {
        int col = Solver.makeOptimalMove(state, turn, movesMde);
        playMove(col);
        return col;
    }

    /**
     * Plays a move
     * @param col The index of the column to play in
     */
    private void playMove(int col) {
        state = nextState(state, turn, col, getHeight(col));
        turn ^= 1;
        movesMde++;
    }

    /**
     * Gets the height of a column
     * @param col The index of the column
     * @return THe height of the column
     */
    public int getHeight(int col) {
        return (int) (state >>> SPOTS + col * 3 & 0b111);
    }

    /**
     * Checks if the game is over
     * @return Integer representing the result of the game
     */
    public int checkGameOver() {
        if (isWin(state, turn ^ 1)) return WIN;
        if (movesMde == SPOTS) return DRAW;
        return NOT_OVER;
    }

    /**
     * Finds the four spots that create a win if one exists
     * @return Set containing the winning spots if they exist, null otherwise
     */
    public HashSet<Integer> getWinningSpots() {
        long board = getPieceLocations(state, turn ^ 1);
        for (int i = 1; i < 9; i += (1 / i << 2) + 1) {
            long connections = board;
            for (int j = 0; j < 3; j++) connections = connections & (connections >>> i);
            if (connections != 0) {
                HashSet<Integer> winningSpots = new HashSet<>();
                int start = Long.numberOfTrailingZeros(connections);
                for (int k = 0; k < 4; k++) winningSpots.add(start + i * k);
                return winningSpots;
            }
        }
        return null;
    }
}
