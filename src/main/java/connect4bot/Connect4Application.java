package connect4bot;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Connect4Application extends Application {
    private static Stage mainStage;

    @Override
    public void start(Stage stage) throws IOException {
        Engine.loadCaches();
        mainStage = stage;
        mainStage.setTitle("Connect Four AI");
        mainStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("Board.png"))));
        loadScene("title.fxml");
    }

    public static void loadScene(String name) throws IOException {
        FXMLLoader loader = new FXMLLoader(Connect4Application.class.getResource(name));
        Scene scene = new Scene(loader.load());
        mainStage.setScene(scene);
        mainStage.show();
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