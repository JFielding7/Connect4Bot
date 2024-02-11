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

/**
 * Starts up the Application
 */
public class Connect4Application extends Application {
    /**
     * The main window of the application
     */
    private static Stage mainStage;

    /**
     * Starts the application, shows the title screen
     * @param stage The main window of the application
     */
    @Override
    public void start(Stage stage) throws IOException {
        Engine.loadDatabase();
        mainStage = stage;
        mainStage.setTitle("Connect 4 AI");
        mainStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("Board.png"))));
        loadScene("title.fxml");
    }

    /**
     * Loads a new scene
     * @param name The name of the scene
     */
    public static void loadScene(String name) throws IOException {
        FXMLLoader loader = new FXMLLoader(Connect4Application.class.getResource(name));
        Scene scene = new Scene(loader.load());
        mainStage.setScene(scene);
        mainStage.show();
        SizeChangeListener sizeListener = new SizeChangeListener(scene, scene.getWidth(), scene.getHeight());
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);
    }

    /**
     * Launches the application
     * @param args N/A
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Automatically scales the elements in the gui when the widow is resized
     * @param scene
     * @param width
     * @param height
     */
    private record SizeChangeListener(Scene scene, double width, double height) implements ChangeListener<Number> {
        public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
            scene.getRoot().getTransforms().setAll(new Scale(scene.getWidth() / width, scene.getHeight() / height, 0, 0));
        }
    }
}