package connect4bot;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Connect4Controller implements Initializable {

    @FXML
    private AnchorPane bg;
    @FXML
    private GridPane grid;

    byte R = 6, C = 7;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeBoard();
    }

    public void initializeBoard() {
        for(int c = 0; c < C; c++) {
            Pane colPane = new Pane();
            Shape column = new Rectangle(67, 336);
            for(int r = 0; r < R; r++) {
                column = Shape.subtract(column, piece("white", 33, 28 + 56 * r));
            }
            column.setFill(Color.BLUE);
            enableMouseEvents(colPane, column, c);
            colPane.getChildren().add(column);
            grid.add(colPane, c, 0);
        }
    }

    private void enableMouseEvents(Pane colPane, Shape column, int colIndex) {
        colPane.setOnMouseEntered(e -> column.setFill(Color.LIGHTBLUE));
        colPane.setOnMouseExited(e -> column.setFill(Color.BLUE));
        colPane.setOnMouseClicked(e -> translateTest(colIndex));
    }

    public void translateTest(int col) {
        double dur = 1;
        Circle c = piece("red", 103 + col * 66, 7);
        bg.getChildren().add(c);
        c.toBack();
        TranslateTransition tr = new TranslateTransition(Duration.seconds(dur), c);
        tr.setByY(336);
        tr.play();
    }

    public Circle piece(String color, double x, double y) {
        Circle c = new Circle();
        c.setRadius(25);
        c.setCenterX(x);
        c.setCenterY(y);
        c.setFill(Color.valueOf(color));
        return c;
    }
}
