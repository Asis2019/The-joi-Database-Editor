package com.asis.ui.asis_node;

import com.asis.joi.model.entities.Scene;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public class SceneNode extends JOIComponentNode {
    private final ReadOnlyBooleanWrapper isBadEnd = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper isGoodEnd = new ReadOnlyBooleanWrapper();

    public SceneNode(int width, int height, int sceneId, SceneNodeMainController sceneNodeMainController, Scene scene) {
        super(width, height, sceneId, sceneNodeMainController, scene);

        setUserData("sceneNode");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint("Default", "normal_output");

        initializeEndVariables();
    }

    private void initializeEndVariables() {
        isGoodEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isGoodEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                getInputConnection().setButtonColor(AsisConnectionButton.GOOD_END_COLOR);
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                getInputConnection().setButtonColor(AsisConnectionButton.DEFAULT_COLOR);
            }
        });

        isBadEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isBadEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                getInputConnection().setButtonColor(AsisConnectionButton.BAD_END_COLOR);
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                getInputConnection().setButtonColor(AsisConnectionButton.DEFAULT_COLOR);
            }
        });

        isGoodEndProperty().bindBidirectional(getJOIScene().goodEndProperty());
        isBadEndProperty().bindBidirectional(getJOIScene().badEndProperty());
    }

    protected void focusState(boolean value) {
        if (value) {
            setStyle(
                    "-fx-background-color: #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8;" +
                            "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle(
                    "-fx-background-color: #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8;" +
                            "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }

    //Getters and setters
    public Scene getJOIScene() {
        return (Scene) joiComponent;
    }

    public boolean isBadEnd() {
        return isBadEnd.get();
    }

    public ReadOnlyBooleanWrapper isBadEndProperty() {
        return isBadEnd;
    }

    public void setIsBadEnd(boolean isBadEnd) {
        this.isBadEnd.set(isBadEnd);
    }

    public boolean isGoodEnd() {
        return isGoodEnd.get();
    }

    public ReadOnlyBooleanWrapper isGoodEndProperty() {
        return isGoodEnd;
    }

    public void setIsGoodEnd(boolean isGoodEnd) {
        this.isGoodEnd.set(isGoodEnd);
    }
}
