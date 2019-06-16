package asis.custom_objects;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;

public class AsisCenteredArc {
    private static final double INSET = 10;

    private static final double ARC_RADIUS = 50;
    private static final double INITIAL_ARC_LENGTH  = 360;
    private static final double ARC_STROKE_WIDTH = 10;
    private static final double ARC_REGION_SIZE = ARC_RADIUS * 2 + ARC_STROKE_WIDTH + INSET * 2;
    private double maxLength = 100;
    private double currentProgress = 100;

    private final Arc arc;
    private final Pane arcPane;
    private final Label progressLabel;

    public AsisCenteredArc() {
        // Create the arc.
        arc = new Arc(
                ARC_REGION_SIZE / 2, ARC_REGION_SIZE / 2,
                ARC_RADIUS, ARC_RADIUS,
                0,
                INITIAL_ARC_LENGTH
        );

        arc.setStartAngle(90);
        arc.setType(ArcType.OPEN);
        arc.setStrokeWidth(10);
        arc.setStroke(Color.FORESTGREEN);
        arc.setFill(null);

        //Create the label
        progressLabel = new Label(String.valueOf((int) currentProgress));
        progressLabel.getStyleClass().add("asis-progress-label");

        final double fillSize = ARC_RADIUS * 2 + arc.getStrokeWidth() + INSET * 2;
        Rectangle fill = new Rectangle(fillSize, fillSize, Color.TRANSPARENT);

        Group centeredArcGroup = new Group(fill, arc);

        arcPane = new StackPane();
        StackPane.setAlignment(arcPane, Pos.TOP_LEFT);
        arcPane.setMinSize(0, 0);
        arcPane.getChildren().addAll(centeredArcGroup, progressLabel);
    }

    public double getArcStrokeWidth() {
        return this.arc.getStrokeWidth();
    }

    public void setArcStrokeWidth(double width) {
        this.arc.setStrokeWidth(width);
    }

    public Paint getArcStrokeColor() {
        return this.arc.getStroke();
    }

    public void setArcStrokeColor(Paint color) {
        this.arc.setStroke(color);
    }

    public void setMaxLength(double maxLength) {
        double multiplicationKey = 360/maxLength;
        this.arc.setLength(multiplicationKey*currentProgress);
        this.maxLength = maxLength;
    }

    public double getMaxLength() {
        return this.maxLength;
    }

    public double getArcProgress() {
        return this.currentProgress;
    }

    public void setArcProgress(double progress) {
        currentProgress = progress;
        progressLabel.setText(String.valueOf((int) currentProgress));
        double multiplicationKey = 360/maxLength;
        this.arc.setLength(multiplicationKey*progress);
    }

    public Arc getArc() {
        return arc;
    }

    public Pane getArcPane() {
        return arcPane;
    }
}