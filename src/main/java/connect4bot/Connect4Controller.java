package connect4bot;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
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

    byte R = 6, C = 7;
    Shape board;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeBoard();
        translateTest();
    }

    public void initializeBoard() {
        Rectangle rect = new Rectangle();
        rect.setHeight(330);
        rect.setWidth(465);
        rect.setLayoutX(67);
        rect.setLayoutY(35);
        board = rect;
        for(int i=0; i<R; i++) {
            for(int j=0; j<C; j++) {
                Circle c = new Circle();
                c.setRadius(25);
                c.setCenterX(103+65*j);
                c.setCenterY(62+55*i);
                board = Shape.subtract(board, c);
            }
        }
        board.setFill(Color.BLUE);
        board.setId("board");
        bg.getChildren().add(board);
    }

    public void translateTest() {
        double dur = 1;

        Circle c = new Circle();
        c.setRadius(25);
        c.setCenterX(103+3*65);
        c.setCenterY(7);
        bg.getChildren().add(c);
        c.toBack();
        c.setFill(Color.RED);
        TranslateTransition tr = new TranslateTransition(Duration.seconds(dur), c);
        tr.setByY(330);
        tr.play();
    }
}