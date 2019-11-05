package com.asis.controllers;

import com.asis.LoadProject;
import com.asis.Story;
import com.asis.ui.asis_node.AsisConnectionButton;
import com.asis.ui.asis_node.SceneNode;
import com.asis.ui.asis_node.SceneNodeMainController;
import com.asis.utilities.Alerts;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Draggable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.asis.utilities.AsisUtils.*;

public class Controller {
    private Story story = new Story();
    private SceneNode selectedScene;
    private int numberOfScenes = 0;
    private double menuEventX;
    private double menuEventY;
    private Boolean addSceneContextMenu = false;
    private static Controller instance = null;
    private static boolean newChanges = false;
    private boolean firstScene = false;
    static boolean createNewProject = false;

    private ArrayList<Stage> openStages = new ArrayList<>();
    private ArrayList<SceneNode> sceneNodes = new ArrayList<>();

    private LoadProject loadProject;

    //These should not be used in any code but the newProject method
    static File newProjectFile;
    static String newProjectName;

    private SceneNodeMainController sceneNodeMainController;

    private ContextMenu mainContextMenu =  new ContextMenu();
    private ContextMenu sceneNodeContextMenu =  new ContextMenu();

    @FXML private AnchorPane anchorPane;
    @FXML private ScrollPane scrollPane;
    @FXML private MenuBar mainMenuBar;

    public void initialize() {
        instance = this;
        loadProject = new LoadProject(numberOfScenes, story, sceneNodeMainController);
        sceneNodeMainController = new SceneNodeMainController();
        sceneNodeMainController.setPane(anchorPane);
        sceneNodeMainController.setScrollPane(scrollPane);
        //TODO replace fixed number with one from mainMenuBar + height of toolbar
        sceneNodeMainController.setMenuBarOffset(70);

        setupMainContextMenu();
        setupSceneNodeContextMenu();

        firstScene = true;
        addScene();
    }

    public static Controller getInstance() {
        return instance;
    }

    public void setNewChanges(Boolean bool) {
        newChanges = bool;
    }

    public static boolean getNewChanges() {
        return newChanges;
    }

