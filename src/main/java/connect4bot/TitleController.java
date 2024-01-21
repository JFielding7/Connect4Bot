package connect4bot;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TitleController implements Initializable {

    @FXML
    private AnchorPane backGround;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final int OFFSET_X = 177, OFFSET_Y = 408, CELL_WIDTH = 66, CELL_HEIGHT = 56, ROWS = 6, COLS = 7;
        final int[] HEIGHTS = {0, 5, 1, 6, 4, 2, 1}, PARITIES = {0, 1, 0, 0, 1, 0, 0};
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                Color color = Color.YELLOW;
                if (r >= HEIGHTS[c]) color = Color.WHITE;
                else if ((r & 1) == PARITIES[c]) color = Color.RED;
                backGround.getChildren().add(new Circle(OFFSET_X + c * CELL_WIDTH, OFFSET_Y - r * CELL_HEIGHT, 25, color));
            }
        }
    }

    public void startGame() throws IOException {
        Connect4Application.loadScene("connect4.fxml");
    }
}
