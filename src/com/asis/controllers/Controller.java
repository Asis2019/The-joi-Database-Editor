package com.asis.controllers;

import com.asis.controllers.dialogs.*;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.entities.GotoScene;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
import com.asis.ui.asis_node.*;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.asis.controllers.dialogs.DialogUnsavedChanges.CHOICE_CANCEL;
import static com.asis.controllers.dialogs.DialogUnsavedChanges.CHOICE_SAVE;

public class Controller {
    private JOIComponentNode selectedComponent;
    private double menuEventX;
    private double menuEventY;
    private Boolean addSceneContextMenu = false;
    private static Controller instance = null;
    private boolean snapToGrid = false;

    private JOIPackage joiPackage;
    private final ArrayList<JOIComponentNode> joiComponentNodes = new ArrayList<>();

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
    @FXML
    private Button gridToggle;

    public void initialize() {
        instance = this;

        setupMainContextMenu();
        setupSceneNodeContextMenu();

        gridToggle.setTooltip(new Tooltip("Snap to grid"));
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
            if (selectedComponent != null) {
                openSceneDetails((SceneNode) selectedComponent);
            }
        });

        editNameItem.setOnAction(actionEvent -> {
            if (selectedComponent != null) {
                String title = DialogSceneTitle.addNewSceneDialog(selectedComponent.getTitle());

                getJoiPackage().getJoi().getScene(selectedComponent.getComponentId()).setComponentTitle(title);
                selectedComponent.setTitle(title);
            }
        });

        deleteNodeItem.setOnAction(actionEvent -> {
            if (selectedComponent != null) {
                removeScene((SceneNode) selectedComponent);
            }
        });

        goodEndItem.setOnAction(actionEvent -> {
            if (selectedComponent != null) {
                ((SceneNode)selectedComponent).setIsGoodEnd(!((SceneNode)selectedComponent).isGoodEnd());
            }
        });

        badEndItem.setOnAction(actionEvent -> {
            if (selectedComponent != null) {
                ((SceneNode)selectedComponent).setIsBadEnd(!((SceneNode)selectedComponent).isBadEnd());
            }
        });
    }

    private void setupMainContextMenu() {
        //Create items and add them to there menu
        MenuItem newSceneItem = new MenuItem("New Scene");
        MenuItem newVariableSetterItem = new MenuItem("New Variable");
        mainContextMenu.getItems().addAll(newSceneItem, newVariableSetterItem);


        //Handle menu actions
        newSceneItem.setOnAction(event -> {
            addSceneContextMenu = true;
            addScene(false);
        });

        newVariableSetterItem.setOnAction(actionEvent -> {
            addSceneContextMenu = true;
            addVariableSetterNode();
        });

        scrollPane.setContextMenu(mainContextMenu);

        //Used to get the coordinates for spawning the scene node and for hiding/showing the proper menu
        scrollPane.setOnContextMenuRequested(contextMenuEvent -> {
            for (Node n : anchorPane.getChildren()) {
                if (n.getUserData() == null) continue;

                if (n.getUserData().equals("sceneNode")) {
                    Bounds boundsInScene = n.localToScene(n.getBoundsInLocal());

                    if (contextMenuEvent.getSceneX() >= boundsInScene.getMinX() &&
                            contextMenuEvent.getSceneX() <= boundsInScene.getMaxX() &&
                            contextMenuEvent.getSceneY() >= boundsInScene.getMinY() &&
                            contextMenuEvent.getSceneY() <= boundsInScene.getMaxY()) {
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

    private void setClickActionForNode(JOIComponentNode componentNode) {
        componentNode.setOnContextMenuRequested(contextMenuEvent -> {
            selectedComponent = componentNode;

            //Change behaviour for first scene
            if (componentNode.getComponentId() == 0) {
                //Is the first scene
                sceneNodeContextMenu.getItems().get(sceneNodeContextMenu.getItems().size() - 1).setDisable(true);
                sceneNodeContextMenu.getItems().get(2).setDisable(true);
                sceneNodeContextMenu.getItems().get(3).setDisable(true);
            } else if(componentNode instanceof SceneNode) {
                SceneNode sceneNode = (SceneNode) componentNode;
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

        componentNode.setOnMousePressed(mouseEvent -> {
            componentNode.requestFocus();
            sceneNodeContextMenu.hide();
        });

        componentNode.setOnMouseClicked(mouseEvent -> {
            //User double clicked
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
                if (mouseEvent.getClickCount() == 2) {
                    if(componentNode instanceof SceneNode)
                        openSceneDetails((SceneNode) componentNode);
                    else if(componentNode instanceof VariableSetterNode)
                        DialogVariableSetter.openVariableSetter((VariableSetter) componentNode.getJoiComponent());
                }
        });
    }

    private void openSceneDetails(SceneNode sceneNode) {
        if (StageManager.getInstance().requestStageFocus(sceneNode.getComponentId())) return;

        //Open new window
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/SceneDetails.fxml"));
            Parent root = fxmlLoader.load();

            SceneDetails sceneDetails = fxmlLoader.getController();
            sceneDetails.initialize(getJoiPackage().getJoi().getScene(sceneNode.getComponentId()));

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setTitle(sceneNode.getTitle());
            stage.setUserData(sceneNode.getComponentId());
            stage.setScene(new Scene(root, 1280, 720));

            StageManager.getInstance().openStage(stage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public void actionExit() {
        Stage stage = (Stage) getAnchorPane().getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void quiteProgram() {
        Platform.exit();
    }

    private void addVariableSetterNode() {
        final int componentId = getJoiPackage().getJoi().getSceneIdCounter();
        addVariableSetterNode(0, 10, null, componentId, false);
    }

    private void addVariableSetterNode(double xPosition, double yPosition, String title, int componentId, boolean suppressJSONUpdating) {
        if(!suppressJSONUpdating) {
            getJoiPackage().getJoi().addNewVariableSetter(componentId);
            getJoiPackage().getJoi().getComponent(componentId).setComponentTitle(title);
        }

        JOIComponentNode componentNode = new VariableSetterNode(300, 100, componentId, sceneNodeMainController, getJoiPackage().getJoi().getComponent(componentId));
        new Draggable.Nature(componentNode);
        componentNode.setTitle(title);

        //Set and save position
        if (!addSceneContextMenu) {
            componentNode.positionInGrid(xPosition, yPosition);

            if(!suppressJSONUpdating) {
                getJoiPackage().getJoi().getComponent(componentId).setLayoutXPosition(xPosition);
                getJoiPackage().getJoi().getComponent(componentId).setLayoutYPosition(yPosition);
            }
        } else {
            Bounds bounds = scrollPane.getViewportBounds();
            double lowestXPixelShown = -1 * bounds.getMinX();
            double lowestYPixelShown = -1 * bounds.getMinY();

            componentNode.positionInGrid(lowestXPixelShown + menuEventX, lowestYPixelShown + menuEventY);
            addSceneContextMenu = false;

            //No suppress check because block only gets run from context menu
            getJoiPackage().getJoi().getComponent(componentId).setLayoutXPosition(lowestXPixelShown + menuEventX);
            getJoiPackage().getJoi().getComponent(componentId).setLayoutYPosition(lowestYPixelShown + menuEventY);
        }


        setClickActionForNode(componentNode);
        getAnchorPane().getChildren().add(componentNode);
        getJoiComponentNodes().add(componentNode);

        if(!suppressJSONUpdating)
            DialogVariableSetter.openVariableSetter((VariableSetter) componentNode.getJoiComponent());
    }

    private void addScene(final boolean isFirstScene) {
        final int sceneId = getJoiPackage().getJoi().getSceneIdCounter() + 1;
        final String defaultTitle = "Scene " + sceneId;
        String title;

        if (isFirstScene) {
            title = defaultTitle;
        } else {
            title = DialogSceneTitle.addNewSceneDialog(defaultTitle);
            if(title == null) return;
        }

        addScene(10, 0, title, sceneId - 1, false);
    }

    private void addScene(double xPosition, double yPosition, String title, int sceneId, boolean suppressJSONUpdating) {
        //Add new scene to json if not suppressed
        if (!suppressJSONUpdating) {
            getJoiPackage().getJoi().addNewScene(sceneId);
            getJoiPackage().getJoi().getComponent(sceneId).setComponentTitle(title);
        }

        SceneNode sceneNode = new SceneNode(300, 100, sceneId, sceneNodeMainController, getJoiPackage().getJoi().getScene(sceneId));
        new Draggable.Nature(sceneNode);

        sceneNode.setTitle(title);

        //Set and save position
        if (!addSceneContextMenu) {
            //TODO issue 5 make new scenes via button adjacent
            sceneNode.positionInGrid(xPosition, yPosition);
            if (!suppressJSONUpdating) {
                getJoiPackage().getJoi().getComponent(sceneId).setLayoutXPosition(xPosition);
                getJoiPackage().getJoi().getComponent(sceneId).setLayoutYPosition(yPosition);
            }
        } else {
            Bounds bounds = scrollPane.getViewportBounds();
            double lowestXPixelShown = -1 * bounds.getMinX();
            double lowestYPixelShown = -1 * bounds.getMinY();

            sceneNode.positionInGrid(lowestXPixelShown + menuEventX, lowestYPixelShown + menuEventY);
            addSceneContextMenu = false;

            if (!suppressJSONUpdating) {
                getJoiPackage().getJoi().getComponent(sceneId).setLayoutXPosition(lowestXPixelShown + menuEventX);
                getJoiPackage().getJoi().getComponent(sceneId).setLayoutYPosition(lowestYPixelShown + menuEventY);
            }
        }

        setClickActionForNode(sceneNode);
        getAnchorPane().getChildren().add(sceneNode);
        getJoiComponentNodes().add(sceneNode);
    }

    private void removeScene(SceneNode sceneNode) {
        getJoiPackage().getJoi().removeComponent(sceneNode.getComponentId());

        sceneNodeMainController.notifySceneRemoved(sceneNode);

        anchorPane.getChildren().remove(sceneNode);
    }

    public void actionNewProject() {
        DialogNewProject.newProjectWindow(false);
    }

    public void processNewProject(File newProjectFile, String newProjectName, String defaultProjectLanguageCode) throws FileAlreadyExistsException {
        File newProjectDirectory = new File(newProjectFile.getPath() + File.separator + newProjectName);
        if (newProjectDirectory.exists()) {
            boolean notEmpty = Arrays.stream(Objects.requireNonNull(newProjectDirectory.list())).anyMatch(fileName -> fileName.contains("joi_text_") || fileName.contains("info_"));

            if(notEmpty) {
                DialogMessage.messageDialog("WARNING", String.format("The project path: %s is not empty.\nPlease select a different path or empty the current one.",
                        newProjectDirectory.getAbsolutePath()), 600, 200);
                throw new FileAlreadyExistsException("CANCEL");
            }
        }

        JOIPackageManager.getInstance().clear();
        JOIPackageManager.getInstance().setJoiPackageDirectory(newProjectDirectory);
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
        getJoiComponentNodes().clear();
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
            try {
                //Set project directory to current
                JOIPackageManager.getInstance().setJoiPackageDirectory(file);
                JOIPackage newJoiPackage = JOIPackageManager.getInstance().getJOIPackage();
                if (newJoiPackage == null) return false;

                //Load joi
                //Reset old variables
                resetJoiPackage(newJoiPackage);

                //Create component nodes
                for (JOIComponent component : getJoiPackage().getJoi().getJoiComponents()) {
                    if(component instanceof com.asis.joi.model.entities.Scene)
                        addScene(component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);
                    else if(component instanceof VariableSetter)
                        addVariableSetterNode(component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);
                }
                //Create connections
                for (JOIComponent component : getJoiPackage().getJoi().getJoiComponents()) {
                    if(component instanceof com.asis.joi.model.entities.Scene) {
                        com.asis.joi.model.entities.Scene scene = (com.asis.joi.model.entities.Scene) component;
                        final AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);
                        if(scene.hasComponent(GotoScene.class)) createConnections(scene.getComponent(GotoScene.class), output);

                        createConnectionsForDialogOutputs(scene);
                    } else if(component instanceof VariableSetter) {
                        VariableSetter setter = (VariableSetter) component;
                        final AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);
                        createConnections(setter.getGotoScene(), output);
                    }
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

    private void createConnectionsForDialogOutputs(com.asis.joi.model.entities.Scene scene) {
        if (scene.hasComponent(Dialog.class) && !scene.getComponent(Dialog.class).getOptionArrayList().isEmpty()) {
            for (DialogOption dialogOption : scene.getComponent(Dialog.class).getOptionArrayList()) {
                AsisConnectionButton output = getJOIComponentNodeWithId(joiComponentNodes, scene.getComponentId()).createNewOutputConnectionPoint("Option " + dialogOption.getOptionNumber(), "dialog_option_" + (dialogOption.getOptionNumber() + 1));
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
            AsisConnectionButton input = getJOIComponentNodeWithId(getJoiComponentNodes(), gotoScene.getGotoSceneArrayList().get(0)).getInputConnection();
            sceneNodeMainController.createConnection(output, input);
        }

        //Check for scene range connections
        if (gotoHasMultipleOutput) {
            for (int i = 0; i < gotoScene.getGotoSceneArrayList().size(); i++) {
                AsisConnectionButton input = getJOIComponentNodeWithId(getJoiComponentNodes(), gotoScene.getGotoSceneArrayList().get(i)).getInputConnection();
                sceneNodeMainController.createConnection(output, input);
            }
        }
    }

    public JOIComponentNode getJOIComponentNodeWithId(ArrayList<JOIComponentNode> components, int componentId) {
        for (JOIComponentNode componentNode : components) if (componentNode.getComponentId() == componentId) return componentNode;
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

    public void actionToggleGrid() {
        snapToGrid = !snapToGrid;
        ImageView imageView;
        if(snapToGrid) {
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_grid_on.png")));
        } else {
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_grid_off.png")));
        }
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        gridToggle.setGraphic(imageView);
    }

    //Getters and setters
    private AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public ArrayList<JOIComponentNode> getJoiComponentNodes() {
        return joiComponentNodes;
    }

    public JOIPackage getJoiPackage() {
        return joiPackage;
    }

    public void setJoiPackage(JOIPackage joiPackage) {
        this.joiPackage = joiPackage;
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }
}
