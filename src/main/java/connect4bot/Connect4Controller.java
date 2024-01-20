package connect4bot;

import javafx.animation.TranslateTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class Connect4Controller implements Initializable {

    private static final int R = 6, C = 7;
    private static final int BOARD_WIDTH = 462, BOARD_HEIGHT = 336, BOARD_X = 70, BOARD_Y = 35;
    private static final int CELL_WIDTH = BOARD_WIDTH / 7, CELL_HEIGHT = BOARD_HEIGHT / 6;
    private static final int PIECE_RADIUS = 25;
    private static final int DROP_START_Y = 7;

    private Game game;

    @FXML
    private AnchorPane bg;
    @FXML
    private GridPane grid;
    @FXML
    private HBox startOptions;
    @FXML
    private Label message;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeBoard();
    }

    public void initializeBoard() {
        for(int c = 0; c < C; c++) {
            Pane colPane = new Pane();
            Shape column = new Rectangle(CELL_WIDTH + 1, BOARD_HEIGHT);
            for(int r = 0; r < R; r++) {
                column = Shape.subtract(column, piece("white", CELL_WIDTH / 2d, CELL_HEIGHT / 2d + CELL_HEIGHT * r));
            }
            column.setFill(Color.BLUE);
            enableMouseEvents(colPane, column, c);
            colPane.getChildren().add(column);
            grid.add(colPane, c, 0);
        }
    }

    private void enableMouseEvents(Pane colPane, Shape column, int colIndex) {
        colPane.setOnMouseEntered(e0 -> runOnValidPLayerTurn(colIndex, e0, e1 -> column.setFill(Color.LIGHTBLUE)));
        colPane.setOnMouseExited(e -> column.setFill(Color.BLUE));
        colPane.setOnMouseClicked(e0 -> runOnValidPLayerTurn(colIndex, e0, e1 -> {
            game.makePlayerTurn(colIndex);
            column.setFill(Color.BLUE);
            TranslateTransition drop = dropPiece(colIndex);
            drop.setOnFinished(e -> {if (!checkWin(false)) makeComputerMove();});
            drop.play();
        }));
    }

    private void runOnValidPLayerTurn(int col, MouseEvent event, EventHandler<MouseEvent> handler) {
        if (game != null && game.playerTurn == game.turn && game.getHeight(col) != 6) {
            handler.handle(event);
        }
    }

    private void makeComputerMove() {
        dropPiece(game.makeComputerMove()).play();
        if (game.checkWin()) {
            message.setText("Computer Wins!");
            message.setVisible(true);
        }
    }

    private boolean checkWin(boolean com) {
        if (game.checkWin()) {
            if (com) System.out.println("Computer Wins");
            else System.out.println("You Win");
            return true;
        }
        return false;
    }

    public TranslateTransition dropPiece(int col) {
        double dur = 1;
        int height = game.getHeight(col) - 1;
        Circle c = piece((game.depth & 1) == 1 ? "red" : "yellow", BOARD_X + CELL_WIDTH / 2d + CELL_WIDTH * col, DROP_START_Y);
        bg.getChildren().add(c);
        c.toBack();
        TranslateTransition tr = new TranslateTransition(Duration.seconds(dur), c);
        tr.setByY(BOARD_HEIGHT - CELL_HEIGHT * height);
        return tr;
    }

    public Circle piece(String color, double x, double y) {
        Circle c = new Circle();
        c.setRadius(PIECE_RADIUS);
        c.setCenterX(x);
        c.setCenterY(y);
        c.setFill(Color.valueOf(color));
        return c;
    }

    public void setPlayerStarting() {
        game = new Game(1);
        startOptions.setVisible(false);
    }

    public void setComputerStarting() {
        game = new Game(0);
        startOptions.setVisible(false);
        makeComputerMove();
    }

    public void setRandomStart() {
        int choice = new Random().nextInt(2);
        if (choice == 1) setPlayerStarting();
        else setComputerStarting();
    }
}