    private void setupSceneNodeContextMenu() {
        //Create items and add them to there menu
        MenuItem editSceneItem = new MenuItem("Edit Scene");
        MenuItem editNameItem = new MenuItem("Change Name");
        MenuItem goodEndItem = new MenuItem("Set as Good End");
        MenuItem badEndItem = new MenuItem("Set as Bad End");
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem deleteNodeItem = new MenuItem("Delete");
        sceneNodeContextMenu.getItems().addAll(editSceneItem, editNameItem, goodEndItem, badEndItem, separatorMenuItem, deleteNodeItem);

        //Handle menu actions
        editSceneItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                openNewWindow(selectedScene);
            }
        });

        editNameItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                String title = new Alerts().addNewSceneDialog(selectedScene.getTitle());
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

        goodEndItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                if(selectedScene.goodEndProperty().getValue()) {
                    selectedScene.setGoodEnd(false);
                } else {
                    selectedScene.setGoodEnd(true);
                }
            }
        });

        badEndItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                if(selectedScene.badEndProperty().getValue()) {
                    selectedScene.setBadEnd(false);
                } else {
                    selectedScene.setBadEnd(true);
                }
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
            for (Node n: anchorPane.getChildren()) {

                if(n.getUserData().equals("sceneNode")) {
                    Bounds boundsInScene = n.localToScene(n.getBoundsInLocal());

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

    public void actionOpenMetadata() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/meta_data_form.fxml"));
            Parent root = fxmlLoader.load();

            MetaDataForm metaDataForm = fxmlLoader.getController();
            metaDataForm.inflateStoryObject(story);

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setTitle("Project Details");
            stage.setScene(new Scene(root, 400, 720));
            stage.show();

            stage.setOnCloseRequest(event -> {
                if (metaDataForm.hasChanged()) {
                    if (!new Alerts().confirmationDialog("Warning", "Are you sure you don't want to save?")) {
                        event.consume();
                    }
                }
            });
        } catch (IOException e) {
            errorDialogWindow(e);
        }
    }

    private void setClickActionForNode(SceneNode sceneNode) {
        sceneNode.getPane().setOnContextMenuRequested(contextMenuEvent -> {
            selectedScene = sceneNode;

            //Change behaviour for first scene
            if(sceneNode.getSceneId() == 0) {
                //Is the first scene
                sceneNodeContextMenu.getItems().get(sceneNodeContextMenu.getItems().size()-1).setDisable(true);
                sceneNodeContextMenu.getItems().get(2).setDisable(true);
                sceneNodeContextMenu.getItems().get(3).setDisable(true);
            } else {
                sceneNodeContextMenu.getItems().get(sceneNodeContextMenu.getItems().size()-1).setDisable(false);

                //Change name of ending buttons
                if(sceneNode.badEndProperty().getValue()) {
                    sceneNodeContextMenu.getItems().get(3).setText("Remove Ending Tag");
                    sceneNodeContextMenu.getItems().get(2).setDisable(true);
                } else {
                    sceneNodeContextMenu.getItems().get(3).setText("Set as Bad End");
                    sceneNodeContextMenu.getItems().get(2).setDisable(false);
                }

                if(sceneNode.goodEndProperty().getValue()) {
                    sceneNodeContextMenu.getItems().get(2).setText("Remove Ending Tag");
                    sceneNodeContextMenu.getItems().get(3).setDisable(true);
                } else {
                    sceneNodeContextMenu.getItems().get(2).setText("Set as Good End");
                    sceneNodeContextMenu.getItems().get(3).setDisable(false);
                }
            }
        });

        sceneNode.setOnMousePressed(mouseEvent -> sceneNodeContextMenu.hide());

        sceneNode.getPane().setOnMouseClicked(mouseEvent -> {
            //User double clicked
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if(mouseEvent.getClickCount() == 2) {
                    openNewWindow(sceneNode);
                }
            }
        });
    }

    private void openNewWindow(SceneNode sceneNode) {
        //Check if stage already exists
        for(Stage stage: getOpenStages()) {
            if((int) stage.getUserData() == sceneNode.getSceneId()) {
                stage.requestFocus();
                return;
            }
        }

        //Open new window
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/SceneDetails.fxml"));
            Parent root = fxmlLoader.load();

            SceneDetails sceneDetails = fxmlLoader.getController();
            sceneDetails.passData(story, sceneNode);

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setTitle(sceneNode.getTitle());
            stage.setUserData(sceneNode.getSceneId());
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();

            //Move stage to proper window
            Screen primaryStageScreen = getScreenForStage((Stage) anchorPane.getScene().getWindow());
            if(primaryStageScreen != null) {
                Rectangle2D bounds = primaryStageScreen.getVisualBounds();
                stage.setX(bounds.getMinX() + 100);
                stage.setY(bounds.getMinY() + 100);
                stage.centerOnScreen();
            }

            stage.setOnCloseRequest(windowEvent -> getOpenStages().remove(stage));
            getOpenStages().add(stage);
        } catch (IOException e) {
            errorDialogWindow(e);
        }
    }

    private static Screen getScreenForStage(Stage stage) {
        //TODO This works but not when fullscreened
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            Point2D point = new Point2D(stage.getScene().getWindow().getX(), stage.getScene().getWindow().getY());
            if(bounds.contains(point)) {
                return screen;
            }
        }

        return null;
    }

    public void actionExit() {
        //Check if dialog is needed
        JSONObject storyData = Story.getInstance().getStoryDataJson();
        JSONObject metadataObject = Story.getInstance().getMetadataObject();

        if(storyData == null && metadataObject == null) {
            quiteProgram();
            return;
        }

        if(getNewChanges()) {
            int choice = new Alerts().unsavedChangesDialog("Warning", "You have unsaved work, are you sure you want to quit?");
            switch (choice) {
                case 0:
                    return;

                case 1:
                    quiteProgram();
                    break;

                case 2:
                    actionSaveProject();
                    quiteProgram();
                    break;
            }
        } else {
            quiteProgram();
        }
    }

    public void quiteProgram() {
        Platform.exit();
        System.exit(0);
    }

    private void addScene() {
        String title;

        if(numberOfScenes != 0) {
            title = new Alerts().addNewSceneDialog("Scene " + (numberOfScenes+1));
            if(title == null) return;
        } else {
            title = "Scene " + (numberOfScenes+1);
        }

        addScene(10, 0, title, numberOfScenes, false);
    }

    public SceneNode addScene(double xPosition, double yPosition, String title, int sceneId, boolean suppressJSONUpdating) {
        numberOfScenes++;

        //Add new scene to json if not suppressed
        if(!suppressJSONUpdating) story.addNewScene(sceneId, title);

        SceneNode sceneNode = new SceneNode(300, 100, sceneId, sceneNodeMainController);
        new Draggable.Nature(sceneNode.getPane());

        sceneNode.setTitle(title);

        //Set and save position
        if (!addSceneContextMenu) {
            //TODO issue 5 make new scenes via button adjacent
            //AsisUtils.getAllNodes(anchorPane)
            /*for (SceneNode listItem : getSceneNodes()) {
                if(listItem.getPane().getBoundsInParent().intersects(sceneNode.getPane().getBoundsInParent())) {
                    System.out.println("There is a scene node here!");
                }
            }*/


            sceneNode.getPane().setLayoutX(xPosition);
            sceneNode.getPane().setLayoutY(yPosition);
            if(!suppressJSONUpdating) {
                story.addDataToScene(sceneNode.getSceneId(), "layoutXPosition", xPosition);
                story.addDataToScene(sceneNode.getSceneId(), "layoutYPosition", yPosition);
            }
        } else {
            Bounds bounds = scrollPane.getViewportBounds();
            double lowestXPixelShown = -1 * bounds.getMinX();
            double lowestYPixelShown = -1 * bounds.getMinY();

            sceneNode.getPane().setLayoutX(lowestXPixelShown + menuEventX);
            sceneNode.getPane().setLayoutY(lowestYPixelShown + menuEventY);
            addSceneContextMenu = false;

            if(!suppressJSONUpdating) {
                story.addDataToScene(sceneNode.getSceneId(), "layoutXPosition", lowestXPixelShown + menuEventX);
                story.addDataToScene(sceneNode.getSceneId(), "layoutYPosition", lowestYPixelShown + menuEventY);
            }
        }

        setClickActionForNode(sceneNode);
        getAnchorPane().getChildren().add(sceneNode.getPane());

        if (!firstScene) {
            setNewChanges(true);
        } else {
            firstScene = false;
            //Override any changes caused by story object
            newChanges = false;
        }

        getSceneNodes().add(sceneNode);
        return sceneNode;
    }

    private void removeScene(SceneNode sceneNode) {
        story.removeScene(sceneNode.getSceneId());

        sceneNodeMainController.notifySceneRemoved(sceneNode);

        anchorPane.getChildren().remove(sceneNode.getPane());
        setNewChanges(true);
    }

    public void actionNewProject() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/dialog_new_project.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setTitle("New Project");
            stage.setScene(new Scene(root, 600, 400));
            stage.showAndWait();
        } catch (IOException e) {
            errorDialogWindow(e);
        }

        if(createNewProject) {
            numberOfScenes = 0;
            anchorPane.getChildren().clear();
            story = new Story();
            addScene();
            newChanges = false;
            createNewProject = false;

            String folderPath = newProjectFile.getPath()+"\\"+newProjectName;
            File projectDirectory = new File(folderPath);
            boolean result = projectDirectory.mkdir();

            if(result) {
                System.out.println("Created folder");
            } else {
                System.out.println("Folder already exists");
            }

            //Set project directory
            story.setProjectDirectory(projectDirectory);

            //Add project name as the Title in metadata
            story.addTitleToMetadataObject(newProjectName);

            //Reset newProject variables
            newProjectName = null;
            newProjectFile = null;
        }
    }

    public boolean actionLoadProject() {
        return loadProject.actionLoadProject();
    }

    public SceneNode getSceneNodeWithId(ArrayList<SceneNode> sceneList, int sceneId) {
        for (SceneNode scene : sceneList) {
            if(scene.getSceneId() == sceneId) {
                return scene;
            }
        }

        return null;
    }

    public void actionSaveProject() {
        saveProject(Story.getInstance().getProjectDirectory());
    }

    public void actionSaveProjectAs() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(story.getProjectDirectory());
        directoryChooser.setTitle("Save Location");
        File file = directoryChooser.showDialog(null);

        saveProject(file);
    }

    private void saveProject(File file) {
        if(file != null) {
            if(story.getMetadataObject() == null || story.getMetadataObject().isEmpty()) {
                //Init metadata
                JSONObject innerMetadataObject = new JSONObject();
                //Single lined info
                innerMetadataObject.put("name", "");
                innerMetadataObject.put("preparations", "");
                innerMetadataObject.put("displayedFetishes", "");
                innerMetadataObject.put("joiId", "");

                innerMetadataObject.put("fetish0", "");
                innerMetadataObject.put("toy0", "");
                innerMetadataObject.put("character0", "");

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(innerMetadataObject);

                JSONObject metadataObject = new JSONObject();
                metadataObject.put("JOI METADATA", jsonArray);

                story.setMetadataObject(metadataObject);
                URL url = this.getClass().getResource("/resources/images/icon_dev.png");
                story.addMetadataIcon(new File(Objects.requireNonNull(url).getPath()));
            }

            AsisUtils.writeJsonToFile(story.getMetadataObject(), "info_en.json", file);
            AsisUtils.writeJsonToFile(story.getStoryDataJson(), "joi_text_en.json", file);

            //Copy image to project directory
            for (File file1 : story.getImagesArray()) {
                try {
                    Files.copy(file1.toPath(), file.toPath().resolve(file1.getName()), StandardCopyOption.REPLACE_EXISTING);
                    newChanges = false;
                } catch (IOException e) {
                    errorDialogWindow(e);
                }
            }

            //Copy icon to project directory
            try {
                if(story.getMetadataIcon() != null) {
                    File file1 = story.getMetadataIcon();
                    Files.copy(file1.toPath(), file.toPath().resolve(file1.getName()), StandardCopyOption.REPLACE_EXISTING);
                    renameFile(new File(file.toPath()+"\\"+file1.getName()), "joi_icon.png");
                    newChanges = false;
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
                AsisUtils.writeJsonToFile(story.getMetadataObject(), "info_en.json", tempFile);
                AsisUtils.writeJsonToFile(story.getStoryDataJson(), "joi_text_en.json", tempFile);

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
                        renameFile(new File(tempFile.toPath()+"\\"+file1.getName()), "joi_icon.png");
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

    public void actionGettingStarted() {
        String message = AsisUtils.getStringFromFile("/resources/text_files/getting_started.txt");
        Alerts.messageDialog("Getting Started", message, 720, 720);
    }

    public void actionProjectDetailsHelp() {
        String message = AsisUtils.getStringFromFile("/resources/text_files/project_details.txt");
        Alerts.messageDialog("Getting Started", message, 720, 720);
    }

    public void actionAbout() {
        String message = AsisUtils.getStringFromFile("/resources/text_files/about.txt");
        Alerts.messageDialog("About", message, 500, 250);
    }

    public void actionSceneEditor() {
        String message = AsisUtils.getStringFromFile("/resources/text_files/scene_editor.txt");
        Alerts.messageDialog("Scene Editor", message, 720, 720);
    }

    public void actionAddSceneButton() {
        addScene();
    }

    public ArrayList<Stage> getOpenStages() {
        return openStages;
    }
    public void setOpenStages(ArrayList<Stage> openStages) {
        this.openStages = openStages;
    }

    private void setAnchorPane(AnchorPane anchorPane) {
        this.anchorPane = anchorPane;
    }
    private AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public ArrayList<SceneNode> getSceneNodes() {
        return sceneNodes;
    }
    public void setSceneNodes(ArrayList<SceneNode> sceneNodes) {
        this.sceneNodes = sceneNodes;
    }
}
