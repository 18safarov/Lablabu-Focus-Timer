package lablabu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lablabu.model.AppState;
import lablabu.service.StorageService;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        StorageService storage = new StorageService();
        AppState state = storage.load();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/lablabu/main-view-v2.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 900, 650);

        String cssFile = state.getTheme().equals("light") ? "theme-light.css" : "theme-dark.css";
        scene.getStylesheets().add(getClass().getResource("/lablabu/" + cssFile).toExternalForm());

        stage.setTitle("Lablabu - Focus Timer");
        stage.setResizable(true);
        stage.setMinWidth(850);
        stage.setMinHeight(600);

        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/lablabu/beet.png").toString()));

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
