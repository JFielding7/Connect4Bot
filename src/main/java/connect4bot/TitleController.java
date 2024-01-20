package connect4bot;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class TitleController {
    @FXML
    private Button start;

    public void startGame() throws IOException {
        Connect4Application.loadScene("connect4.fxml");
    }

}
