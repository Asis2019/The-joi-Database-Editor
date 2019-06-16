package asis.custom_objects.asis_node;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineCap;

class BoundLine extends CubicCurve {

    private static final String lineStyle =
            "-fx-stroke: rgb(115, 115, 115);" +
            "-fx-stroke-width: 3px;" +
            "-fx-border-color: black;" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
            "-fx-fill: transparent;";

    BoundLine(ReadOnlyDoubleProperty startX, ReadOnlyDoubleProperty startY) {
        startXProperty().bind(startX);
        startYProperty().bind(startY);
        setStyle(lineStyle);
        setStrokeLineCap(StrokeLineCap.BUTT);
    }
}
