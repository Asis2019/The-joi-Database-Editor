package asis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.JSONObject;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Main.fxml"));
        Parent root = fxmlLoader.load();

        Controller controller = fxmlLoader.getController();
        Scene main_scene = new Scene(root, 1280, 720);

        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("images/icon.png")));
        primaryStage.setScene(main_scene);
        primaryStage.setTitle("Editor");
        primaryStage.show();
        primaryStage.setMaximized(true);

        Controller.getInstance().actionNewProject();

        primaryStage.setOnCloseRequest(event -> {
            //Check if dialog is needed
            JSONObject storyData = Story.getInstance().getStoryDataJson();
            JSONObject metadataObject = Story.getInstance().getMetadataObject();

            if(storyData == null && metadataObject == null) {
                controller.quiteProgram();
                return;
            }

            if (Controller.getInstance().getNewChanges()) {
                int choice = new Alerts().unsavedChangesDialog(this.getClass(), "Warning", "You have unsaved work, are you sure you want to quit?");
                switch (choice) {
                    case 0:
                        event.consume();
                        break;

                    case 1:
                        controller.quiteProgram();
                        break;

                    case 2:
                        controller.actionSaveProject();
                        controller.quiteProgram();
                        break;
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
