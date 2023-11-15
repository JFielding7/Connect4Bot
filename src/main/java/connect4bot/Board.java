package connect4bot;

import java.util.stream.IntStream;

public class Board {
    Piece[][] board;
    int R = 6, C = 7;

    public Board() {
        board = new Piece[R][C];
        IntStream.range(0, R).parallel().forEach(i -> IntStream.range(0, C).parallel().forEach(j -> board[i][j] = new Piece()));
    }

    public boolean play(int p, int c) {
        if(c>=0 && c<=C && board[0][c].isEmpty()) {
            for(int r=R-1; r>=0; r--) {
                if(board[r][c].isEmpty()) {
                    board[r][c].set(p);
                    return true;
                }
            }
        } return false;
    }

    public void setBoard(int[][] set) {
        for(int r=0; r<R; r++) {
            for(int c=0; c<C; c++) {
                board[r][c] = new Piece();
                board[r][c].set(set[r][c]);
            }
        }
    }

    /**
     * Encoding process: heights of each column as 3 bits for 21 total bits, then fill each column (top to bottom) with its information (1 for p1, 0 for p2) for a total of 42 bits, making 63 bits total
     * @return encoded position
     */
    public long encode() {
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

    public void decode(long code) {
        for(int c=0; c<C; c++) {
            byte h = (byte) (code >> (60-3*c) & 7);
            for(int r=R-1; r>=0; r--) {
                if(r>=R-h) board[r][c].set((code >> (41-6*c-r) & 1)==1 ? 1 : -1);
                else board[r][c].set(0);
            }
        }
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
