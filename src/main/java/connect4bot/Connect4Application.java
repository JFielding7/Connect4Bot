package connect4bot;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.IOException;

public class Connect4Application extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Connect4Application.class.getResource("connect4.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Connect Four");
        stage.setScene(scene);
        stage.show();

        SizeChangeListener sizeListener = new SizeChangeListener(scene, scene.getWidth(), scene.getHeight());
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);
    }

    public static void main(String[] args) {
        launch();
    }

    private record SizeChangeListener(Scene scene, double width, double height) implements ChangeListener<Number> {
        public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
            scene.getRoot().getTransforms().setAll(new Scale(scene.getWidth() / width, scene.getHeight() / height, 0, 0));
        }
    }
}