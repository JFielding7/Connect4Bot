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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controllers the gui elements used when playing a game
 */
public class Connect4Controller implements Initializable {
    /**
     * Number of rows and columns in the board
     */
    private static final int ROWS = 6, COLUMNS = 7;
    /**
     * Dimensions of the board
     */
    private static final int BOARD_WIDTH = 462, BOARD_HEIGHT = 336, BOARD_X = 144, BOARD_Y = 70;
    /**
     * Dimensions of each cell on the board
     */
    private static final int CELL_WIDTH = BOARD_WIDTH / 7, CELL_HEIGHT = BOARD_HEIGHT / 6;
    /**
     * Radius of each game piece
     */
    private static final int PIECE_RADIUS = 25;
    /**
     * Starting y location of a piece when it is dropped into the board
     */
    private static final int DROP_START_Y = 42;
    /**
     * Opacity of a faded piece
     */
    private static final double FADED_OPACITY = .375;
    /**
     * Stores all spots filled during the game
     */
    private final HashMap<Integer, Circle> spotsFilled = new HashMap<>();
    /**
     * Keeps track of the current state of the game
     */
    private Game game;
    /**
     * Highlights the spot where the user's piece will fall
     */
    @FXML
    private Circle moveMarker;
    /**
     * Background of the scene
     */
    @FXML
    private AnchorPane backGround;
    /**
     * Container for the columns
     */
    @FXML
    private GridPane grid;
    /**
     * Options for who starts the game
     */
    @FXML
    private HBox startOptions;
    /**
     * Options that allow the user to quit or restart
     */
    @FXML
    private HBox endOptions;
    /**
     * Label that displays different messages to the user
     */
    @FXML
    private Label message;

