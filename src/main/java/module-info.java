module com.example.connect4bot {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.connect4bot to javafx.fxml;
    exports com.example.connect4bot;
}