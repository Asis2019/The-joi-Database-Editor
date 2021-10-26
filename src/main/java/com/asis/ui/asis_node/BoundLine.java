package com.asis.ui.asis_node;

import javafx.beans.binding.Bindings;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineCap;

public class BoundLine extends CubicCurve {

    private AsisConnectionButton startPointConnectionObject, endPointConnectionObject;

    private static final String lineStyle =
            "-fx-stroke: rgb(115, 115, 115);" +
                    "-fx-stroke-width: 3px;" +
                    "-fx-border-color: black;" +
                    "-fx-border-width: 1px;" +
                    "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                    "-fx-fill: transparent;";
    private static final String lineStyleHover =
            "-fx-stroke: rgb(115, 115, 115);" +
                    "-fx-stroke-width: 3px;" +
                    "-fx-border-color: black;" +
                    "-fx-border-width: 1px;" +
                    "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                    "-fx-fill: transparent;";

    BoundLine(AsisConnectionButton startPointConnectionObject) {
        setStartPointConnectionObject(startPointConnectionObject);

        configureLineCurve();
    }

    BoundLine(AsisConnectionButton startPointConnectionObject, AsisConnectionButton endPointConnectionObject) {
        setStartPointConnectionObject(startPointConnectionObject);
        setEndPointConnectionObject(endPointConnectionObject);

        configureLineCurve();
    }

    private void configureLineCurve() {
        hoverProperty().addListener((observableValue, aBoolean, t1) -> {
            if(t1) setStyle(lineStyleHover);
            else setStyle(lineStyle);
        });

        setStyle(lineStyle);
        setStrokeLineCap(StrokeLineCap.ROUND);
        setCache(false);
        controlX1Property().bind(Bindings.add(startXProperty(), 100));
        controlX2Property().bind(Bindings.add(endXProperty(), -100));
        controlY1Property().bind(Bindings.add(startYProperty(), 0));
        controlY2Property().bind(Bindings.add(endYProperty(), 0));
    }

    private void bindStart() {
        startXProperty().bind(getStartPointConnectionObject().centerXProperty());
        startYProperty().bind(getStartPointConnectionObject().centerYProperty());
    }

    private void bindEnd() {
        endXProperty().bind(getEndPointConnectionObject().centerXProperty());
        endYProperty().bind(getEndPointConnectionObject().centerYProperty());
    }


    public void unbindEnd() {
        endXProperty().unbind();
        endYProperty().unbind();
    }

    public void setStartPointConnectionObject(AsisConnectionButton startPointConnectionObject) {
        this.startPointConnectionObject = startPointConnectionObject;
        bindStart();
    }

    public void setEndPointConnectionObject(AsisConnectionButton endPointConnectionObject) {
        this.endPointConnectionObject = endPointConnectionObject;
        bindEnd();
    }

    public AsisConnectionButton getStartPointConnectionObject() {
        return this.startPointConnectionObject;
    }

    public AsisConnectionButton getEndPointConnectionObject() {
        return this.endPointConnectionObject;
    }
}
