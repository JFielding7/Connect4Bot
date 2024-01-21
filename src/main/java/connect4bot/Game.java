package connect4bot;

import java.util.HashSet;
import static connect4bot.Engine.*;

public class Game {
    static final int SPOTS = 42;
    static final int WIN = 1, DRAW = 0, NOT_OVER = -1;

    int turn = 1, movesMde;
    boolean isPlayerTurn;
    long state;

    public Game(boolean playerTurn) {
        this.isPlayerTurn = playerTurn;
    }

    public void makePlayerTurn(int col) {
        isPlayerTurn = false;
        playMove(col);
    }

    public int makeComputerMove() {
        int col = makeOptimalMove(state, turn, movesMde);
        playMove(col);
        return col;
    }

    private void playMove(int col) {
        state = nextState(state, turn, col, getHeight(col));
        turn ^= 1;
        movesMde++;
    }

    public int getHeight(int col) {
        return (int) (state >>> SPOTS + col * 3 & 0b111);
    }

    public int checkGameOver() {
        if (isWin(state, turn ^ 1)) return WIN;
        if (movesMde == SPOTS) return DRAW;
        return NOT_OVER;
    }

    public HashSet<Integer> getWinningSpots() {
        long board = adjustBoard(state, turn ^ 1);
        for (int i = 1; i < 9; i += 1 / i * 4 + 1) {
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
