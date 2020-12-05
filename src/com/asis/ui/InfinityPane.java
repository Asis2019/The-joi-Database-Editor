package com.asis.ui;

import com.asis.controllers.Controller;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import static com.asis.utilities.AsisUtils.clamp;

public class InfinityPane extends StackPane {

    private ContextMenu contextMenu;
    private final Pane container = new Pane();

    private double scale = 1d;

    public InfinityPane() {
        super();

        addEventFilter(ScrollEvent.ANY, scrollEvent -> {
            scrollEvent.consume();
            zoom(scrollEvent);
        });
        setOnScroll(scrollEvent -> {
            scrollEvent.consume();
            zoom(scrollEvent);
        });

        container.setStyle("-fx-background-color: transparent");
        container.translateXProperty().addListener((observableValue, number, t1) -> reSetStyle());
        container.translateYProperty().addListener((observableValue, number, t1) -> reSetStyle());

        getChildren().add(container);
        new Pannable(container);

        addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            requestFocus();

            if(mouseEvent.isSecondaryButtonDown()) {
                contextMenu.hide();
                contextMenu.show(this, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            } else {
                contextMenu.hide();
            }
        });
        focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if(!t1) contextMenu.hide();
        });

        Platform.runLater(()-> {
            minWidthProperty().bind(getScene().widthProperty());
            minHeightProperty().bind(getScene().heightProperty());

            clipChildren(this);

            getScene().addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                if(keyEvent.getCode() == KeyCode.R) {
                    container.setTranslateX(0);
                    container.setTranslateY(0);
                    scale = 1;
                    container.setScaleX(scale);
                    container.setScaleY(scale);
                    reSetStyle();
                }
            });

            reSetStyle();
        });
    }

    private void reSetStyle() {
        double startPointX = container.getTranslateX()+(getScene().getWidth()/2)+(0.5*scale);
        double endPointX = container.getTranslateX()+(getScene().getWidth()/2)+(10.5*scale);
        double zeroX = container.getTranslateX()+(getScene().getWidth()/2)*scale;

        startPointX = Math.round(startPointX * 1000d)/1000d;
        endPointX = Math.round(endPointX * 1000d)/1000d;
        zeroX = Math.round(zeroX * 1000d)/1000d;

        double startPointY = container.getTranslateY()+(getScene().getHeight()/2)+(0.5*scale);
        double endPointY = container.getTranslateY()+(getScene().getHeight()/2)+(10.5*scale);
        double zeroY = container.getTranslateY()+(getScene().getHeight()/2)*scale;

        startPointY = Math.round(startPointY * 1000d)/1000d;
        endPointY = Math.round(endPointY * 1000d)/1000d;
        zeroY = Math.round(zeroY * 1000d)/1000d;

        setStyle("-fx-background-color: #393939," +
                "linear-gradient(from "+startPointX+"px "+zeroX+"px to "+endPointX+"px "+zeroX+"px, repeat, #2f2f2f 5%, transparent 6%)," +
                "linear-gradient(from "+zeroY+"px "+startPointY+"px to "+zeroY+"px "+endPointY+"px, repeat, #2f2f2f 5%, transparent 6%);");
    }

    private void zoom(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() == 0) return;

        double SCALE_DELTA = 0.1;
        double scaleFactor = (scrollEvent.getDeltaY() > 0) ? SCALE_DELTA : -1 * SCALE_DELTA;

        double nonZoomedXOffset = container.getTranslateX() / scale;
        double nonZoomedYOffset = container.getTranslateY() / scale;

        //Rounding is needed because java will cause floating point errors otherwise
        scale = clamp(Math.round((container.getScaleX() + scaleFactor) * 1000d)/1000d, 0.5, 2.5);

        container.setScaleX(scale);
        container.setScaleY(scale);

        container.setTranslateX(nonZoomedXOffset * scale);
        container.setTranslateY(nonZoomedYOffset * scale);

        reSetStyle();
    }

    public void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public Point2D sceneToWorld(double sceneX, double sceneY) {
        Controller controller = Controller.getInstance();
        final double menuBarOffset = controller.mainMenuBar.getHeight() + controller.toolBar.getHeight();

        double localToSceneTranslateX = getContainer().getLocalToSceneTransform().getTx();
        double localToSceneTranslateY = getContainer().getLocalToSceneTransform().getTy();

        double offsetX = localToSceneTranslateX / scale - getContainer().getTranslateX();
        double offsetY = localToSceneTranslateY / scale - menuBarOffset - getContainer().getTranslateY();

        double lowestXPixelShown = -1 * getContainer().getTranslateX();
        double lowestYPixelShown = -1 * getContainer().getTranslateY();

        return new Point2D((lowestXPixelShown + sceneX / scale) - offsetX, (lowestYPixelShown + sceneY / scale) - offsetY);
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

    private static class Pannable implements EventHandler<MouseEvent> {
        private double lastMouseX = 0, lastMouseY = 0; // scene coords

        private boolean dragging = false;

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
                if (!this.dragging) this.dragging = true;

                final double deltaX = (event.getSceneX() - this.lastMouseX);
                final double deltaY = (event.getSceneY() - this.lastMouseY);

                final double initialTranslateX = dragNode.getTranslateX();
                final double initialTranslateY = dragNode.getTranslateY();
                dragNode.setTranslateX(initialTranslateX+deltaX);
                dragNode.setTranslateY(initialTranslateY+deltaY);

                this.lastMouseX = event.getSceneX();
                this.lastMouseY = event.getSceneY();

                event.consume();
            } else if (MouseEvent.MOUSE_RELEASED == event.getEventType()) {
                if (this.dragging) {
                    event.consume();
                    this.dragging = false;
                }
            }

        }
    }

}
