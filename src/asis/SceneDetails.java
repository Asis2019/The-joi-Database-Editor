package asis;

import asis.custom_objects.asis_node.SceneNode;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SceneDetails {
    private Story story;
    private SceneNode sceneNode;
    private int sceneId;

    @FXML private TabTimerController tabTimerController;
    @FXML private TabNormalOperationController tabNormalOperationController;
    @FXML private TabTransitionController tabTransitionController;
    @FXML private TabDialogOptionController tabDialogController;
    @FXML private TabPane effectTabs;
    @FXML private Tab timerTab, transitionTab, dialogOptionsTab;
    @FXML private MenuItem menuItemAddTimer, menuItemAddTransition, menuItemAddDialog;
    @FXML private BorderPane sceneDetailBorderPane;

    public void initialize() {
        effectTabs.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            switch (newTab.getText()) {
                case "Normal Operation":
                    tabNormalOperationController.setVisibleImage();
                    break;

                case "Timer":
                    tabTimerController.setVisibleImage();
                    break;

                case "Transition":
                    break;
            }
        });
    }

    void passData(Story story, SceneNode sceneNode) {
        this.story = story;
        this.sceneNode = sceneNode;
        this.sceneId = sceneNode.getSceneId();

        if(tabNormalOperationController != null) {
            tabNormalOperationController.passData(story, sceneId);
        }

        if(tabTransitionController != null) {
            tabTransitionController.passData(story, sceneId);
        }

        if(story.hasNoFade(sceneId)) {
            effectTabs.getTabs().remove(transitionTab);
            menuItemAddTransition.setDisable(false);
        }

        if(story.getDialogData(sceneId) != null) {
            actionAddDialog();
        }

        if(story.getTimerData(sceneId) != null) {
            actionAddTimer();
        }
    }

    public void actionClose() {
        Stage stage = (Stage) sceneDetailBorderPane.getScene().getWindow();
        stage.close();
    }

    public void actionChangeSceneImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(story.getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            //Add image to json object
            story.addDataToScene(sceneId, "sceneImage", file.getName());
            story.addImage(file);

            //setVisibleImage();
            if(tabTimerController != null) {
                tabTimerController.setVisibleImage();
            }

            tabNormalOperationController.setVisibleImage();
            Controller.getInstance().setNewChanges();
        }
    }

    public void actionAddTimer() {
        try {
            timerTab = new Tab("Timer");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/tab_timer.fxml"));
            Parent root = fxmlLoader.load();

            tabTimerController = fxmlLoader.getController();
            tabTimerController.passData(story, sceneId);

            timerTab.setContent(root);
            timerTab.setOnCloseRequest(this::actionTimerCloseRequested);

            effectTabs.getTabs().add(1, timerTab);

            if(tabTimerController != null) {
                tabTimerController.passData(story, sceneId);
            }

            menuItemAddTimer.setDisable(true);
            Controller.getInstance().setNewChanges();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionAddTransition() {
        try {
            transitionTab = new Tab("Transition");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/tab_transition.fxml"));
            Parent root = fxmlLoader.load();

            tabTransitionController = fxmlLoader.getController();
            tabTransitionController.passData(story, sceneId);

            transitionTab.setContent(root);
            transitionTab.setOnCloseRequest(this::actionTransitionCloseRequested);

            effectTabs.getTabs().add(transitionTab);

            menuItemAddTransition.setDisable(true);

            story.removeDataFromScene(sceneId, "noFade");
            Controller.getInstance().setNewChanges();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionAddDialog() {
        try {
            dialogOptionsTab = new Tab("Dialog Options");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/tab_dialog_option.fxml"));
            Parent root = fxmlLoader.load();

            tabDialogController = fxmlLoader.getController();
            tabDialogController.passData(story, sceneNode);

            dialogOptionsTab.setContent(root);
            dialogOptionsTab.setOnCloseRequest(this::actionDialogCloseRequested);

            effectTabs.getTabs().add(1, dialogOptionsTab);

            menuItemAddDialog.setDisable(true);
            Controller.getInstance().setNewChanges();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void actionTransitionClosed() {
        menuItemAddTransition.setDisable(false);

        story.removeTransition(sceneId);
        story.addDataToScene(sceneId, "noFade", true);
        Controller.getInstance().setNewChanges();
    }

    private void actionTimerClosed() {
        menuItemAddTimer.setDisable(false);
        story.removeTimer(sceneId);
        Controller.getInstance().setNewChanges();
    }

    private void actionDialogClosed() {
        menuItemAddDialog.setDisable(false);
        sceneNode.removeAllOutputConnection();
        story.removeDialog(sceneId);
        Controller.getInstance().setNewChanges();
    }

    private void actionDialogCloseRequested(Event event) {
        //Check if dialog is needed
        if(story.getDialogData(sceneId) != null && !story.getDialogData(sceneId).isEmpty()) {
            if (!new Alerts().confirmationDialog(this.getClass(), "Delete Dialogs", "Are you sure you want to remove dialogs?")) {
                event.consume();
                return;
            }
        }

        //Close tab properly
        actionDialogClosed();
    }

    public void actionTransitionCloseRequested(Event event) {
        if(story.getTransitionData(sceneId) != null && !story.getTransitionData(sceneId).isEmpty()) {
            if (!new Alerts().confirmationDialog(this.getClass(), "Delete Transition", "Are you sure you want to remove the transition?")) {
                event.consume();
                return;
            }
        }

        //Close tab properly
        actionTransitionClosed();
    }

    private void actionTimerCloseRequested(Event event) {
        if(story.getTimerData(sceneId) != null && !story.getTimerData(sceneId).isEmpty()) {
            if (!new Alerts().confirmationDialog(this.getClass(), "Delete Timer", "Are you sure you want to remove the timer?")) {
                event.consume();
                return;
            }
        }

        //Close tab properly
        actionTimerClosed();
    }
}
