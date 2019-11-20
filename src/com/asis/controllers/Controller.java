package com.asis.controllers;

import com.asis.joi.JOIPackage;
import com.asis.joi.MetaData;
import com.asis.joi.components.GotoScene;
import com.asis.joi.components.dialog.DialogOption;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Controller {
    private SceneNode selectedScene;
    private double menuEventX;
    private double menuEventY;
    private Boolean addSceneContextMenu = false;
    private static Controller instance = null;

    private JOIPackage joiPackage = new JOIPackage();
    private ArrayList<Stage> openStages = new ArrayList<>();
    private ArrayList<SceneNode> sceneNodes = new ArrayList<>();

    private SceneNodeMainController sceneNodeMainController;

    private ContextMenu mainContextMenu =  new ContextMenu();
    private ContextMenu sceneNodeContextMenu =  new ContextMenu();

    @FXML private AnchorPane anchorPane;
    @FXML private ScrollPane scrollPane;
    @FXML private MenuBar mainMenuBar;

    public void initialize() {
        instance = this;
        sceneNodeMainController = new SceneNodeMainController(getJoiPackage());
        sceneNodeMainController.setPane(anchorPane);
        sceneNodeMainController.setScrollPane(scrollPane);
        //TODO replace fixed number with one from mainMenuBar + height of toolbar
        sceneNodeMainController.setMenuBarOffset(70);

        setupMainContextMenu();
        setupSceneNodeContextMenu();
        addScene(true);
    }

    public static Controller getInstance() {
        return instance;
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
                openSceneDetails(selectedScene);
            }
        });

        editNameItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                String title = new Alerts().addNewSceneDialog(selectedScene.getTitle());
                if(title == null) {
                    return;
                }

                getJoiPackage().getJoi().getScene(selectedScene.getSceneId()).setSceneTitle(title);
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
                if(selectedScene.isGoodEnd()) {
                    selectedScene.setIsGoodEnd(false);
                } else {
                    selectedScene.setIsGoodEnd(true);
                }
            }
        });

        badEndItem.setOnAction(actionEvent -> {
            if(selectedScene != null) {
                if(selectedScene.isBadEnd()) {
                    selectedScene.setIsBadEnd(false);
                } else {
                    selectedScene.setIsBadEnd(true);
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
            addScene(false);
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
        if (requestStageIfOpen("metaData")) return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/meta_data_form.fxml"));
            Parent root = fxmlLoader.load();

            MetaDataForm metaDataForm = fxmlLoader.getController();
            metaDataForm.inflateJOIPackageObject(getJoiPackage());

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setTitle("Project Details");
            stage.setUserData("metaData");
            stage.setScene(new Scene(root, 400, 720));
            stage.show();

            stage.setOnCloseRequest(event -> {
                if(metaDataForm.changesHaveOccurred()) {
                    if (!new Alerts().confirmationDialog("Warning", "You have unsaved data, are you sure you want to close?")) {
                        event.consume();
                        return;
                    }
                }
                getOpenStages().remove(stage);
                stage.close();
            });
            getOpenStages().add(stage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
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
                if(sceneNode.isBadEndProperty().getValue()) {
                    sceneNodeContextMenu.getItems().get(3).setText("Remove Ending Tag");
                    sceneNodeContextMenu.getItems().get(2).setDisable(true);
                } else {
                    sceneNodeContextMenu.getItems().get(3).setText("Set as Bad End");
                    sceneNodeContextMenu.getItems().get(2).setDisable(false);
                }

                if(sceneNode.isGoodEndProperty().getValue()) {
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
                    openSceneDetails(sceneNode);
                }
            }
        });
    }

    private void openSceneDetails(SceneNode sceneNode) {
        if (requestStageIfOpen(sceneNode.getSceneId())) return;

        //Open new window
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/SceneDetails.fxml"));
            Parent root = fxmlLoader.load();

            SceneDetails sceneDetails = fxmlLoader.getController();
            sceneDetails.initialize(getJoiPackage().getJoi().getScene(sceneNode.getSceneId()));

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setTitle(sceneNode.getTitle());
            stage.setUserData(sceneNode.getSceneId());
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();

            stage.setOnCloseRequest(windowEvent -> getOpenStages().remove(stage));
            getOpenStages().add(stage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    private boolean requestStageIfOpen(Object userData) {
        //Check if stage already exists
        for(Stage stage: getOpenStages()) {
            if(stage.getUserData().equals(userData)) {
                stage.setIconified(false);
                stage.requestFocus();
                return true;
            }
        }
        return false;
    }

    public boolean changesHaveOccurred() {
        try {
            final JOIPackage originalPackage = new JOIPackage();
            originalPackage.setPackageLanguageCode(getJoiPackage().getPackageLanguageCode());
            originalPackage.importPackageFromDirectory(getJoiPackage().getPackageDirectory());

            //The icons are set to null, because when a joi is saved it exports and icon
            //This icon will not be present in the joi stored in memory causing the results
            //to be inaccurate.
            MetaData memoryData = new MetaData(getJoiPackage().getMetaData());
            memoryData.setJoiIcon(null);
            MetaData fileData = new MetaData(originalPackage.getMetaData());
            fileData.setJoiIcon(null);

            return !getJoiPackage().getJoi().equals(originalPackage.getJoi()) || !memoryData.equals(fileData);
        }catch (RuntimeException e) {
            return true;
        }
    }

    public void actionExit() {
        //Check if dialog is needed
        if(changesHaveOccurred()) {
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

    private void addScene(final boolean isFirstScene) {
        final int sceneId = getJoiPackage().getJoi().getSceneIdCounter()+1;
        final String defaultTitle = "Scene " + sceneId;
        String title;

        if(isFirstScene) {
            title = defaultTitle;
        } else {
            title = new Alerts().addNewSceneDialog(defaultTitle);
            if(title == null) return;
        }

        addScene(10, 0, title, sceneId-1, false);
    }

    private SceneNode addScene(double xPosition, double yPosition, String title, int sceneId, boolean suppressJSONUpdating) {
        //Add new scene to json if not suppressed
        if(!suppressJSONUpdating) {
            getJoiPackage().getJoi().addNewScene(sceneId);
            getJoiPackage().getJoi().getScene(sceneId).setSceneTitle(title);
        }

        SceneNode sceneNode = new SceneNode(300, 100, sceneId, sceneNodeMainController, getJoiPackage().getJoi().getScene(sceneId));
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
                getJoiPackage().getJoi().getScene(sceneId).setLayoutXPosition(xPosition);
                getJoiPackage().getJoi().getScene(sceneId).setLayoutYPosition(yPosition);
            }
        } else {
            Bounds bounds = scrollPane.getViewportBounds();
            double lowestXPixelShown = -1 * bounds.getMinX();
            double lowestYPixelShown = -1 * bounds.getMinY();

            sceneNode.getPane().setLayoutX(lowestXPixelShown + menuEventX);
            sceneNode.getPane().setLayoutY(lowestYPixelShown + menuEventY);
            addSceneContextMenu = false;

            if(!suppressJSONUpdating) {
                getJoiPackage().getJoi().getScene(sceneId).setLayoutXPosition(lowestXPixelShown + menuEventX);
                getJoiPackage().getJoi().getScene(sceneId).setLayoutYPosition(lowestYPixelShown + menuEventY);
            }
        }

        setClickActionForNode(sceneNode);
        getAnchorPane().getChildren().add(sceneNode.getPane());

        getSceneNodes().add(sceneNode);
        return sceneNode;
    }

    private void removeScene(SceneNode sceneNode) {
        getJoiPackage().getJoi().removeScene(sceneNode.getSceneId());

        sceneNodeMainController.notifySceneRemoved(sceneNode);

        anchorPane.getChildren().remove(sceneNode.getPane());
    }

    public void actionNewProject() {
        Alerts.newProjectWindow(false);
    }

    void processNewProject(File newProjectFile, String newProjectName, String defaultProjectLanguageCode) {
        resetJoiPackage(new JOIPackage());
        addScene(true);

        File projectDirectory = new File(newProjectFile.getPath()+"/"+newProjectName);
        //noinspection ResultOfMethodCallIgnored
        projectDirectory.mkdir();

        //Set project directory
        getJoiPackage().setPackageDirectory(projectDirectory);

        //Add project name as the Title in metadata
        getJoiPackage().getMetaData().setName(newProjectName);

        //Set default language code of package
        getJoiPackage().setPackageLanguageCode(defaultProjectLanguageCode);
    }

    private void resetJoiPackage(JOIPackage joiPackage) {
        anchorPane.getChildren().clear();
        getSceneNodes().clear();
        setJoiPackage(joiPackage);
        sceneNodeMainController.setJoiPackage(joiPackage);
        sceneNodeMainController.setLineList(new ArrayList<>());
        for(Stage stage: getOpenStages()) {
            stage.close();
        }
        getOpenStages().clear();
    }

    public boolean actionLoadProject() {
        if (changesHaveOccurred()) {
            int choice = new Alerts().unsavedChangesDialog("Load Project", "You have unsaved work, are you sure you want to continue?");
            switch (choice) {
                case 0:
                    return false;

                case 2:
                    actionSaveProject();
                    break;
            }
        }

        return processLoadProject();
    }

    public boolean processLoadProject() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(getJoiPackage().getPackageDirectory());
        File file = directoryChooser.showDialog(null);

        if(file != null) {
            JOIPackage newJoiPackage = new JOIPackage();

            //Set project directory to current
            newJoiPackage.setPackageDirectory(file);

            //Load joi
            try {
                if (newJoiPackage.importPackageFromDirectory(file)) {
                    //JOI folder was imported successfully

                    //Reset old variables
                    resetJoiPackage(newJoiPackage);

                    //Create scene nodes
                    for (com.asis.joi.components.Scene scene : getJoiPackage().getJoi().getSceneArrayList()) {
                        addScene(scene.getLayoutXPosition(), scene.getLayoutYPosition(), scene.getSceneTitle(), scene.getSceneId(), true);
                    }

                    //Create connections
                    for (com.asis.joi.components.Scene scene : getJoiPackage().getJoi().getSceneArrayList()) {
                        final AsisConnectionButton output = getSceneNodeWithId(getSceneNodes(), scene.getSceneId()).getOutputButtons().get(0);
                        createConnections(scene.getGotoScene(), output);

                        createConnectionsForDialogOutputs(scene);
                    }
                } else {
                    return false;
                }

                //Loading completed successfully
                return true;
            } catch (RuntimeException e) {
                Alerts.messageDialog("LOADING FAILED", "The editor was unable to load this joi for the following reason:\n"+e.getMessage(), 600, 200);
                return false;
            }
        }

        //Loading failed do to null file
        return false;
    }

    private void createConnectionsForDialogOutputs(com.asis.joi.components.Scene scene) {
        if(scene.getDialog() != null && !scene.getDialog().getOptionArrayList().isEmpty()) {
            for(DialogOption dialogOption: scene.getDialog().getOptionArrayList()) {
                AsisConnectionButton output = getSceneNodeWithId(sceneNodes, scene.getSceneId()).createNewOutputConnectionPoint("Option "+dialogOption.getOptionNumber(), "dialog_option_" + (dialogOption.getOptionNumber() + 1));
                output.setOptionNumber(dialogOption.getOptionNumber());

                createConnections(dialogOption.getGotoScene(), output);
            }
        }
    }

    private void createConnections(GotoScene gotoScene, AsisConnectionButton output) {
        final boolean gotoHasSingleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() == 1;
        final boolean gotoHasMultipleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() > 1;

        //Check for scene normal connections
        if(gotoHasSingleOutput) {
            AsisConnectionButton input = getSceneNodeWithId(getSceneNodes(), gotoScene.getGotoSceneArrayList().get(0)).getInputConnection();
            sceneNodeMainController.createConnection(output, input);
        }

        //Check for scene range connections
        if (gotoHasMultipleOutput) {
            for (int i = 0; i < gotoScene.getGotoSceneArrayList().size(); i++) {
                AsisConnectionButton input = getSceneNodeWithId(getSceneNodes(), gotoScene.getGotoSceneArrayList().get(i)).getInputConnection();
                sceneNodeMainController.createConnection(output, input);
            }
        }
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
        saveProject(getJoiPackage().getPackageDirectory());
    }

    public void actionSaveProjectAs() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(getJoiPackage().getPackageDirectory());
        directoryChooser.setTitle("Save Location");
        File file = directoryChooser.showDialog(null);

        saveProject(file);
    }

    private void saveProject(File file) {
        if(file != null) {
            getJoiPackage().exportPackageAsFiles(file);
        }
    }

    public void actionExportToZip() {
        //Export project as zip
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Zip");
        fileChooser.setInitialDirectory(getJoiPackage().getPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("zip", "*.zip"));
        File dest = fileChooser.showSaveDialog(null);

        if(dest != null) {
            getJoiPackage().exportPackageAsZip(dest);
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
        addScene(false);
    }

    //Getters and setters
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

    public JOIPackage getJoiPackage() {
        return joiPackage;
    }
    public void setJoiPackage(JOIPackage joiPackage) {
        this.joiPackage = joiPackage;
    }
}
