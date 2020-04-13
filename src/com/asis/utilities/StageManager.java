package com.asis.utilities;

import javafx.stage.Stage;

import java.util.ArrayList;

public class StageManager {

    private static final StageManager stageManager = new StageManager();
    private final ArrayList<Stage> openStages = new ArrayList<>();

    private StageManager() {

    }

    public static StageManager getInstance() {
        return stageManager;
    }

    public void closeStage(Stage stage) {
        getOpenStages().remove(stage);
        stage.close();
    }

    public void openStage(Stage stage) {
        stage.setOnCloseRequest(windowEvent -> getOpenStages().remove(stage));
        getOpenStages().add(stage);
        stage.show();
    }

    public boolean requestStageFocus(Object stageUserData) {
        for(Stage stage: getOpenStages()) {
            if(stage.getUserData().equals(stageUserData)) {
                stage.setIconified(false);
                stage.requestFocus();
                return true;
            }
        }
        return false;
    }

    public void closeAllStages() {
        for(Stage stage: getOpenStages()) stage.close();
        getOpenStages().clear();
    }

    //Getters and setters
    public ArrayList<Stage> getOpenStages() {
        return openStages;
    }
}
