package com.asis.controllers;

import com.asis.controllers.tabs.*;
import com.asis.joi.components.Scene;
import com.asis.joi.components.Timer;
import com.asis.joi.components.Transition;
import com.asis.joi.components.dialog.Dialog;
import com.asis.utilities.Alerts;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.StageManager;
import javafx.application.Platform;
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
    private Scene scene;

    private TabNormalOperationController tabNormalOperationController;
    private TabTimerController tabTimerController;
    private TabDialogOptionController tabDialogOptionController;
    private TabTransitionController tabTransitionController;

    @FXML private TabPane effectTabs;
    @FXML private MenuItem menuItemAddTimer, menuItemAddTransition, menuItemAddDialog;
    @FXML private BorderPane sceneDetailBorderPane;

    void initialize(Scene scene) {
        setScene(scene);

        //Load appropriate tabs for current scene
        try {
            //Normal tab always need to be loaded and present
            TabNormalOperationController tabNormalOperationController = new TabNormalOperationController("Normal Operation", getScene());
            tabNormalOperationController.setClosable(false);
            setTabNormalOperationController(tabNormalOperationController);
            createNewTab(tabNormalOperationController, "/resources/fxml/tab_normal_operation.fxml");

            //add timer tab to sceneDetails
            if (getScene().getTimer() != null) {
                addTimerTab(getScene().getTimer());
            }

            //add dialog tab to sceneDetails
            if (getScene().getDialog() != null) {
                addDialogTab(getScene().getDialog());
            }

            //add transition tab to sceneDetails
            if (getScene().getTransition() != null) {
                addTransitionTab(getScene().getTransition());
            }
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        Platform.runLater(this::addListenerToEffectTab);
    }

    private void addListenerToEffectTab() {
        getEffectTabs().getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            //This is done so that if a scene image is added in one tab, it is synced with the others
            switch (newTab.getText()) {
                case "Normal Operation":
                    if(getTabNormalOperationController() != null) {
                        getTabNormalOperationController().setVisibleImage();
                    }
                    break;

                case "Timer":
                    if(getTabTimerController() != null) {
                        getTabTimerController().setVisibleImage();
                    }
                    break;
            }
        });
    }

    private void addTimerTab(Timer timer) {
        try {
            TabTimerController tabTimerController = new TabTimerController("Timer", timer);
            setTabTimerController(tabTimerController);
            createNewTab(tabTimerController, "/resources/fxml/tab_timer.fxml", 1);
            menuItemAddTimer.setDisable(true);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    private void addDialogTab(Dialog dialog) {
        try {
            TabDialogOptionController tabDialogOptionController = new TabDialogOptionController("Dialog", dialog);
            setTabDialogOptionController(tabDialogOptionController);
            createNewTab(tabDialogOptionController, "/resources/fxml/tab_dialog_option.fxml", 1);
            menuItemAddDialog.setDisable(true);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    private void addTransitionTab(Transition transition) {
        try {
            TabTransitionController tabTransitionController = new TabTransitionController("Transition", transition);
            setTabTransitionController(tabTransitionController);
            createNewTab(tabTransitionController, "/resources/fxml/tab_transition.fxml");
            menuItemAddTransition.setDisable(true);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    private void createNewTab(final TabController tabController, final String fxmlResourcePath, final int... indexPosition) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SceneDetails.class.getResource(fxmlResourcePath));
        fxmlLoader.setController(tabController);
        Parent root = fxmlLoader.load();

        Tab newTab = new Tab();
        newTab.setText(tabController.getTabTitle());
        newTab.setClosable(tabController.isClosable());
        newTab.setContent(root);
        newTab.setOnCloseRequest(this::handleOnTabCloseRequested);

        if(indexPosition.length > 0) {
            getEffectTabs().getTabs().add(indexPosition[0], newTab);
        } else {
            getEffectTabs().getTabs().add(newTab);
        }
    }

    private void handleOnTabCloseRequested(Event event) {
        final Tab toCloseTab = ((Tab) event.getSource());

        switch(toCloseTab.getText()) {
            case "Normal Operation":
                Alerts.messageDialog("Error", "Normal operations tab can't be closed.");
                break;

            case "Timer":
                if(!getTabTimerController().getTimer().equals(new Timer())) {
                    if (!new Alerts().confirmationDialog("Delete Timer", "Are you sure you want to remove the timer?")) {
                        event.consume();
                        return;
                    }
                }
                closeTimer();
                break;

            case "Transition":
                if(!getTabTransitionController().getTransition().equals(new Transition())) {
                    if (!new Alerts().confirmationDialog("Delete Transition", "Are you sure you want to remove the transition?")) {
                        event.consume();
                        return;
                    }
                }
                closeTransition();
                break;

            case "Dialog":
                if(!getTabDialogOptionController().getDialog().equals(new Dialog())) {
                    if (!new Alerts().confirmationDialog("Delete Dialogs", "Are you sure you want to remove dialogs?")) {
                        event.consume();
                        return;
                    }
                }
                closeDialog();
                break;
        }

        toCloseTab.getTabPane().getTabs().remove(toCloseTab);
    }

    private void closeTransition() {
        menuItemAddTransition.setDisable(false);
        setTabTransitionController(null);
        getScene().setTransition(null);
    }

    private void closeDialog() {
        menuItemAddDialog.setDisable(false);
        Controller.getInstance().getSceneNodeWithId(Controller.getInstance().getSceneNodes(), getScene().getSceneId()).removeAllOutputConnection();
        setTabDialogOptionController(null);
        getScene().setDialog(null);
    }

    private void closeTimer() {
        menuItemAddTimer.setDisable(false);

        setTabTimerController(null);
        getScene().setTimer(null);
    }

    @FXML private void actionClose() {
        Stage stage = (Stage) sceneDetailBorderPane.getScene().getWindow();
        StageManager.getInstance().closeStage(stage);
    }

    @FXML private void actionChangeSceneImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(Controller.getInstance().getJoiPackage().getPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            //Add image to json object
            getScene().setSceneImage(file);

            //setVisibleImage();
            if(getTabNormalOperationController() != null) {
                getTabNormalOperationController().setVisibleImage();
            }

            if(getTabTimerController() != null) {
                getTabTimerController().setVisibleImage();
            }
        }
    }

    @FXML private void menuItemAddTimer() {
        Timer timer = new Timer();
        getScene().setTimer(timer);

        addTimerTab(timer);
    }

    @FXML private void menuItemAddTransition() {
        Transition transition = new Transition();
        getScene().setTransition(transition);

        addTransitionTab(transition);
    }

    @FXML private void menuItemAddDialog() {
        Dialog dialog = new Dialog();
        getScene().setDialog(dialog);

        addDialogTab(dialog);
    }

    //Getters and setters
    public Scene getScene() {
        return scene;
    }
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    private TabPane getEffectTabs() {
        return effectTabs;
    }
    private void setEffectTabs(TabPane effectTabs) {
        this.effectTabs = effectTabs;
    }

    private TabNormalOperationController getTabNormalOperationController() {
        return tabNormalOperationController;
    }
    private void setTabNormalOperationController(TabNormalOperationController tabNormalOperationController) {
        this.tabNormalOperationController = tabNormalOperationController;
    }

    private TabTimerController getTabTimerController() {
        return tabTimerController;
    }
    private void setTabTimerController(TabTimerController tabTimerController) {
        this.tabTimerController = tabTimerController;
    }

    private TabDialogOptionController getTabDialogOptionController() {
        return tabDialogOptionController;
    }
    private void setTabDialogOptionController(TabDialogOptionController tabDialogOptionController) {
        this.tabDialogOptionController = tabDialogOptionController;
    }

    private TabTransitionController getTabTransitionController() {
        return tabTransitionController;
    }
    private void setTabTransitionController(TabTransitionController tabTransitionController) {
        this.tabTransitionController = tabTransitionController;
    }
}
