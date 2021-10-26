package com.asis.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import static com.sun.javafx.util.Utils.clamp;

public class InfinityPane extends StackPane {

    private ContextMenu contextMenu;
    private final Pane container = new Pane();

    private double scale = 1d, minimumScale = 0.5, maximumScale = 2.5;


    public InfinityPane() {
        super();

        addEventFilter(ScrollEvent.ANY, scrollEvent -> {
            scrollEvent.consume();
            zoom(scrollEvent);
        });
        addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> requestFocus());
        addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            contextMenu.hide();
            ((Node) mouseEvent.getSource()).setCursor(Cursor.DEFAULT);
        });
        focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if(!t1) contextMenu.hide();
        });

        container.setStyle("-fx-background-color: transparent");
        container.translateXProperty().addListener((observableValue, number, t1) -> reSetStyle());
        container.translateYProperty().addListener((observableValue, number, t1) -> reSetStyle());

        getChildren().add(container);
        new Pannable(container);

        Platform.runLater(()-> {
            minWidthProperty().bind(getScene().widthProperty());
            minHeightProperty().bind(getScene().heightProperty());
            clipChildren(this);
            reSetStyle();
        });
    }

    private void reSetStyle() {
        double defaultBlockSize = 30;//px Default size of the grid at 100% scale
        double gridSize = Math.ceil(defaultBlockSize * scale);
        double xStart = Math.ceil(container.getTranslateX()+getScene().getWidth()/2);
        double xEnd = xStart + gridSize;
        double yStart = Math.ceil(container.getTranslateY()+getScene().getHeight()/2);
        double yEnd = yStart + gridSize;
        double linePx = 2; // the line thickness in px
        String vLines = "linear-gradient(from "+xStart+"px 0px to " + xEnd + "px 0px , repeat, transparent " + (gridSize - linePx) + "px, #2f2f2f 1px)";
        String hLines = "linear-gradient(from 0px "+yStart+"px to 0px " + yEnd + "px , repeat, transparent " + (gridSize - linePx) + "px , #2f2f2f 1px)";
        Background background = new Background(new BackgroundFill(Paint.valueOf("#393939"), CornerRadii.EMPTY, Insets.EMPTY),
                new BackgroundFill(LinearGradient.valueOf(vLines), CornerRadii.EMPTY, Insets.EMPTY),
                new BackgroundFill(LinearGradient.valueOf(hLines), CornerRadii.EMPTY, Insets.EMPTY));
        setBackground(background);
    }

    private void zoom(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() == 0) return;

        double SCALE_DELTA = 0.1;
        double scaleFactor = (scrollEvent.getDeltaY() > 0) ? SCALE_DELTA : -1 * SCALE_DELTA;

        double nonZoomedXOffset = container.getTranslateX() / scale;
        double nonZoomedYOffset = container.getTranslateY() / scale;

        //Rounding is needed because java will cause floating point errors otherwise
        scale = clamp(minimumScale, Math.round((container.getScaleX() + scaleFactor) * 1000d)/1000d, maximumScale);

        container.setScaleX(scale);
        container.setScaleY(scale);

        container.setTranslateX(nonZoomedXOffset * scale);
        container.setTranslateY(nonZoomedYOffset * scale);

        reSetStyle();
    }

    public void resetPosition() {
        container.setTranslateX(0);
        container.setTranslateY(0);
        scale = 1;
        container.setScaleX(scale);
        container.setScaleY(scale);
        reSetStyle();
    }

    public void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public boolean nodeAtPosition(double x, double y) {
        for (Node n : getContainer().getChildren()) {
            if (n.getUserData() == null) continue;

            if (n.localToScene(n.getBoundsInLocal()).contains(x, y)) return true;
        }

        return false;
    }

    public Pane getContainer() {
        return container;
    }

    private static void clipChildren(Region region) {
        final Rectangle outputClip = new Rectangle(region.getWidth(), region.getHeight());
        outputClip.heightProperty().bind(region.heightProperty());
        outputClip.widthProperty().bind(region.widthProperty());

        region.setClip(outputClip);
    }

    public void setMinimumScale(double minimumScale) {
        this.minimumScale = minimumScale;
    }

    public void setMaximumScale(double maximumScale) {
        this.maximumScale = maximumScale;
    }

    private static class Pannable implements EventHandler<MouseEvent> {
        private double lastMouseX = 0, lastMouseY = 0; // scene coords

        private final Node eventNode;
        private final Node dragNode;

        public Pannable(final Node node) {
            this.eventNode = node.getParent();
            this.dragNode = node;
            this.eventNode.addEventHandler(MouseEvent.ANY, this);
        }

        @Override
        public final void handle(final MouseEvent event) {
            if(!event.isPrimaryButtonDown() && !event.isMiddleButtonDown()) return;

            if (MouseEvent.MOUSE_PRESSED == event.getEventType()) {
                if (this.eventNode.contains(event.getX(), event.getY())) {
                    this.lastMouseX = event.getSceneX();
                    this.lastMouseY = event.getSceneY();
                    event.consume();
                }
            } else if (MouseEvent.MOUSE_DRAGGED == event.getEventType()) {
                ((Node) event.getSource()).setCursor(Cursor.MOVE);

                final double deltaX = (event.getSceneX() - this.lastMouseX);
                final double deltaY = (event.getSceneY() - this.lastMouseY);

                final double initialTranslateX = dragNode.getTranslateX();
                final double initialTranslateY = dragNode.getTranslateY();
                dragNode.setTranslateX(initialTranslateX+deltaX);
                dragNode.setTranslateY(initialTranslateY+deltaY);

                this.lastMouseX = event.getSceneX();
                this.lastMouseY = event.getSceneY();
            }
        }
    }

}
