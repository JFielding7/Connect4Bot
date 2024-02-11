module Connect4bot {
    requires javafx.controls;
    requires javafx.fxml;

    opens connect4bot to javafx.fxml;
    exports connect4bot;
}
