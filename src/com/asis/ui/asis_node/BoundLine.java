package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;

import static com.asis.utilities.AsisUtils.hackTooltipStartTiming;

public class BoundLine extends CubicCurve {

    private AsisConnectionButton startPointConnectionObject, endPointConnectionObject;

    private static final String lineStyle =
            "-fx-stroke: rgb(115, 115, 115);" +
                    "-fx-stroke-width: 3px;" +
                    "-fx-border-color: black;" +
                    "-fx-border-width: 1px;" +
                    "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
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
        setStyle(lineStyle);
        setStrokeLineCap(StrokeLineCap.BUTT);
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

        Controller controller = Controller.getInstance();
        ArrayList<JOIComponentNode> joiComponentNodes = controller.getJoiComponentNodes();


        //TODO is unable to handle multiple lines connected to the same point
        String tooltipText = controller.getJOIComponentNodeWithId(joiComponentNodes, getEndPointConnectionObject().getParentSceneId()).getTitle();

        Tooltip tooltip = new Tooltip(String.valueOf(tooltipText));
        hackTooltipStartTiming(tooltip);
        tooltip.getScene().cursorProperty().bind(getStartPointConnectionObject().cursorProperty());
        getStartPointConnectionObject().setTooltip(tooltip);

        String tooltip2Text = controller.getJOIComponentNodeWithId(joiComponentNodes, getStartPointConnectionObject().getParentSceneId()).getTitle();

        Tooltip tooltip2 = new Tooltip(tooltip2Text);
        hackTooltipStartTiming(tooltip2);
        tooltip2.getScene().cursorProperty().bind(getEndPointConnectionObject().cursorProperty());
        getEndPointConnectionObject().setTooltip(tooltip2);
    }


    public void unbindEnd() {
        endXProperty().unbind();
        endYProperty().unbind();

        if(getStartPointConnectionObject() != null) getStartPointConnectionObject().setTooltip(null);
        if(getEndPointConnectionObject() != null) getEndPointConnectionObject().setTooltip(null);
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
