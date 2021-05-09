package com.asis.ui;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import static java.lang.Math.abs;
import static java.lang.Math.max;


public class ResizableRectangle extends Rectangle {

    private double rectangleStartX;
    private double rectangleStartY;
    private double mouseClickPozX;
    private double mouseClickPozY;
    private static final double RESIZER_SQUARE_SIDE = 4;
    private final Paint resizerSquareColor = Color.WHITE;

    public ResizableRectangle(double x, double y, double width, double height, Group group) {
        super(x, y, width, height);
        group.getChildren().add(this);
        Paint rectangleStrokeColor = Color.BLACK;
        super.setStroke(rectangleStrokeColor);
        super.setStrokeWidth(1);
        super.setFill(Color.color(1, 1, 1, 0));

        Rectangle moveRect = new Rectangle(0, 0, 0, 0);
        moveRect.setFill(Color.color(1, 1, 1, 0));
        moveRect.xProperty().bind(super.xProperty());
        moveRect.yProperty().bind(super.yProperty());
        moveRect.widthProperty().bind(super.widthProperty());
        moveRect.heightProperty().bind(super.heightProperty());

        group.getChildren().add(moveRect);

        moveRect.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> moveRect.getParent().setCursor(Cursor.HAND));
        moveRect.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            moveRect.getParent().setCursor(Cursor.MOVE);
            mouseClickPozX = event.getX();
            mouseClickPozY = event.getY();
        });

        moveRect.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> moveRect.getParent().setCursor(Cursor.HAND));
        moveRect.addEventHandler(MouseEvent.MOUSE_EXITED, event -> moveRect.getParent().setCursor(Cursor.DEFAULT));
        moveRect.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double offsetX = event.getX() - mouseClickPozX;
            double offsetY = event.getY() - mouseClickPozY;

            double newX = super.getX() + offsetX;
            double newY = super.getY() + offsetY;

            if (newX >= 0 && newX + super.getWidth() <= super.getParent().getChildrenUnmodifiable().get(0).getBoundsInLocal().getWidth()) {
                super.setX(newX);
            } else if (newX + super.getWidth() > super.getParent().getChildrenUnmodifiable().get(0).getBoundsInLocal().getWidth()) {
                super.setX(super.getParent().getChildrenUnmodifiable().get(0).getBoundsInLocal().getWidth() - super.getWidth());
            }

            if (newY >= 0 && newY + super.getHeight() <= super.getParent().getChildrenUnmodifiable().get(0).getBoundsInLocal().getHeight()) {
                super.setY(newY);
            } else if (newY + super.getHeight() > super.getParent().getChildrenUnmodifiable().get(0).getBoundsInLocal().getHeight()) {
                super.setY(super.getParent().getChildrenUnmodifiable().get(0).getBoundsInLocal().getHeight() - super.getHeight());
            }

            mouseClickPozX = event.getX();
            mouseClickPozY = event.getY();
        });

        makeSEResizerSquare(group);
    }

    private void makeSEResizerSquare(Group group) {
        Rectangle squareSE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareSE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(
                squareSE.widthProperty().divide(2.0)));
        squareSE.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(
                squareSE.heightProperty().divide(2.0))));
        group.getChildren().add(squareSE);

        squareSE.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareSE.getParent().setCursor(Cursor.SE_RESIZE));

        prepareResizerSquare(squareSE);

        squareSE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartX = super.getX();
            rectangleStartY = super.getY();

            double offsetX = abs(event.getX() - rectangleStartX);
            double offsetY = abs(event.getY() - rectangleStartY);

            double size = max(offsetX, offsetY);

            if (offsetX >= 0 && offsetX <= super.getX() + super.getWidth() - 5) {
                super.setWidth(size);
            }

            if (offsetY >= 0 && offsetY <= super.getY() + super.getHeight() - 5) {
                super.setHeight(size);
            }
        });
    }

    private void prepareResizerSquare(Rectangle rect) {
        rect.setFill(resizerSquareColor);
        rect.addEventHandler(MouseEvent.MOUSE_EXITED, event -> rect.getParent().setCursor(Cursor.DEFAULT));
    }

}