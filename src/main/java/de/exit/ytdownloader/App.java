package de.exit.ytdownloader;

import de.exit.ytdownloader.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    private MainWindow mainWindow;

    @Override
    public void start(Stage primaryStage) {
        mainWindow = new MainWindow(primaryStage);

        Scene scene = new Scene(mainWindow, 960, 640);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/de/exit/ytdownloader/style.css")
        ).toExternalForm());

        primaryStage.setTitle("YouTube Downloader");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(520);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(e -> {
            if (mainWindow != null) {
                mainWindow.cleanup();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
