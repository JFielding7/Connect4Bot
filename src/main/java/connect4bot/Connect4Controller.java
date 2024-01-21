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

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Connect4Controller implements Initializable {
    private static final int ROWS = 6, COLUMNS = 7;
    private static final int BOARD_WIDTH = 462, BOARD_HEIGHT = 336, BOARD_X = 144, BOARD_Y = 70;
    private static final int CELL_WIDTH = BOARD_WIDTH / 7, CELL_HEIGHT = BOARD_HEIGHT / 6;
    private static final int PIECE_RADIUS = 25;
    private static final int DROP_START_Y = 42;
    private static final double FADED_OPACITY = .375;
    private final HashMap<Integer, Circle> spotsFilled = new HashMap<>();
    private Game game;
    @FXML
    private Circle moveMarker;
    @FXML
    private AnchorPane backGround;
    @FXML
    private GridPane grid;
    @FXML
    private HBox startOptions;
    @FXML
    private HBox endOptions;
    @FXML
    private Label message;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeBoard();
    }

    public void initializeBoard() {
        Shape board = new Rectangle(BOARD_X, BOARD_Y, BOARD_WIDTH, BOARD_HEIGHT);
        for(int c = 0; c < COLUMNS; c++) {
            for(int r = 0; r < ROWS; r++) {
                board = Shape.subtract(board, getPiece("white", BOARD_X + CELL_WIDTH * (c + 0.5), BOARD_Y + CELL_HEIGHT * (r + 0.5)));
            }
            Pane colPane = new Pane();
            enableMouseEvents(colPane, c);
            grid.add(colPane, c, 0);
        }
        board.setFill(Color.BLUE);
        board.setMouseTransparent(true);
        backGround.getChildren().add(board);
    }

    private void enableMouseEvents(Pane colPane, int colIndex) {
        colPane.setOnMouseEntered(e0 -> runOnValidPLayerMove(colIndex, e0, e1 -> {
            moveMarker.setCenterX(BOARD_X + CELL_WIDTH * (0.5 + colIndex));
            moveMarker.setCenterY(BOARD_Y + BOARD_HEIGHT - CELL_HEIGHT * (0.5 + game.getHeight(colIndex)));
            moveMarker.setVisible(true);
        }));
        colPane.setOnMouseExited(e -> moveMarker.setVisible(false));
        colPane.setOnMouseClicked(e0 -> runOnValidPLayerMove(colIndex, e0, e1 -> {
            message.setText("");
            game.makePlayerTurn(colIndex);
            moveMarker.setVisible(false);
            TranslateTransition drop = dropPiece(colIndex);
            drop.setOnFinished(e -> {
                if (gameNotOver(true)) makeComputerMove();
            });
            drop.play();
        }));
    }

    private void runOnValidPLayerMove(int col, MouseEvent event, EventHandler<MouseEvent> handler) {
        if (game != null && game.isPlayerTurn && game.getHeight(col) != 6) handler.handle(event);
    }

    private void makeComputerMove() {
        TranslateTransition pieceDrop = dropPiece(game.makeComputerMove());
        pieceDrop.setOnFinished(e -> {
            if (gameNotOver(false)) {
                game.isPlayerTurn = true;
                message.setText("Your Turn");
                grid.getChildren().forEach(col -> {
                    if (col.isHover()) col.getOnMouseEntered().handle(null);
                });
            }
        });
        pieceDrop.play();
    }

    private boolean gameNotOver(boolean isPlayerTurn) {
        int result = game.checkGameOver();
        if (result == Game.NOT_OVER) return true;
        else if (result == Game.WIN) {
            message.setText(isPlayerTurn ? "You Win!" : "Computer Wins!");
            highlightWin();
        }
        else message.setText("Draw!");
        startOptions.setVisible(false);
        message.setVisible(true);
        return false;
    }

    public TranslateTransition dropPiece(int col) {
        double dur = 1;
        int height = game.getHeight(col) - 1;
        Circle piece = getPiece((game.movesMde & 1) == 1 ? "red" : "yellow", BOARD_X + CELL_WIDTH / 2d + CELL_WIDTH * col, DROP_START_Y);
        spotsFilled.put(col * 7 + height, piece);
        backGround.getChildren().add(piece);
        piece.toBack();
        TranslateTransition tr = new TranslateTransition(Duration.seconds(dur), piece);
        tr.setByY(BOARD_HEIGHT - CELL_HEIGHT * height);
        return tr;
    }

    public Circle getPiece(String color, double x, double y) {
        Circle piece = new Circle();
        piece.setRadius(PIECE_RADIUS);
        piece.setCenterX(x);
        piece.setCenterY(y);
        piece.setFill(Color.valueOf(color));
        return piece;
    }

    public void setPlayerStarting() {
        game = new Game(true);
        setOptions();
        message.setText("Your Turn");
        moveMarker.setFill(Color.RED);
    }

    public void setComputerStarting() {
        game = new Game(false);
        setOptions();
        message.setText("");
        moveMarker.setFill(Color.YELLOW);
        makeComputerMove();
    }

    public void setRandomStart() {
        int choice = new Random().nextInt(2);
        if (choice == 1) setPlayerStarting();
        else setComputerStarting();
    }

    private void setOptions() {
        startOptions.setVisible(false);
        endOptions.setVisible(true);
    }

    public void highlightWin() {
        HashSet<Integer> winningSpots = game.getWinningSpots();
        for (int spot : spotsFilled.keySet()) {
            if (!winningSpots.contains(spot)) spotsFilled.get(spot).setOpacity(FADED_OPACITY);
        }
    }

    public void playAgain() throws IOException {
        Connect4Application.loadScene("connect4.fxml");
    }

    public void quit() throws IOException {
        Connect4Application.loadScene("title.fxml");
    }
}
