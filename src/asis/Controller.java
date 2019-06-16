package asis;

import asis.custom_objects.Draggable;
import asis.custom_objects.asis_node.SceneNode;
import asis.custom_objects.asis_node.SceneNodeMainController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.dialog.ExceptionDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Controller {
    private Story story = new Story();
    private int numberOfScenes = 0;

    private SceneNodeMainController sceneNodeMainController;

    @FXML private AnchorPane anchorPane;

    public void initialize() {

    }

    void inflater() {
         sceneNodeMainController = new SceneNodeMainController();
         sceneNodeMainController.setPane(anchorPane);

        addScene();

        createMetadataNode();
    }

    private void createMetadataNode() {
        SceneNode metaDataNode = new SceneNode(300, 100, "metaData", sceneNodeMainController);
        new Draggable.Nature(metaDataNode.getPane());
        metaDataNode.getPane().setLayoutX(10);
        metaDataNode.getPane().setLayoutY(200);
        metaDataNode.setTitle("Metadata");
        metaDataNode.getPane().setOnMouseClicked(new EventHandler<>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //User double clicked
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if(mouseEvent.getClickCount() == 2){
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/meta_data_form.fxml"));
                            Parent root = fxmlLoader.load();

                            MetaDataForm metaDataForm = fxmlLoader.getController();
                            metaDataForm.inflateStoryObject(story);

                            Stage stage = new Stage();
                            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("images/icon.png")));
                            stage.setTitle("Project Details");
                            stage.setScene(new Scene(root, 400, 720));
                            stage.show();
                        } catch (IOException e) {
                            errorDialogWindow(e);
                        }
                    }
                }
            }
        });
        anchorPane.getChildren().add(metaDataNode.getPane());
    }

    private void setClickActionForNode(SceneNode sceneNode) {
        sceneNode.getPane().setOnMouseClicked(mouseEvent -> {
            //User double clicked
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if(mouseEvent.getClickCount() == 2){
                    openNewWindow(sceneNode.getTitle(), sceneNode.getSceneId());
                }
            }

            if(mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                if(mouseEvent.getClickCount() == 2) {
                    removeScene(sceneNode);
                }
            }
        });
    }

    private void openNewWindow(String title, String sceneId) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/SceneDetails.fxml"));
            Parent root = fxmlLoader.load();

            SceneDetails sceneDetails = fxmlLoader.getController();
            sceneDetails.passData(story, sceneId);

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("images/icon.png")));
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
        } catch (IOException e) {
            errorDialogWindow(e);
        }
    }

    public void actionExit() {
        Platform.exit();
        System.exit(0);
    }

    public void addScene() {
        numberOfScenes++;

        story.addNewScene(String.valueOf(numberOfScenes));

        SceneNode sceneNode = new SceneNode(300, 100, String.valueOf(numberOfScenes), sceneNodeMainController);
        new Draggable.Nature(sceneNode.getPane());
        sceneNode.setTitle("Scene "+numberOfScenes);
        sceneNode.getPane().setLayoutX(10);
        setClickActionForNode(sceneNode);
        anchorPane.getChildren().add(sceneNode.getPane());
    }

    private void removeScene(SceneNode sceneNode) {
        numberOfScenes--;

        story.removeScene(sceneNode.getSceneId());

        anchorPane.getChildren().remove(sceneNode.getPane());
    }

    private void writeJsonToFile(JSONObject jsonObject, String fileName, File saveLocation) {
        try {
            FileWriter fileWriter = new FileWriter(saveLocation.toPath() + File.separator + fileName);
            fileWriter.write(jsonObject.toString());
            fileWriter.flush();
        } catch (IOException e) {
            errorDialogWindow(e);
        }
    }

    private JSONObject readJsonFromFile(File file) {
        try {
            String text = new String(Files.readAllBytes(file.toPath()));
            return new JSONObject(text);
        } catch (IOException | JSONException e) {
            errorDialogWindow(e);
        }
        return null;
    }

    private void errorDialogWindow(Exception e) {
        ExceptionDialog exceptionDialog = new ExceptionDialog(e);
        exceptionDialog.setTitle("Error");
        exceptionDialog.setHeaderText("Oh no an error! Send it to Asis so he can feel bad.\n"+e.getMessage());
        exceptionDialog.show();
    }

    public void actionNewProject() {
        //TODO Will init some things within the story object
    }

    public void actionLoadProject() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(story.getProjectDirectory());
        File file = directoryChooser.showDialog(null);

        if(file != null) {
            story.setProjectDirectory(file);
        }
    }

    public void actionSaveProject() {
        writeJsonToFile(story.getMetadataObject(), "info_en.json", story.getProjectDirectory());
        writeJsonToFile(story.getStoryDataJson(), "joi_text_en.json", story.getProjectDirectory());

        //Copy image to project directory
        for (File file : story.getImagesArray()) {
            try {
                Files.copy(file.toPath(), story.getProjectDirectory().toPath().resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Copy icon to project directory
        try {
            if(story.getMetadataIcon() != null) {
                File file = story.getMetadataIcon();
                Files.copy(file.toPath(), story.getProjectDirectory().toPath().resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
