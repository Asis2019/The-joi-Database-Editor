package asis;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    private int sceneId;

    @FXML private TabTimerController tabTimerController;
    @FXML private TabNormalOperationController tabNormalOperationController;
    @FXML private TabTransitionController tabTransitionController;
    @FXML private TabPane effectTabs;
    @FXML private Tab timerTab, transitionTab;
    @FXML private MenuItem menuItemAddTimer, menuItemAddTransition;
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

    void passData(Story story, int sceneId) {
        this.story = story;
        this.sceneId = sceneId;

        if(tabTimerController != null) {
            tabTimerController.passData(story, sceneId);
        }

        if(tabNormalOperationController != null) {
            tabNormalOperationController.passData(story, sceneId);
        }

        if(tabTransitionController != null) {
            tabTransitionController.passData(story, sceneId);
        }

        if(story.hasNoFade(sceneId)) {
            effectTabs.getTabs().remove(2);
            menuItemAddTransition.setDisable(false);
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
            tabTimerController.setVisibleImage();
            tabNormalOperationController.setVisibleImage();
        }
    }

    public void actionAddTimer() {
        try {
            timerTab = new Tab("Timer");
            timerTab.setContent(FXMLLoader.load(this.getClass().getResource("fxml/tab_timer.fxml")));
            timerTab.setOnClosed(event -> actionTimerClosed());

            effectTabs.getTabs().add(1, timerTab);

            menuItemAddTimer.setDisable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionAddTransition() {
        try {
            transitionTab = new Tab("Transition");
            transitionTab.setContent(FXMLLoader.load(this.getClass().getResource("fxml/tab_transition.fxml")));
            transitionTab.setOnClosed(event -> actionTransitionClosed());

            effectTabs.getTabs().add(transitionTab);

            menuItemAddTransition.setDisable(true);

            story.removeDataFromScene(sceneId, "noFade");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionTransitionClosed() {
        menuItemAddTransition.setDisable(false);

        story.removeTransition(sceneId);
        story.addDataToScene(sceneId, "noFade", true);
    }

    public void actionTimerClosed() {
        menuItemAddTimer.setDisable(false);

        story.removeTimer(sceneId);
    }
}
