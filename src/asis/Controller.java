package asis;

import asis.custom_objects.Draggable;
import asis.custom_objects.asis_node.AsisConnectionButton;
import asis.custom_objects.asis_node.SceneNode;
import asis.custom_objects.asis_node.SceneNodeMainController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.dialog.ExceptionDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Controller {
    private Story story = new Story();
    private int numberOfScenes = 0;
    private double menuEventX;
    private double menuEventY;
    private Boolean addSceneContextMeanu = false;

    private SceneNodeMainController sceneNodeMainController;

    @FXML private AnchorPane anchorPane;
    @FXML private ScrollPane scrollPane;
    @FXML private MenuBar mainMenuBar;

    public void initialize() {

    }

    void inflater() {
         sceneNodeMainController = new SceneNodeMainController(this);
         sceneNodeMainController.setPane(anchorPane);
         sceneNodeMainController.setScrollPane(scrollPane);
         //TODO replace fixed number with one from mainMenuBar + height of toolbar
         sceneNodeMainController.setMenuBarOffset(70);

        addScene();
        contextMenu();

        createMetadataNode();
    }

    private void contextMenu() {
        ContextMenu contextMenu =  new ContextMenu();
        MenuItem menuItem1 = new MenuItem("New Scene");

        menuItem1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addSceneContextMeanu = true;
                addScene();
            }
        });

        contextMenu.getItems().add(menuItem1);

        scrollPane.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent contextMenuEvent) {
                menuEventX = contextMenuEvent.getX();
                menuEventY = contextMenuEvent.getY();
                contextMenu.show(scrollPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            }
        });
    }

    private void createMetadataNode() {
        SceneNode metaDataNode = new SceneNode(300, 100, -1, sceneNodeMainController);
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
                    openNewWindow(sceneNode.getTitle(), sceneNode);
                }
            }

            if(mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                if(mouseEvent.getClickCount() == 2) {
                    removeScene(sceneNode);
                }
            }
        });
    }

    private void openNewWindow(String title, SceneNode sceneNode) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/SceneDetails.fxml"));
            Parent root = fxmlLoader.load();

            SceneDetails sceneDetails = fxmlLoader.getController();
            sceneDetails.passData(story, sceneNode);

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

        story.addNewScene(numberOfScenes-1);

        SceneNode sceneNode = new SceneNode(300, 100, numberOfScenes-1, sceneNodeMainController);
        new Draggable.Nature(sceneNode.getPane());
        sceneNode.setTitle("Scene "+numberOfScenes);

        if (!addSceneContextMeanu) {
            sceneNode.getPane().setLayoutX(10);
        } else {
            sceneNode.getPane().setLayoutX(menuEventX);
            sceneNode.getPane().setLayoutY(menuEventY);
            addSceneContextMeanu = false;
        }


        setClickActionForNode(sceneNode);
        anchorPane.getChildren().add(sceneNode.getPane());
    }

    private void removeScene(SceneNode sceneNode) {
        numberOfScenes--;

        story.removeScene(sceneNode.getSceneId());

        sceneNodeMainController.notifySceneRemoved(sceneNode);

        anchorPane.getChildren().remove(sceneNode.getPane());
    }

    public void addConnectionToStory(AsisConnectionButton outputConnection, AsisConnectionButton inputConnection) {
        //Process where to add the jump to
        if(outputConnection.getConnectionId().contains("dialog_option")) {
            System.out.println(outputConnection.getOptionNumber());
            story.addDialogOptionGoTo(outputConnection.getParentSceneId(), outputConnection.getOptionNumber(), inputConnection.getParentSceneId());
        } else {
            story.addDataToScene(outputConnection.getParentSceneId(), "jumpTo", inputConnection.getParentSceneId());
        }
    }

    public void removeConnectionFromStory(int sceneId) {
        story.removeDataFromScene(sceneId, "jumpTo");
    }

    private void writeJsonToFile(JSONObject jsonObject, String fileName, File saveLocation) {
        try {
            FileWriter fileWriter = new FileWriter(saveLocation.toPath() + File.separator + fileName);
            fileWriter.write(jsonObject.toString());
            fileWriter.flush();
            fileWriter.close();
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
        //TODO Will init some things within the story object and create nodes
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
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(story.getProjectDirectory());
        directoryChooser.setTitle("Save Location");
        File file = directoryChooser.showDialog(null);

        if(file != null) {
            writeJsonToFile(story.getMetadataObject(), "info_en.json", file);
            writeJsonToFile(story.getStoryDataJson(), "joi_text_en.json", file);

            //Copy image to project directory
            for (File file1 : story.getImagesArray()) {
                try {
                    Files.copy(file1.toPath(), file.toPath().resolve(file1.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    errorDialogWindow(e);
                }
            }

            //Copy icon to project directory
            try {
                if(story.getMetadataIcon() != null) {
                    File file1 = story.getMetadataIcon();
                    Files.copy(file1.toPath(), file.toPath().resolve(file1.getName()), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                errorDialogWindow(e);
            }
        }
    }

    public void actionExportToZip() {
        //Export everything to temp folder inside selected directory
        //Compress this folder
        //Delete temp folder
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(story.getProjectDirectory());
        directoryChooser.setTitle("Export Location");
        File file = directoryChooser.showDialog(null);

        if(file != null) {
            File tempFile = new File(file.toPath() + "\\tmp");
            boolean result = tempFile.mkdir();

            if(result) {
                writeJsonToFile(story.getMetadataObject(), "info_en.json", tempFile);
                writeJsonToFile(story.getStoryDataJson(), "joi_text_en.json", tempFile);

                //Copy image to project directory
                for (File file1 : story.getImagesArray()) {
                    try {
                        Files.copy(file1.toPath(), tempFile.toPath().resolve(file1.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        errorDialogWindow(e);
                    }
                }

                //Copy icon to project directory
                try {
                    if(story.getMetadataIcon() != null) {
                        File file1 = story.getMetadataIcon();
                        Files.copy(file1.toPath(), tempFile.toPath().resolve(file1.getName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    errorDialogWindow(e);
                }

                //Compress temp folder
                String zipFile = file.getPath()+"\\joi.zip";
                String srcDir = tempFile.getPath();

                try {
                    // create byte buffer
                    byte[] buffer = new byte[1024];
                    FileOutputStream fos = new FileOutputStream(zipFile);
                    ZipOutputStream zos = new ZipOutputStream(fos);
                    File dir = new File(srcDir);
                    File[] files = dir.listFiles();

                    if (files != null) {
                        for (File value : files) {
                            FileInputStream fis = new FileInputStream(value);
                            zos.putNextEntry(new ZipEntry(value.getName()));
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, length);
                            }
                            zos.closeEntry();

                            // close the InputStream
                            fis.close();
                        }
                    }

                    // close the ZipOutputStream
                    zos.close();
                } catch (IOException e) {
                    errorDialogWindow(e);
                }

                //Delete temp folder
                deleteFolder(tempFile);
            }
        }
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files != null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    if(!f.delete()) {
                        System.out.println("Failed to delete file: "+f.getPath());
                    }
                }
            }
        }

        try {
            Files.delete(folder.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
