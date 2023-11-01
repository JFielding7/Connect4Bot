module com.example.connect4bot {
    requires javafx.controls;
    requires javafx.fxml;


    opens connect4bot to javafx.fxml;
    exports connect4.connect4bot;
}