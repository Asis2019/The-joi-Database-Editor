package com.asis.controllers.dialogs;

import com.asis.controllers.Controller;
import com.asis.ui.ImageViewPane;
import com.asis.ui.ResizableRectangle;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static java.lang.Math.max;

public class DialogCropImage {

    @FXML
    private Group selectionGroup;

    private ImageViewPane mainImageViewPane;
    private final ImageView mainImageView = new ImageView();
    private final AreaSelection areaSelection = new AreaSelection();
    private static Image finalImage;

    public void initialize() {
        Platform.runLater(() -> {
            mainImageViewPane = new ImageViewPane();
            mainImageViewPane.setImageView(mainImageView);
            mainImageView.setPreserveRatio(true);
            mainImageViewPane.minWidthProperty().bind(selectionGroup.getScene().widthProperty());
            mainImageViewPane.minHeightProperty().bind(selectionGroup.getScene().heightProperty());

            selectionGroup.getChildren().add(mainImageViewPane);

            mainImageView.setImage(finalImage);

            areaSelection.selectArea(selectionGroup);
        });
    }

    private Image scale(Image source) {
        ImageView imageView = new ImageView(source);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(300);
        imageView.setFitHeight(300);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        return imageView.snapshot(parameters, null);
    }

    private void cropImage(Bounds bounds, ImageView imageView) {
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), width, height));

        WritableImage wi = new WritableImage((int) width, (int) height);
        finalImage = scale(imageView.snapshot(parameters, wi));
    }

    private void clearSelection(Group group) {
        group.getChildren().remove(1, group.getChildren().size());
    }

    public void actionSaveSelection() {
        try {
            cropImage(areaSelection.selectArea(selectionGroup).getBoundsInParent(), mainImageView);

            Stage stage = (Stage) selectionGroup.getScene().getWindow();
            stage.close();
        } catch (NullPointerException e) {
            DialogMessage.messageDialog("WARNING", "Please create a selection by clicking and dragging",
                    300, 150);
        }
    }

    public static Image openImageCrop(File image) {
        try {

            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(DialogCropImage.class.getResource("/fxml/dialog_crop_image.fxml"));
            Parent root = fxmlLoader.load();

            Scene main_scene = new Scene(root);

            stage.setResizable(true);
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/images/icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(main_scene);
            stage.setTitle("Crop Image");

            finalImage = new Image(new FileInputStream(image));

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return finalImage;
    }

    private class AreaSelection {

        private Group group;

        private ResizableRectangle selectionRectangle = null;
        private double rectangleStartX;
        private double rectangleStartY;
        private final Paint darkAreaColor = Color.color(0, 0, 0, 0.5);

        private ResizableRectangle selectArea(Group group) {
            this.group = group;

            if (finalImage != null) {
                this.group.getChildren().get(0).addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
                this.group.getChildren().get(0).addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            }

            return selectionRectangle;
        }

        EventHandler<MouseEvent> onMousePressedEventHandler = event -> {
            if (event.isSecondaryButtonDown()) return;

            rectangleStartX = event.getX();
            rectangleStartY = event.getY();

            clearSelection(group);

            selectionRectangle = new ResizableRectangle(rectangleStartX, rectangleStartY, 0, 0, group);
            darkenOutsideRectangle(selectionRectangle);
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {
            if (event.isSecondaryButtonDown()) return;

            double offsetX = event.getX() - rectangleStartX;
            double offsetY = event.getY() - rectangleStartY;

            double size = max(offsetX, offsetY);

            if (size > 0) {
                selectionRectangle.setWidth(size);
                selectionRectangle.setHeight(size);
            }
        };

        private void darkenOutsideRectangle(Rectangle rectangle) {
            Rectangle darkAreaTop = new Rectangle(0, 0, darkAreaColor);
            Rectangle darkAreaLeft = new Rectangle(0, 0, darkAreaColor);
            Rectangle darkAreaRight = new Rectangle(0, 0, darkAreaColor);
            Rectangle darkAreaBottom = new Rectangle(0, 0, darkAreaColor);

            darkAreaTop.widthProperty().bind(selectionGroup.getScene().widthProperty());
            darkAreaTop.heightProperty().bind(rectangle.yProperty());

            darkAreaLeft.yProperty().bind(rectangle.yProperty());
            darkAreaLeft.widthProperty().bind(rectangle.xProperty());
            darkAreaLeft.heightProperty().bind(rectangle.heightProperty());

            darkAreaRight.xProperty().bind(rectangle.xProperty().add(rectangle.widthProperty()));
            darkAreaRight.yProperty().bind(rectangle.yProperty());
            darkAreaRight.widthProperty().bind(selectionGroup.getScene().widthProperty().subtract(
                    rectangle.xProperty().add(rectangle.widthProperty())));
            darkAreaRight.heightProperty().bind(rectangle.heightProperty());

            darkAreaBottom.yProperty().bind(rectangle.yProperty().add(rectangle.heightProperty()));
            darkAreaBottom.widthProperty().bind(selectionGroup.getScene().widthProperty());
            darkAreaBottom.heightProperty().bind(selectionGroup.getScene().heightProperty().subtract(
                    rectangle.yProperty().add(rectangle.heightProperty())));

            // adding dark area rectangles before the selectionRectangle. So it can't overlap rectangle
            group.getChildren().add(1, darkAreaTop);
            group.getChildren().add(1, darkAreaLeft);
            group.getChildren().add(1, darkAreaBottom);
            group.getChildren().add(1, darkAreaRight);

            // make dark area container layer as well
            darkAreaTop.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            darkAreaTop.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);

            darkAreaLeft.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            darkAreaLeft.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);

            darkAreaRight.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            darkAreaRight.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);

            darkAreaBottom.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            darkAreaBottom.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        }
    }
}
