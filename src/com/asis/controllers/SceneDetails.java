package com.asis.controllers;

import com.asis.controllers.dialogs.DialogConfirmation;
import com.asis.controllers.tabs.*;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
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

    @FXML
    private TabPane effectTabs;
    @FXML
    private MenuItem menuItemAddTimer, menuItemAddTransition, menuItemAddDialog, menuItemAddNormalOperation;
    @FXML
    private BorderPane sceneDetailBorderPane;

    public void initialize(Scene scene) {
        setScene(scene);

        //--Load appropriate tabs for current scene
        //add normal operation tab (line tab) to sceneDetails
        if (getScene().hasComponent(LineGroup.class))
            addLineTab(getScene().getComponent(LineGroup.class));

        //add timer tab to sceneDetails
        if (getScene().hasComponent(Timer.class))
            addTimerTab(getScene().getComponent(Timer.class));

        //add dialog tab to sceneDetails
        if (getScene().hasComponent(Dialog.class))
            addDialogTab(getScene().getComponent(Dialog.class));

        //add transition tab to sceneDetails
        if (getScene().hasComponent(Transition.class))
            addTransitionTab(getScene().getComponent(Transition.class));

        Platform.runLater(this::addListenerToEffectTab);
    }

    private void addListenerToEffectTab() {
        getEffectTabs().getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            if(newTab == null) return;

            //This is done so that if a scene image is added in one tab, it is synced with the others
            switch (newTab.getText()) {
                case "Normal Operation":
                    if (getTabNormalOperationController() != null) getTabNormalOperationController().setVisibleImage();
                    break;

                case "Timer":
                    if (getTabTimerController() != null) getTabTimerController().setVisibleImage();
                    break;
            }
        });
    }

    private void addLineTab(LineGroup lineGroup) {
        try {
            TabNormalOperationController tabNormalOperationController = new TabNormalOperationController("Normal Operation", lineGroup);
            setTabNormalOperationController(tabNormalOperationController);
            createNewTab(tabNormalOperationController, "/resources/fxml/tab_normal_operation.fxml", 0);
            menuItemAddNormalOperation.setDisable(true);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
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

        if (indexPosition.length > 0) {
            if(getEffectTabs().getTabs().size() >= 1)
                getEffectTabs().getTabs().add(indexPosition[0], newTab);
            else
                getEffectTabs().getTabs().add(newTab);
        } else {
            getEffectTabs().getTabs().add(newTab);
        }
    }

    private void handleOnTabCloseRequested(Event event) {
        final Tab toCloseTab = ((Tab) event.getSource());

        switch (toCloseTab.getText()) {
            case "Normal Operation":
                if (!getTabNormalOperationController().getLineGroup().equals(new LineGroup())) {
                    if (!DialogConfirmation.show("Delete Line", "Are you sure you want to remove the text in your scene?")) {
                        event.consume();
                        return;
                    }
                }
                closeLine();
                break;

            case "Timer":
                if (!getTabTimerController().getTimer().equals(new Timer())) {
                    if (!DialogConfirmation.show("Delete Timer", "Are you sure you want to remove the timer?")) {
                        event.consume();
                        return;
                    }
                }
                closeTimer();
                break;

            case "Transition":
                if (!getTabTransitionController().getTransition().equals(new Transition())) {
                    if (!DialogConfirmation.show("Delete Transition", "Are you sure you want to remove the transition?")) {
                        event.consume();
                        return;
                    }
                }
                closeTransition();
                break;

            case "Dialog":
                if (!getTabDialogOptionController().getDialog().equals(new Dialog())) {
                    if (!DialogConfirmation.show("Delete Dialogs", "Are you sure you want to remove dialogs?")) {
                        event.consume();
                        return;
                    }
                }
                closeDialog();
                break;
        }

        //toCloseTab.getTabPane().getTabs().remove(toCloseTab);
    }

    private void closeLine() {
        menuItemAddNormalOperation.setDisable(false);
        setTabNormalOperationController(null);
        getScene().removeComponent(LineGroup.class);
    }

    private void closeTransition() {
        menuItemAddTransition.setDisable(false);
        setTabTransitionController(null);
        getScene().removeComponent(Transition.class);
    }

    private void closeDialog() {
        menuItemAddDialog.setDisable(false);
        Controller.getInstance().getJOIComponentNodeWithId(Controller.getInstance().getJoiComponentNodes(), getScene().getComponentId()).removeAllOutputConnection();
        setTabDialogOptionController(null);
        getScene().removeComponent(Dialog.class);
    }

    private void closeTimer() {
        menuItemAddTimer.setDisable(false);

        setTabTimerController(null);
        getScene().removeComponent(Timer.class);
    }

    @FXML
    private void actionClose() {
        Stage stage = (Stage) sceneDetailBorderPane.getScene().getWindow();
        StageManager.getInstance().closeStage(stage);
    }

    @FXML
    private void actionChangeSceneImage() {
        File file = AsisUtils.imageFileChooser();

        if (file != null) {
            //Add image to json object
            if (getScene().hasComponent(SceneImage.class)) getScene().removeComponent(SceneImage.class);
            SceneImage sceneImage = new SceneImage();
            sceneImage.setImage(file);
            getScene().addComponent(sceneImage);

            //setVisibleImage();
            if (getTabNormalOperationController() != null) {
                getTabNormalOperationController().setVisibleImage();
            }

            if (getTabTimerController() != null) {
                getTabTimerController().setVisibleImage();
            }
        }
    }

    @FXML
    private void menuItemAddNormalOperation() {
        LineGroup lineGroup = new LineGroup();
        getScene().addComponent(lineGroup);

        addLineTab(lineGroup);
    }

    @FXML
    private void menuItemAddTimer() {
        Timer timer = new Timer();
        getScene().addComponent(timer);

        addTimerTab(timer);
    }

    @FXML
    private void menuItemAddTransition() {
        Transition transition = new Transition();
        getScene().addComponent(transition);

        addTransitionTab(transition);
    }

    @FXML
    private void menuItemAddDialog() {
        Dialog dialog = new Dialog();
        getScene().addComponent(dialog);

        addDialogTab(dialog);
    }

    @FXML
    private void menuItemAmbience() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("ogg", "*.ogg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) getScene().setAmbience(file.getName());
        else getScene().setAmbience(null);
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
