package asis;

import asis.custom_objects.Draggable;
import asis.custom_objects.asis_node.AsisConnectionButton;
import asis.custom_objects.asis_node.SceneNode;
import asis.custom_objects.asis_node.SceneNodeMainController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static asis.custom_objects.AsisUtils.errorDialogWindow;
import static asis.custom_objects.AsisUtils.getAllNodes;

public class Controller {
    private Story story = new Story();
    private SceneNode selectedScene;
    private int numberOfScenes = 0;
    private double menuEventX;
    private double menuEventY;
    private Boolean addSceneContextMenu = false;
    private static Controller instance = null;
    private Stage primaryStage;

    private SceneNodeMainController sceneNodeMainController;

    private ContextMenu mainContextMenu =  new ContextMenu();
    private ContextMenu sceneNodeContextMenu =  new ContextMenu();

    @FXML private AnchorPane anchorPane;
    @FXML private ScrollPane scrollPane;
    @FXML private MenuBar mainMenuBar;

    public void initialize() {

    }

    void inflater(Stage primaryStage) {
        this.primaryStage = primaryStage;
        instance = this;
        sceneNodeMainController = new SceneNodeMainController(this);
        sceneNodeMainController.setPane(anchorPane);
        sceneNodeMainController.setScrollPane(scrollPane);
        //TODO replace fixed number with one from mainMenuBar + height of toolbar
        sceneNodeMainController.setMenuBarOffset(70);

        setupMainContextMenu();
        setupSceneNodeContextMenu();

        addScene();
    }

    static Controller getInstance() {
        return instance;
    }

    private void setupSceneNodeContextMenu() {
        //Create items and add them to there menu
        MenuItem editSceneItem = new MenuItem("Edit Scene");
        MenuItem editNameItem = new MenuItem("Change Name");
        MenuItem deleteNodeItem = new MenuItem("Delete");
        sceneNodeContextMenu.getItems().addAll(editSceneItem, editNameItem, deleteNodeItem);

        //Handle menu actions
        editSceneItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                openNewWindow(selectedScene.getTitle(), selectedScene);
            }
        });

        editNameItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                String title = new Alerts().addNewSceneDialog(this.getClass(), selectedScene.getTitle());
                if(title == null) {
                    return;
                }

                story.addDataToScene(selectedScene.getSceneId(), "sceneTitle", title);
                selectedScene.setTitle(title);
            }
        });

        deleteNodeItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                removeScene(selectedScene);
            }
        });
    }

    private void setupMainContextMenu() {
        //Create items and add them to there menu
        MenuItem newSceneItem = new MenuItem("New Scene");
        mainContextMenu.getItems().add(newSceneItem);


        //Handle menu actions
        newSceneItem.setOnAction(event -> {
            addSceneContextMenu = true;
            addScene();
        });

        scrollPane.setContextMenu(mainContextMenu);

        //Used to get the coordinates for spawning the scene node and for hiding/showing the proper menu
        scrollPane.setOnContextMenuRequested(contextMenuEvent -> {
            ArrayList<Node> tmp = getAllNodes(anchorPane);
            for (Node n: tmp) {
                Bounds boundsInScene = n.localToScene(n.getBoundsInLocal());

                Optional<Node> node = findNode(tmp, contextMenuEvent.getSceneX(), contextMenuEvent.getSceneY());
                if(node.isPresent()) {
                    if (contextMenuEvent.getSceneX() >= boundsInScene.getMinX() && contextMenuEvent.getSceneX() <= boundsInScene.getMaxX() && contextMenuEvent.getSceneY() >= boundsInScene.getMinY() && contextMenuEvent.getSceneY() <= boundsInScene.getMaxY()) {
                        mainContextMenu.hide();
                        sceneNodeContextMenu.show(anchorPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                    }
                }
            }

            menuEventX = contextMenuEvent.getX();
            menuEventY = contextMenuEvent.getY();
        });

        scrollPane.setOnMouseClicked(mouseEvent -> sceneNodeContextMenu.hide() );
    }

    private Optional<Node> findNode(ArrayList<Node> list, double x, double y) {
        for (Node n : list) {
            Bounds boundsInScene = n.localToScene(n.getBoundsInLocal());

            if(x >= boundsInScene.getMinX() && x <= boundsInScene.getMaxX() && y >= boundsInScene.getMinY() && y <= boundsInScene.getMaxY()) {
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    public void actionOpenMetadata() {
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

    private void setClickActionForNode(SceneNode sceneNode) {
        sceneNode.getPane().setOnContextMenuRequested(contextMenuEvent -> {
            selectedScene = sceneNode;
            if(sceneNode.getSceneId() == 0) {
                //Is the first scene
                sceneNodeContextMenu.getItems().get(2).setDisable(true);
            } else {
                sceneNodeContextMenu.getItems().get(2).setDisable(false);
            }
        });

        sceneNode.getPane().setOnMouseClicked(mouseEvent -> {
            sceneNodeContextMenu.hide();

            //User double clicked
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if(mouseEvent.getClickCount() == 2){
                    openNewWindow(sceneNode.getTitle(), sceneNode);
                }
            }

            /*if(mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                if(mouseEvent.getClickCount() == 2) {
                    removeScene(sceneNode);
                }
            }*/
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
        String title;
        if(numberOfScenes != 0) {
            title = new Alerts().addNewSceneDialog(this.getClass(), "Scene " + (numberOfScenes+1));
            if(title == null) {
                return;
            }
        } else {
            title = "Scene " + (numberOfScenes+1);
        }

        numberOfScenes++;

        story.addNewScene(numberOfScenes-1, title);

        SceneNode sceneNode = new SceneNode(300, 100, numberOfScenes-1, sceneNodeMainController);
        new Draggable.Nature(sceneNode.getPane());

        sceneNode.setTitle(title);

        if (!addSceneContextMenu) {
            sceneNode.getPane().setLayoutX(10);
        } else {
            Bounds bounds = scrollPane.getViewportBounds();
            double lowestXPixelShown = -1 * bounds.getMinX();
            double lowestYPixelShown = -1 * bounds.getMinY();

            sceneNode.getPane().setLayoutX(lowestXPixelShown + menuEventX);
            sceneNode.getPane().setLayoutY(lowestYPixelShown + menuEventY);
            addSceneContextMenu = false;
        }


        setClickActionForNode(sceneNode);
        anchorPane.getChildren().add(sceneNode.getPane());
    }

    private void removeScene(SceneNode sceneNode) {
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
            story.addDataToScene(outputConnection.getParentSceneId(), "gotoScene", inputConnection.getParentSceneId());
        }
    }

    public void removeConnectionFromStory(int sceneId) {
        story.removeDataFromScene(sceneId, "gotoScene");
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

    public void actionNewProject() {
        //TODO Will init some things within the story object and create nodes
        Alerts.warningDialog("Load Project", "Do you want to save changes first?", "", primaryStage, null);
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Zip");
        fileChooser.setInitialDirectory(story.getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("zip", "*.zip"));
        File dest = fileChooser.showSaveDialog(null);

        if(dest != null) {
            File tempFile = new File(dest.getParent() + "\\tmp");
            if(tempFile.isDirectory()) {
                deleteFolder(tempFile);
            }

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
                String zipFile = dest.getPath();
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
                            fis.close();
                        }
                    }
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
