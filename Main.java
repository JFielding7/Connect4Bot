import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String pos = """
                0111100
                0001000
                1101000
                0101000
                1010000
                0001000""";
        String pos2 = """
                1111111
                1110111
                0000111
                1011111
                0101111
                1110111""";
        System.out.println(flipTurn(encodePos(pos)) == encodePos(pos2));
        System.out.println(checkWin(encodePos(pos), 5));
        long start = System.currentTimeMillis();
        long posNum = encodePos(pos);
        for(long i = 0; i < 40000000; i++){
            boolean win = checkWin(i, (int) i % 42);
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    static long encodePos(String board){
        String[] cols = new String[7];
        Arrays.fill(cols, "");
        for(String row : board.split("\n")){
            for(int i = 0; i < 7; i++){
                cols[i] += row.charAt(i);
            }
        }
        return ((long) cols[0].length() << 60) + ((long) cols[1].length() << 57) + ((long) cols[2].length() << 54) + ((long) cols[3].length() << 51) + ((long) cols[4].length() << 48) + ((long) cols[5].length() << 45) + ((long) cols[6].length() << 42)
                + (Long.parseLong(cols[0], 2) << 36) + (Long.parseLong(cols[1], 2) << 30) + (Long.parseLong(cols[2], 2) << 24) + (Long.parseLong(cols[3], 2) << 18) + (Long.parseLong(cols[4], 2) << 12) + (Long.parseLong(cols[5], 2) << 6) + (Long.parseLong(cols[6], 2));
    }

    public static boolean checkWin(long state, int lastMove){
        int row = lastMove % 6, col = lastMove / 6;
        return connectionCount(state, lastMove, row, col, 0, 1, 6) + connectionCount(state, lastMove, row, col, 0, -1, -6) >= 3 ||
                connectionCount(state, lastMove, row, col, -1, 0, -1) >= 3 ||
                connectionCount(state, lastMove, row, col, 1, 1, 7) + connectionCount(state, lastMove, row, col, -1, -1, -7) >= 3 ||
                connectionCount(state, lastMove, row, col, 1, -1, -5) + connectionCount(state, lastMove, row, col, -1, 1, 5) >= 3;
    }

    static int connectionCount(long grid, int lastMove, int row, int col, int dr, int dc, int dp){
        int count = 0;
        int r = row + dr;
        int c = col + dc;
        int pos = lastMove + dp;
        while(r > -1 && r < 6 && c > -1 && c < 7 && (grid >> pos & 1) == 1){
            count++;
            r += dr;
            c += dc;
            pos += dp;
        }
        return count;
    }

    static long flipTurn(long grid){
        int[] heights = new int[7];
        for(int i = 0; i < 7; i++){
            heights[i] = (int) (grid >> (42 + 3 * i) & 7);
        }
        System.out.println(Arrays.toString(heights));
        long flippedGrid = 0L;
        for(int i = 0; i < 7; i++){
            int height = heights[i];
            if(height == 0) continue;
            int col = (int) (grid >> (i * 6) & 63) ^ ((1 << (height)) - 1);
            System.out.print((grid >> (i * 6) & 63));
            System.out.println(" " + (grid >> (i * 6) & 63 ^ 63));
            flippedGrid += ((long) col) << i * 6;
        }
        System.out.println(Long.toBinaryString(grid));
        flippedGrid = flippedGrid + (grid >> 42 << 42);
        System.out.println(Long.toBinaryString(flippedGrid));
        return flippedGrid;
    }
}