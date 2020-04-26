package com.asis.controllers;

import com.asis.controllers.dialogs.*;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.entites.GotoScene;
import com.asis.joi.model.entites.dialog.DialogOption;
import com.asis.ui.asis_node.AsisConnectionButton;
import com.asis.ui.asis_node.SceneNode;
import com.asis.ui.asis_node.SceneNodeMainController;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Draggable;
import com.asis.utilities.StageManager;
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

import static com.asis.controllers.dialogs.DialogUnsavedChanges.*;

public class Controller {
    private SceneNode selectedScene;
    private double menuEventX;
    private double menuEventY;
    private Boolean addSceneContextMenu = false;
    private static Controller instance = null;

    private JOIPackage joiPackage;
    private final ArrayList<SceneNode> sceneNodes = new ArrayList<>();

    private SceneNodeMainController sceneNodeMainController;

    private final ContextMenu mainContextMenu = new ContextMenu();
    private final ContextMenu sceneNodeContextMenu = new ContextMenu();

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    public MenuBar mainMenuBar;
    @FXML
    public ToolBar toolBar;

    public void initialize() {
        instance = this;

        setupMainContextMenu();
        setupSceneNodeContextMenu();
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
            if (selectedScene != null) {
                openSceneDetails(selectedScene);
            }
        });

        editNameItem.setOnAction(actionEvent -> {
            if (selectedScene != null) {
                String title = DialogSceneTitle.addNewSceneDialog(selectedScene.getTitle());

                getJoiPackage().getJoi().getScene(selectedScene.getSceneId()).setSceneTitle(title);
                selectedScene.setTitle(title);
            }
        });

        deleteNodeItem.setOnAction(actionEvent -> {
            if (selectedScene != null) {
                removeScene(selectedScene);
            }
        });

        goodEndItem.setOnAction(actionEvent -> {
            if (selectedScene != null) {
                selectedScene.setIsGoodEnd(!selectedScene.isGoodEnd());
            }
        });

        badEndItem.setOnAction(actionEvent -> {
            if (selectedScene != null) {
                selectedScene.setIsBadEnd(!selectedScene.isBadEnd());
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
            for (Node n : anchorPane.getChildren()) {
                if (n.getUserData() == null) continue;

                if (n.getUserData().equals("sceneNode")) {
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

        scrollPane.setOnMouseClicked(mouseEvent -> sceneNodeContextMenu.hide());
    }

    public void actionOpenMetadata() {
        if (StageManager.getInstance().requestStageFocus("metaData")) return;

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
            StageManager.getInstance().openStage(stage);
            stage.setOnCloseRequest(event -> {
                if (metaDataForm.changesHaveOccurred()) {
                    if (!DialogConfirmation.show("Warning", "You have unsaved data, are you sure you want to close?")) {
                        event.consume();
                        return;
                    }
                }
                StageManager.getInstance().closeStage(stage);
            });

        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public void actionOpenTranslationEditor() {
        if (StageManager.getInstance().requestStageFocus("translationEditor")) return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/translation_editor.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setTitle("Translation Editor");
            stage.setUserData("translationEditor");
            stage.setScene(new Scene(root, 1280, 720));
            StageManager.getInstance().openStage(stage);
            stage.setOnCloseRequest(event -> StageManager.getInstance().closeStage(stage));

        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    private void setClickActionForNode(SceneNode sceneNode) {
        sceneNode.setOnContextMenuRequested(contextMenuEvent -> {
            selectedScene = sceneNode;

            //Change behaviour for first scene
            if (sceneNode.getSceneId() == 0) {
                //Is the first scene
                sceneNodeContextMenu.getItems().get(sceneNodeContextMenu.getItems().size() - 1).setDisable(true);
                sceneNodeContextMenu.getItems().get(2).setDisable(true);
                sceneNodeContextMenu.getItems().get(3).setDisable(true);
            } else {
                sceneNodeContextMenu.getItems().get(sceneNodeContextMenu.getItems().size() - 1).setDisable(false);

                //Change name of ending buttons
                if (sceneNode.isBadEndProperty().getValue()) {
                    sceneNodeContextMenu.getItems().get(3).setText("Remove Ending Tag");
                    sceneNodeContextMenu.getItems().get(2).setDisable(true);
                } else {
                    sceneNodeContextMenu.getItems().get(3).setText("Set as Bad End");
                    sceneNodeContextMenu.getItems().get(2).setDisable(false);
                }

                if (sceneNode.isGoodEndProperty().getValue()) {
                    sceneNodeContextMenu.getItems().get(2).setText("Remove Ending Tag");
                    sceneNodeContextMenu.getItems().get(3).setDisable(true);
                } else {
                    sceneNodeContextMenu.getItems().get(2).setText("Set as Good End");
                    sceneNodeContextMenu.getItems().get(3).setDisable(false);
                }
            }
        });

        sceneNode.setOnMousePressed(mouseEvent -> {
            sceneNode.requestFocus();
            sceneNodeContextMenu.hide();
        });

        sceneNode.setOnMouseClicked(mouseEvent -> {
            //User double clicked
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    openSceneDetails(sceneNode);
                }
            }
        });
    }

    private void openSceneDetails(SceneNode sceneNode) {
        if (StageManager.getInstance().requestStageFocus(sceneNode.getSceneId())) return;

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

            StageManager.getInstance().openStage(stage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public void actionExit() {
        //Check if dialog is needed
        if (JOIPackageManager.getInstance().changesHaveOccurred()) {
            int choice = DialogUnsavedChanges.unsavedChangesDialog("Warning", "You have unsaved work, are you sure you want to quit?");
            switch (choice) {
                case CHOICE_CANCEL:
                    return;

                case CHOICE_DO_NOT_SAVE:
                    actionSaveProject();
                    break;
            }
        }

        quiteProgram();
    }

    public void quiteProgram() {
        Platform.exit();
    }

    private void addScene(final boolean isFirstScene) {
        final int sceneId = getJoiPackage().getJoi().getSceneIdCounter() + 1;
        final String defaultTitle = "Scene " + sceneId;
        String title;

        if (isFirstScene) {
            title = defaultTitle;
        } else {
            title = DialogSceneTitle.addNewSceneDialog(defaultTitle);
        }

        addScene(10, 0, title, sceneId - 1, false);
    }

    private void addScene(double xPosition, double yPosition, String title, int sceneId, boolean suppressJSONUpdating) {
        //Add new scene to json if not suppressed
        if (!suppressJSONUpdating) {
            getJoiPackage().getJoi().addNewScene(sceneId);
            getJoiPackage().getJoi().getScene(sceneId).setSceneTitle(title);
        }

        SceneNode sceneNode = new SceneNode(300, 100, sceneId, sceneNodeMainController, getJoiPackage().getJoi().getScene(sceneId));
        new Draggable.Nature(sceneNode);

        sceneNode.setTitle(title);

        //Set and save position
        if (!addSceneContextMenu) {
            //TODO issue 5 make new scenes via button adjacent
            //AsisUtils.getAllNodes(anchorPane)
            /*for (SceneNode listItem : getSceneNodes()) {
                if(listItem.getPane().getBoundsInParent().intersects(sceneNode.getBoundsInParent())) {
                    System.out.println("There is a scene node here!");
                }
            }*/

            sceneNode.setTranslateX(xPosition);
            sceneNode.setTranslateY(yPosition);
            if (!suppressJSONUpdating) {
                getJoiPackage().getJoi().getScene(sceneId).setLayoutXPosition(xPosition);
                getJoiPackage().getJoi().getScene(sceneId).setLayoutYPosition(yPosition);
            }
        } else {
            Bounds bounds = scrollPane.getViewportBounds();
            double lowestXPixelShown = -1 * bounds.getMinX();
            double lowestYPixelShown = -1 * bounds.getMinY();

            sceneNode.setTranslateX(lowestXPixelShown + menuEventX);
            sceneNode.setTranslateY(lowestYPixelShown + menuEventY);
            addSceneContextMenu = false;

            if (!suppressJSONUpdating) {
                getJoiPackage().getJoi().getScene(sceneId).setLayoutXPosition(lowestXPixelShown + menuEventX);
                getJoiPackage().getJoi().getScene(sceneId).setLayoutYPosition(lowestYPixelShown + menuEventY);
            }
        }

        setClickActionForNode(sceneNode);
        getAnchorPane().getChildren().add(sceneNode);

        getSceneNodes().add(sceneNode);
        sceneNode.refreshConnectionCenters();
    }

    private void removeScene(SceneNode sceneNode) {
        getJoiPackage().getJoi().removeScene(sceneNode.getSceneId());

        sceneNodeMainController.notifySceneRemoved(sceneNode);

        anchorPane.getChildren().remove(sceneNode);
    }

    public void actionNewProject() {
        DialogNewProject.newProjectWindow(false);
    }

    public void processNewProject(File newProjectFile, String newProjectName, String defaultProjectLanguageCode) {
        JOIPackageManager.getInstance().clear();
        JOIPackageManager.getInstance().setJoiPackageDirectory(new File(newProjectFile.getPath() + File.separator + newProjectName));
        resetJoiPackage(JOIPackageManager.getInstance().getNewJOIPackage(defaultProjectLanguageCode));
        addScene(true);
    }

    private void resetJoiPackage(JOIPackage joiPackage) {
        setJoiPackage(joiPackage);

        if (sceneNodeMainController == null) {
            sceneNodeMainController = new SceneNodeMainController(getJoiPackage());
            sceneNodeMainController.setPane(anchorPane);
            sceneNodeMainController.setScrollPane(scrollPane);
        }

        anchorPane.getChildren().clear();
        getSceneNodes().clear();
        sceneNodeMainController.setJoiPackage(joiPackage);
        sceneNodeMainController.setLineList(new ArrayList<>());
        StageManager.getInstance().closeAllStages();
    }

    public boolean actionLoadProject() {
        if (JOIPackageManager.getInstance().changesHaveOccurred()) {
            int choice = DialogUnsavedChanges.unsavedChangesDialog("Load Project", "You have unsaved work, are you sure you want to continue?");
            switch (choice) {
                case CHOICE_CANCEL:
                    return false;

                case CHOICE_SAVE:
                    actionSaveProject();
                    break;
            }
        }

        try {
            return processLoadProject();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
            return false;
        }
    }

    public boolean processLoadProject() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        File file = directoryChooser.showDialog(null);

        if (file != null) {
            //Set project directory to current
            JOIPackageManager.getInstance().clear();
            JOIPackageManager.getInstance().setJoiPackageDirectory(file);
            JOIPackage newJoiPackage = JOIPackageManager.getInstance().getJOIPackage();
            if(newJoiPackage == null) return false;

            //Load joi
            try {
                //Reset old variables
                resetJoiPackage(newJoiPackage);

                //Create scene nodes
                for (com.asis.joi.model.entites.Scene scene : getJoiPackage().getJoi().getSceneArrayList()) {
                    addScene(scene.getLayoutXPosition(), scene.getLayoutYPosition(), scene.getSceneTitle(), scene.getSceneId(), true);
                }

                //Create connections
                for (com.asis.joi.model.entites.Scene scene : getJoiPackage().getJoi().getSceneArrayList()) {
                    final AsisConnectionButton output = getSceneNodeWithId(getSceneNodes(), scene.getSceneId()).getOutputButtons().get(0);
                    createConnections(scene.getGotoScene(), output);

                    createConnectionsForDialogOutputs(scene);
                }

                //Loading completed successfully
                return true;
            } catch (RuntimeException e) {
                e.printStackTrace();
                DialogMessage.messageDialog("LOADING FAILED", "The editor was unable to load this joi for the following reason:\n" + e.getMessage(), 600, 200);
                return false;
            }
        }

        //Loading failed do to null file
        return false;
    }

    private void createConnectionsForDialogOutputs(com.asis.joi.model.entites.Scene scene) {
        if (scene.getDialog() != null && !scene.getDialog().getOptionArrayList().isEmpty()) {
            for (DialogOption dialogOption : scene.getDialog().getOptionArrayList()) {
                AsisConnectionButton output = getSceneNodeWithId(sceneNodes, scene.getSceneId()).createNewOutputConnectionPoint("Option " + dialogOption.getOptionNumber(), "dialog_option_" + (dialogOption.getOptionNumber() + 1));
                output.setOptionNumber(dialogOption.getOptionNumber());

                createConnections(dialogOption.getGotoScene(), output);
            }
        }
    }

    private void createConnections(GotoScene gotoScene, AsisConnectionButton output) {
        final boolean gotoHasSingleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() == 1;
        final boolean gotoHasMultipleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() > 1;

        //Check for scene normal connections
        if (gotoHasSingleOutput) {
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
        //System.out.println("Checking for scene with id "+sceneId);
        for (SceneNode scene : sceneList) {
            //System.out.println("Current scene id: "+scene.getSceneId());

            if (scene.getSceneId() == sceneId) {
                return scene;
            }
        }

        return null;
    }

    public void actionSaveProject() {
        saveProject(JOIPackageManager.getInstance().getJoiPackageDirectory());
    }

    public void actionSaveProjectAs() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        directoryChooser.setTitle("Save Location");
        File file = directoryChooser.showDialog(null);

        saveProject(file);
    }

    private void saveProject(File file) {
        if (file != null) JOIPackageManager.getInstance().exportJOIPackageAsFiles(file);
    }

    public void actionExportToZip() {
        //Export project as zip
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Zip");
        fileChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("zip", "*.zip"));
        File dest = fileChooser.showSaveDialog(null);

        if (dest != null) JOIPackageManager.getInstance().exportJOIPackageAsZip(dest);
    }

    public void actionGettingStarted() {
        String message = AsisUtils.getStringFromFile("/resources/text_files/getting_started.txt");
        DialogMessage.messageDialog("Getting Started", message, 720, 720);
    }

    public void actionProjectDetailsHelp() {
        String message = AsisUtils.getStringFromFile("/resources/text_files/project_details.txt");
        DialogMessage.messageDialog("Getting Started", message, 720, 720);
    }

    public void actionAbout() {
        String message = AsisUtils.getStringFromFile("/resources/text_files/about.txt");
        DialogMessage.messageDialog("About", message, 500, 250);
    }

    public void actionSceneEditor() {
        String message = AsisUtils.getStringFromFile("/resources/text_files/scene_editor.txt");
        DialogMessage.messageDialog("Scene Editor", message, 720, 720);
    }

    public void actionAddSceneButton() {
        addScene(false);
    }

    //Getters and setters
    private AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public ArrayList<SceneNode> getSceneNodes() {
        return sceneNodes;
    }

    public JOIPackage getJoiPackage() {
        return joiPackage;
    }

    public void setJoiPackage(JOIPackage joiPackage) {
        this.joiPackage = joiPackage;
    }
}
