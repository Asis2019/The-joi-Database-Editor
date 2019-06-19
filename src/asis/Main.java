package asis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Main.fxml"));
        Parent root = fxmlLoader.load();

        Controller controller = fxmlLoader.getController();
        Scene main_scene = new Scene(root, 1280, 720);
        controller.inflater(primaryStage);

        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("images/icon.png")));
        primaryStage.setScene(main_scene);
        primaryStage.setTitle("Editor");
        primaryStage.show();
        primaryStage.setMaximized(true);

        primaryStage.setOnCloseRequest(event -> {
            primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> event.getEventType() );
            Alerts.warningDialog("Close Application", "Do you want to save your project first?", "", primaryStage, event);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
