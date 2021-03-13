package com.asis.controllers;

import com.asis.controllers.dialogs.DialogConfirmation;
import com.asis.controllers.dialogs.DialogMessage;
import com.asis.controllers.dialogs.DialogNewProject;
import com.asis.controllers.dialogs.DialogUnsavedChanges;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
import com.asis.ui.InfinityPane;
import com.asis.ui.asis_node.*;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Config;
import com.asis.utilities.SelectionModel;
import com.asis.utilities.StageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.asis.controllers.dialogs.DialogUnsavedChanges.CHOICE_CANCEL;
import static com.asis.controllers.dialogs.DialogUnsavedChanges.CHOICE_SAVE;

public class Controller {
    public double menuEventX;
    public double menuEventY;
    public Boolean addSceneContextMenu = false;
    private static Controller instance = null;
    private boolean snapToGrid = false;
    private boolean showThumbnail = false;

    private SelectionModel selectionModel = new SelectionModel();
    private JOIPackage joiPackage;

    private final ContextMenu mainContextMenu = new ContextMenu();

    @FXML
    private InfinityPane infinityPane;
    @FXML
    public MenuBar mainMenuBar;
    @FXML
    public ToolBar toolBar;
    @FXML
    private Button gridToggle, thumbnailToggle;

    public void initialize() {
        instance = this;
        setupMainContextMenu();
        gridToggle.setTooltip(new Tooltip("Snap to grid"));
        thumbnailToggle.setTooltip(new Tooltip("Toggle Scene thumbnails"));

        try {
            JSONObject object = (JSONObject) Config.get("ZOOM");
            if(object.has("minimum")) getInfinityPane().setMinimumScale(object.getDouble("minimum"));
            if(object.has("maximum")) getInfinityPane().setMaximumScale(object.getDouble("maximum"));

        } catch (ClassCastException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public static Controller getInstance() {
        return instance;
    }

    private void setupMainContextMenu() {
        //Create items and add them to there menu
        MenuItem newSceneItem = new MenuItem("New Scene");
        MenuItem newVariableSetterItem = new MenuItem("New Variable");
        MenuItem newConditionItem = new MenuItem("New Condition");
        MenuItem newArithmeticItem = new MenuItem("New Arithmetic");
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        MenuItem reset_view = new MenuItem("Reset view");
        mainContextMenu.getItems().addAll(newSceneItem, newVariableSetterItem, newConditionItem,
                newArithmeticItem, separatorMenuItem, reset_view);

        //Handle menu actions
        newSceneItem.setOnAction(event -> {
            addSceneContextMenu = true;
            ComponentNodeManager.getInstance().addScene(false);
        });
        newVariableSetterItem.setOnAction(actionEvent -> {
            addSceneContextMenu = true;
            ComponentNodeManager.getInstance().addJOIComponentNode(VariableSetterNode.class, VariableSetter.class);
        });
        newConditionItem.setOnAction(actionEvent -> {
            addSceneContextMenu = true;
            ComponentNodeManager.getInstance().addJOIComponentNode(ConditionNode.class, Condition.class);
        });
        newArithmeticItem.setOnAction(actionEvent -> {
            addSceneContextMenu = true;
            ComponentNodeManager.getInstance().addJOIComponentNode(ArithmeticNode.class, Arithmetic.class);
        });
        reset_view.setOnAction(actionEvent -> getInfinityPane().resetPosition());

        infinityPane.setContextMenu(mainContextMenu);
        infinityPane.setOnContextMenuRequested(contextMenuEvent -> {
            if (!getInfinityPane().nodeAtPosition(contextMenuEvent.getSceneX(), contextMenuEvent.getSceneY())) {
                mainContextMenu.show(infinityPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            }

            menuEventX = contextMenuEvent.getX();
            menuEventY = contextMenuEvent.getY();
        });
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

    public void actionExit() {
        Stage stage = (Stage) getInfinityPane().getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void quiteProgram() {
        Platform.exit();
    }

    public void actionNewProject() {
        DialogNewProject.newProjectWindow(false);
    }

    public void processNewProject(File newProjectFile, String newProjectName, String defaultProjectLanguageCode) throws FileAlreadyExistsException {
        File newProjectDirectory = new File(newProjectFile.getPath() + File.separator + newProjectName);
        if (newProjectDirectory.exists()) {
            boolean notEmpty = Arrays.stream(Objects.requireNonNull(newProjectDirectory.list())).anyMatch(fileName -> fileName.contains("joi_text_") || fileName.contains("info_"));

            if (notEmpty) {
                DialogMessage.messageDialog("WARNING", String.format("The project path: %s is not empty.\nPlease select a different path or empty the current one.",
                        newProjectDirectory.getAbsolutePath()), 600, 200);
                throw new FileAlreadyExistsException("CANCEL");
            }
        }

        JOIPackageManager.getInstance().clear();
        JOIPackageManager.getInstance().setJoiPackageDirectory(newProjectDirectory);
        resetJoiPackage(JOIPackageManager.getInstance().getNewJOIPackage(defaultProjectLanguageCode));
        ComponentNodeManager.getInstance().addScene(true);
    }

    private void resetJoiPackage(JOIPackage joiPackage) {
        setJoiPackage(joiPackage);

        infinityPane.getContainer().getChildren().clear();
        getJoiComponentNodes().clear();

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
                    if (component instanceof com.asis.joi.model.entities.Scene)
                        ComponentNodeManager.getInstance().addJOIComponentNode(
                                SceneNode.class, com.asis.joi.model.entities.Scene.class,
                                component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(),
                                component.getComponentId(), true);
                    else if (component instanceof VariableSetter)
                        ComponentNodeManager.getInstance().addJOIComponentNode(
                                VariableSetterNode.class, VariableSetter.class,
                                component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);
                    else if (component instanceof Condition)
                        ComponentNodeManager.getInstance().addJOIComponentNode(
                                ConditionNode.class, Condition.class,
                                component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);
                    else if (component instanceof Arithmetic)
                        ComponentNodeManager.getInstance().addJOIComponentNode(
                                ArithmeticNode.class, Arithmetic.class,
                                component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);
                }
                //Create connections
                for (JOIComponent component : getJoiPackage().getJoi().getJoiComponents()) {
                    if (component instanceof com.asis.joi.model.entities.Scene) {
                        com.asis.joi.model.entities.Scene scene = (com.asis.joi.model.entities.Scene) component;
                        final AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);
                        if (scene.hasComponent(GotoScene.class))
                            createConnections(scene.getComponent(GotoScene.class), output);

                        createConnectionsForDialogOutputs(scene);
                    } else if (component instanceof VariableSetter) {
                        VariableSetter setter = (VariableSetter) component;
                        final AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);
                        createConnections(setter.getGotoScene(), output);
                    } else if (component instanceof Condition) {
                        Condition condition = (Condition) component;
                        final AsisConnectionButton trueOutput = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);
                        final AsisConnectionButton falseOutput = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(1);

                        createConnections(condition.getGotoSceneTrue(), trueOutput);
                        createConnections(condition.getGotoSceneFalse(), falseOutput);
                    } else if (component instanceof Arithmetic) {
                        Arithmetic arithmetic = (Arithmetic) component;
                        final AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);

                        createConnections(arithmetic.getGotoScene(), output);
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
                AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), scene.getComponentId()).createNewOutputConnectionPoint("Option " + dialogOption.getOptionNumber(), "dialog_option_" + (dialogOption.getOptionNumber() + 1));
                output.setOptionNumber(dialogOption.getOptionNumber());

                createConnections(dialogOption.getGotoScene(), output);
            }
        }
    }

    private void createConnections(GotoScene gotoScene, AsisConnectionButton output) {
        final boolean gotoHasSingleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() == 1;
        final boolean gotoHasMultipleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() > 1;

        try {
            ComponentConnectionManager componentConnectionManager = ComponentConnectionManager.getInstance();

            //Check for scene normal connections
            if (gotoHasSingleOutput) {
                AsisConnectionButton input = getJOIComponentNodeWithId(getJoiComponentNodes(), gotoScene.getGotoSceneArrayList().get(0)).getInputConnection();
                componentConnectionManager.createConnection(output, input);
            }

            //Check for scene range connections
            if (gotoHasMultipleOutput) {
                for (int i = 0; i < gotoScene.getGotoSceneArrayList().size(); i++) {
                    AsisConnectionButton input = getJOIComponentNodeWithId(getJoiComponentNodes(), gotoScene.getGotoSceneArrayList().get(i)).getInputConnection();
                    componentConnectionManager.createConnection(output, input);
                }
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("Failed to load scene: " + output.getJoiComponent().getComponentTitle());
        }
    }

    public JOIComponentNode getJOIComponentNodeWithId(ArrayList<JOIComponentNode> components, int componentId) {
        for (JOIComponentNode componentNode : components)
            if (componentNode.getComponentId() == componentId) return componentNode;
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
        ComponentNodeManager.getInstance().addScene(false);
    }

    public void actionToggleGrid() {
        snapToGrid = !snapToGrid;
        ImageView imageView;
        if (snapToGrid)
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_grid_on.png")));
        else
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_grid_off.png")));

        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        gridToggle.setGraphic(imageView);
    }

    public void actionToggleThumbnail() {
        showThumbnail = !showThumbnail;
        ImageView imageView;
        if (showThumbnail)
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_thumbnail_on.png")));
        else
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_thumbnail_off.png")));

        getJoiComponentNodes().forEach(joiComponentNode -> {
            if (joiComponentNode instanceof SceneNode)
                ((SceneNode) joiComponentNode).toggleSceneThumbnail(showThumbnail);
        });

        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        thumbnailToggle.setGraphic(imageView);
    }

    //Getters and setters
    public InfinityPane getInfinityPane() {
        return infinityPane;
    }

    public ArrayList<JOIComponentNode> getJoiComponentNodes() {
        return ComponentNodeManager.getInstance().getJoiComponentNodes();
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

    public boolean isShowThumbnail() {
        return showThumbnail;
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }
}
