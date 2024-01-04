package connect4bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.IntStream;

public class Board {
    Piece[][] board;
    int R = 6, C = 7;
    HashMap<Long, Integer> cache;

    public Board() {
        board = setBoard();
        cache = new HashMap<>();
    }

    public Piece[][] setBoard() {
        Piece[][] board = new Piece[R][C];
        IntStream.range(0, R).parallel().forEach(i -> IntStream.range(0, C).parallel().forEach(j -> board[i][j] = new Piece()));
        return board;
    }

    public boolean play(Piece[][] board, int p, int c) {
        if(c>=0 && c<=C && board[0][c].isEmpty()) {
            for(int r=R-1; r>=0; r--) {
                if(board[r][c].isEmpty()) {
                    board[r][c].set(p);
                    return true;
                }
            }
        } return false;
    }

    public Piece[][] setBoard(int[][] set) {
        Piece[][] board = setBoard();
        for(int r=0; r<R; r++) {
            for(int c=0; c<C; c++) {
                board[r][c] = new Piece();
                board[r][c].set(set[r][c]);
            }
        }
        return board;
    }
    public Piece[][] copy(Piece[][] board) {
        Piece[][] copy = setBoard();
        IntStream.range(0, R).parallel().forEach(i -> IntStream.range(0, C).parallel().forEach(j -> copy[i][j].set(board[i][j].p)));
        return copy;
    }
    public Piece[][] copy(long code) {
        return copy(decode(code));
    }

    /**
     * Checks if a win exists on the board
     * @param board current board
     * @return winner of board, 0 if none
     */
    public int evaluate(Piece[][] board) {
        for(int r=0; r<R; r++) {
            int result = checkWin(board, r, 3);
            if(result!=0) return result;
        }
        for(int c=0; c<C/2; c++) {
            int r1 = checkWin(board, 2, c);
            int r2 = checkWin(board, 2, C-c-1);
            if(r1 != 0) return r1;
            if(r2 != 0) return r2;
        }
        return 0;
    }

    public int checkWin(Piece[][] board, int r, int c) {
        int p = board[r][c].p;

        int horizontal = connectionsHorizontal(board, r, c, 1) + connectionsHorizontal(board, r, c, -1);
        int vertical = connectionsVertical(board, r, c, 1);
        int negDiag = connectionsDiagonal(board, r, c, 1, 1) + connectionsDiagonal(board, r, c, -1, -1);
        int posDiag = connectionsDiagonal(board, r, c, -1, 1) + connectionsDiagonal(board, r, c, 1, -1);

        return Math.max(Math.max(horizontal, vertical), Math.max(negDiag, posDiag)) >= 3 ? p : 0;
    }

    public int connectionsHorizontal(Piece[][] board, int r, int c, int dc) {
        int p = board[r][c].p;
        int connections = 0;
        c += dc;
        while(c>=0 && c<C) {
            if(board[r][c].p == p) connections++;
            else break;
            c += dc;
        }
        return connections;
    }
    public int connectionsVertical(Piece[][] board, int r, int c, int dr) {
        int p = board[r][c].p;
        int connections = 0;
        r += dr;
        while(r>=0 && r<R) {
            if(board[r][c].p == p) connections++;
            else break;
            r += dr;
        }
        return connections;
    }
    public int connectionsDiagonal(Piece[][] board, int r, int c, int dr, int dc) {
        int p = board[r][c].p;
        int connections = 0;
        r += dr;
        c += dc;
        while(r>=0 && r<R && c>=0 && c<C) {
            if(board[r][c].p == p) connections++;
            else break;
            r += dr;
            c += dc;
        }
        return connections;
    }


    /**
     * Encoding process: heights of each column as 3 bits for 21 total bits, then fill each column (top to bottom) with its information (1 for p1, 0 for p2) for a total of 42 bits, making 63 bits total
     * @return encoded position
     */
    public long encode(Piece[][] board) {
        long h, data, code = 0;
        for(int c=0; c<C; c++) { // Height of columns
            h = 0;
            for(int r=R-1; r>=0; r--) {
                if(board[r][c].isEmpty()) break;
                h++;
            }
            code += h << (60-3*c);
        }
        for(int c=0; c<C; c++) { // Data of columns
            data = 0;
            for(int r=0; r<R; r++) {
                data += (long) board[r][c].getBit() << (R-r-1);
            }
            code += data << (36-6*c);
        }
        return code;
    }

    public Piece[][] decode(long code) {
        Piece[][] board = setBoard();
        for(int c=0; c<C; c++) {
            int h = (int) (code >> (60-3*c) & 7);
            for(int r=R-1; r>=0; r--) {
                if(r>=R-h) board[r][c].set((code >> (41-6*c-r) & 1)==1 ? 1 : -1);
                else board[r][c].set(0);
            }
        }
        return board;
    }

    /**
     * Gets a set of legal moves in the current game state
     * @param code game state
     * @return set of legal moves
     */
    public HashSet<Integer> getMoves(long code) {
        HashSet<Integer> moves = new HashSet<>();
        for(int c=0; c<C; c++) {
            if((code >> (60-3*c) & 7) < 6) moves.add(c);
        }
        return moves;
    }

    /**
     * Determines whether a position forces a move
     * @param code game state
     * @return index of forced col, -1 if none
     */
    public int forcedMove(long code) {
        HashSet<Integer>[] threats = findThreats(code);
        int[] heights = getHeights(code);
        for(int c=0; c<C; c++) {
            if(threats[c].contains(5-heights[c])) return c;
        }
        return -1;
    }

    /**
     * Gets all locations of places that the other player can win as a list of HashSets
     * @param code game state
     * @return threats as list of sets
     */
    public HashSet<Integer>[] findThreats(long code) {
        HashSet<Integer>[] threats = new HashSet[C];
        int[] heights = getHeights(code);
        int p = getP(code);
        Piece[][] copy;
        for(int c=0; c<C; c++) {
            threats[c] = new HashSet<>();
            for(int r=0; r<6-heights[c]; r++) {
                copy = copy(code);
                if(play(copy, -p, c) && checkWin(copy, r, c) == -p) threats[c].add(r);
            }
        }
        return threats;
    }

    // Returns the player whose turn it is
    public int getP(long code) {
        return (countEmptySlots(code) & 1) == 1 ? -1 : 1;
    }

    public int countEmptySlots(long code) {
        int empty = R*C;
        int[] heights = getHeights(code);
        for(int h : heights) {
            empty -= h;
        }
        return empty;
    }

    public int[] getHeights(long code) {
        int[] heights = new int[C];
        for(int c=0; c<C; c++) {
            heights[c] = (int) (code >> (60-3*c) & 7);
        }
        return heights;
    }
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for(int r=0; r<R; r++) {
            for(int c=0; c<C; c++) {
                ret.append(board[r][c].toString());
            }
            ret.append("\n");
        }
        return ret.toString();
    }
}