    /**
     * Initializes the gui elements for starting the game
     * @param url N/A
     * @param resourceBundle N/A
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Shape board = new Rectangle(BOARD_X, BOARD_Y, BOARD_WIDTH, BOARD_HEIGHT);
        for(int c = 0; c < COLUMNS; c++) {
            for(int r = 0; r < ROWS; r++) {
                board = Shape.subtract(board, getPiece(Color.WHITE, BOARD_X + CELL_WIDTH * (c + 0.5), BOARD_Y + CELL_HEIGHT * (r + 0.5)));
            }
            Pane colPane = new Pane();
            configureMouseEvents(colPane, c);
            grid.add(colPane, c, 0);
        }
        board.setFill(Color.BLUE);
        board.setMouseTransparent(true);
        backGround.getChildren().add(board);
    }

    /**
     * Sets up events that are dispatched when the user clicks a column
     * @param colPane The Pane containing the column
     * @param col The index of the column
     */
    private void configureMouseEvents(Pane colPane, int col) {
        colPane.setOnMouseEntered(e0 -> runOnValidPLayerMove(col, e0, e1 -> {
            moveMarker.setCenterX(BOARD_X + CELL_WIDTH * (0.5 + col));
            moveMarker.setCenterY(BOARD_Y + BOARD_HEIGHT - CELL_HEIGHT * (0.5 + game.getHeight(col)));
            moveMarker.setVisible(true);
        }));
        colPane.setOnMouseExited(e -> moveMarker.setVisible(false));
        colPane.setOnMouseClicked(e0 -> runOnValidPLayerMove(col, e0, e1 -> {
            message.setText("");
            game.makePlayerTurn(col);
            moveMarker.setVisible(false);

            AtomicInteger computerMove = new AtomicInteger(-1);
            Thread moveThread = new Thread(() -> {
                if (game.checkGameOver() == Game.NOT_OVER) computerMove.set(game.makeComputerMove());
            });
            moveThread.start();

            TranslateTransition drop = dropPiece(col);
            drop.setOnFinished(e -> {
                try {
                    moveThread.join();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                if (computerMove.get() != -1) makeComputerMove(computerMove.get());
                else showResult(true);
            });
            drop.play();
        }));
    }

    /**
     * Handles a mouse event when it is the user's turn
     * @param col The index of the column
     * @param event The event to handler
     * @param handler The event handler
     */
    private void runOnValidPLayerMove(int col, MouseEvent event, EventHandler<MouseEvent> handler) {
        if (game != null && game.isPlayerTurn && game.getHeight(col) != 6) handler.handle(event);
    }

    /**
     * Makes the computer's move
     * @param move The index of the computer selected column
     */
    private void makeComputerMove(int move) {
        TranslateTransition pieceDrop = dropPiece(move);
        pieceDrop.setOnFinished(e -> {
            int result = game.checkGameOver();
            if (result == Game.NOT_OVER) {
                game.isPlayerTurn = true;
                message.setText("Your Turn");
                grid.getChildren().forEach(col -> {
                    if (col.isHover()) col.getOnMouseEntered().handle(null);
                });
            }
            else showResult(false);
        });
        pieceDrop.play();
    }

    /**
     * Displays the result of the game
     * @param isPlayerTurn true if the user just played the last move, false otherwise
     */
    private void showResult(boolean isPlayerTurn) {
        int result = game.checkGameOver();
        if (result == Game.DRAW) message.setText("Draw!");
        else message.setText(isPlayerTurn ? "You Win!" : "Computer Wins!");
        message.setVisible(true);
        highlightWin();
    }

    /**
     * Creates the animation for dropping a piece into a column
     * @param col The index of the column
     * @return The animation
     */
    public TranslateTransition dropPiece(int col) {
        Circle piece = getPiece((game.movesMde & 1) == 1 ? Color.RED : Color.YELLOW, BOARD_X + CELL_WIDTH / 2d + CELL_WIDTH * col, DROP_START_Y);
        int height = game.getHeight(col) - 1;
        spotsFilled.put(col * 7 + height, piece);
        backGround.getChildren().add(piece);
        piece.toBack();
        TranslateTransition drop = new TranslateTransition(Duration.seconds(0.6), piece);
        drop.setByY(BOARD_HEIGHT - CELL_HEIGHT * height);
        return drop;
    }

    /**
     * Creates a new piece
     * @param color The color of the piece
     * @param x The x-position of the piece
     * @param y The y-position of the piece
     * @return THe piece
     */
    public Circle getPiece(Color color, double x, double y) {
        Circle piece = new Circle();
        piece.setRadius(PIECE_RADIUS);
        piece.setCenterX(x);
        piece.setCenterY(y);
        piece.setFill(color);
        return piece;
    }

    /**
     * Sets up the game for the user to start
     */
    public void setPlayerStarting() {
        game = new Game(true);
        displayEndOptions();
        message.setText("Your Turn");
        moveMarker.setFill(Color.RED);
    }

    /**
     * Sets up the game for the computer to start
     */
    public void setComputerStarting() {
        game = new Game(false);
        displayEndOptions();
        message.setText("");
        moveMarker.setFill(Color.YELLOW);
        makeComputerMove(game.makeComputerMove());
    }

    /**
     * Sets up the game for a random player to start
     */
    public void setRandomStart() {
        if (new Random().nextInt(2) == 1) setPlayerStarting();
        else setComputerStarting();
    }

    /**
     * Displays the options for ending the game
     */
    private void displayEndOptions() {
        startOptions.setVisible(false);
        endOptions.setVisible(true);
    }

    /**
     * Highlights the pieces that create a win
     */
    public void highlightWin() {
        HashSet<Integer> winningSpots = game.getWinningSpots();
        if (winningSpots == null) return;
        for (int spot : spotsFilled.keySet()) {
            if (!winningSpots.contains(spot)) spotsFilled.get(spot).setOpacity(FADED_OPACITY);
        }
    }

    /**
     * Restarts the game
     */
    public void playAgain() throws IOException {
        Connect4Application.loadScene("connect4.fxml");
    }

    /**
     * Returns to the title screen
     */
    public void quit() throws IOException {
        Connect4Application.loadScene("title.fxml");
    }
}
