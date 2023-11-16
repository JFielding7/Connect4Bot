package connect4bot;

public class Piece {
    String color;
    int p;

    public Piece() {p=0;}

    public void set(int p) {
        this.p = p;
        color = p==1 ? "red" : "yellow";
    }

    public boolean isEmpty() {
        return p==0;
    }

    public int getBit() {
        return (p==1 ? 1 : 0);
    }

    public String toString() {
        if(p==0) return " ";
        return p==1 ? "+" : "o";
    }
}
