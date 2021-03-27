package com.asis.controllers;

import com.asis.controllers.dialogs.DialogConfirmation;
import com.asis.controllers.dialogs.DialogMessage;
import com.asis.controllers.dialogs.DialogNewProject;
import com.asis.controllers.dialogs.DialogUnsavedChanges;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.LoadJOIService;
import com.asis.joi.model.entities.Arithmetic;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.Group;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.ui.InfinityPane;
import com.asis.ui.asis_node.*;
import com.asis.ui.asis_node.node_group.NodeGroup;
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
        /* Block for context menu of infinity pane. This should be moved somewhere else probably */{
            ContextMenu contextMenu = buildWorkspaceContextMenu(getInfinityPane(), -1);

            infinityPane.setContextMenu(contextMenu);
            infinityPane.setOnContextMenuRequested(contextMenuEvent -> {
                if (!getInfinityPane().nodeAtPosition(contextMenuEvent.getSceneX(), contextMenuEvent.getSceneY())) {
                    contextMenu.show(infinityPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                }

                menuEventX = contextMenuEvent.getX();
                menuEventY = contextMenuEvent.getY();
            });
        }

        gridToggle.setTooltip(new Tooltip("Snap to grid"));
        thumbnailToggle.setTooltip(new Tooltip("Toggle Scene thumbnails"));

        try {
            JSONObject object = (JSONObject) Config.get("ZOOM");
            if(object.has("minimum")) getInfinityPane().setMinimumScale(object.getDouble("minimum"));
            if(object.has("maximum")) getInfinityPane().setMaximumScale(object.getDouble("maximum"));
        } catch (ClassCastException ignore) {}
    }

    public static Controller getInstance() {
        return instance;
    }

    public static ContextMenu buildWorkspaceContextMenu(InfinityPane infinityPane, int groupId) {
        ContextMenu contextMenu = new ContextMenu();

        //Create items and add them to there menu
        MenuItem newSceneItem = new MenuItem("New Scene");
        MenuItem newVariableSetterItem = new MenuItem("New Variable");
        MenuItem newConditionItem = new MenuItem("New Condition");
        MenuItem newArithmeticItem = new MenuItem("New Arithmetic");
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem newGroupItem = new MenuItem("New Node Group");
        SeparatorMenuItem separatorMenuItem2 = new SeparatorMenuItem();

        MenuItem reset_view = new MenuItem("Reset view");
        contextMenu.getItems().addAll(newSceneItem, newVariableSetterItem, newConditionItem,
                newArithmeticItem, separatorMenuItem, newGroupItem, separatorMenuItem2, reset_view);

        //Handle menu actions
        Controller controller = Controller.getInstance();
        newSceneItem.setOnAction(event -> {
            controller.addSceneContextMenu = true;
            ComponentNodeManager.getInstance().setWorkingPane(infinityPane);
            ComponentNodeManager.getInstance().setWorkingGroupId(groupId);
            ComponentNodeManager.getInstance().addScene(false);
        });
        newVariableSetterItem.setOnAction(actionEvent -> {
            controller.addSceneContextMenu = true;
            ComponentNodeManager.getInstance().setWorkingPane(infinityPane);
            ComponentNodeManager.getInstance().setWorkingGroupId(groupId);
            ComponentNodeManager.getInstance().addJOIComponentNode(VariableSetterNode.class, VariableSetter.class);
        });
        newConditionItem.setOnAction(actionEvent -> {
            controller.addSceneContextMenu = true;
            ComponentNodeManager.getInstance().setWorkingPane(infinityPane);
            ComponentNodeManager.getInstance().setWorkingGroupId(groupId);
            ComponentNodeManager.getInstance().addJOIComponentNode(ConditionNode.class, Condition.class);
        });
        newArithmeticItem.setOnAction(actionEvent -> {
            controller.addSceneContextMenu = true;
            ComponentNodeManager.getInstance().setWorkingPane(infinityPane);
            ComponentNodeManager.getInstance().setWorkingGroupId(groupId);
            ComponentNodeManager.getInstance().addJOIComponentNode(ArithmeticNode.class, Arithmetic.class);
        });
        newGroupItem.setOnAction(actionEvent -> {
            controller.addSceneContextMenu = true;
            ComponentNodeManager.getInstance().setWorkingPane(infinityPane);
            ComponentNodeManager.getInstance().setWorkingGroupId(groupId);
            ComponentNodeManager.getInstance().addJOIComponentNode(NodeGroup.class, Group.class);
        });
        reset_view.setOnAction(actionEvent -> infinityPane.resetPosition());

        return contextMenu;
    }

    public void actionOpenMetadata() {
        if (StageManager.getInstance().requestStageFocus("metaData")) return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/meta_data_form.fxml"));
            Parent root = fxmlLoader.load();

            MetaDataForm metaDataForm = fxmlLoader.getController();
            metaDataForm.inflateJOIPackageObject(LoadJOIService.getInstance().getJoiPackage());

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

        //Clear all previous nodes and set new package
        LoadJOIService.getInstance().setJoiPackage(JOIPackageManager.getInstance().getNewJOIPackage(defaultProjectLanguageCode));
        getInfinityPane().getContainer().getChildren().clear();
        getJoiComponentNodes().clear();

        //Set the pane and add the first scene
        ComponentNodeManager.getInstance().setWorkingPane(infinityPane);
        ComponentNodeManager.getInstance().addScene(true);
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
            return LoadJOIService.getInstance().processLoadProject(getInfinityPane());
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
            return false;
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
