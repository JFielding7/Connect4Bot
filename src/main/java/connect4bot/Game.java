package connect4bot;

import static connect4bot.Engine.*;

public class Game {

    static final int SPOTS = 42;

    int turn = 1, depth;
    final int playerTurn;
    long state;

    public Game(int playerTurn) {
        this.playerTurn = playerTurn;
    }

    public void makePlayerTurn(int col) {
        playMove(col);
    }

    public int makeComputerMove() {
        int col = makeOptimalMove(state, turn, depth);
        playMove(col);
        return col;
    }

    private void playMove(int col) {
        state = nextState(state, turn, col, getHeight(col));
        turn ^= 1;
        depth++;
    }

    public int getHeight(int col) { return (int) (state >>> SPOTS + col * 3 & 0b111); }

    public boolean checkWin() { return Engine.isWin(state, turn ^ 1); }
}
