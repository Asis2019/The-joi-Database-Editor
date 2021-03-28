package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.controllers.EditorWindow;
import com.asis.joi.model.entities.JOIComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public abstract class JOIComponentNode extends BorderPane {
    protected final JOIComponent joiComponent;
    private final VBox outputContainer = new VBox();
    private final VBox inputContainer = new VBox();
    protected final Label titleLabel = new Label("Undefined");
    private List<AsisConnectionButton> outputConnections = new ArrayList<>();
    private AsisConnectionButton inputConnection;
    protected ContextMenu contextMenu = new ContextMenu();

    //The controller window this node is attached too
    private EditorWindow editorWindow;

    public double innerX, innerY;

    public JOIComponentNode(int width, int height, int componentId, JOIComponent component, EditorWindow editorWindow) {
        this.joiComponent = component;
        this.joiComponent.setComponentId(componentId);
        setEditorWindow(editorWindow);

        titleLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 25px;" +
                        "-fx-focus-color: blue;"
        );

        setMinSize(width, height);
        setFocusTraversable(true);
        setCenter(titleLabel);

        translateXProperty().addListener((observableValue, number, t1) -> joiComponent.setLayoutXPosition(t1.doubleValue()));
        translateYProperty().addListener((observableValue, number, t1) -> joiComponent.setLayoutYPosition(t1.doubleValue()));

        setDoubleClickAction();
        initializeVBoxes();

        focusState(false);

        titleLabel.textProperty().bindBidirectional(component.componentTitleProperty());

        setOnContextMenuRequested(contextMenuEvent -> {
            contextMenu.hide();
            contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
    }

    public abstract void focusState(boolean value);

    protected abstract boolean openDialog();

    protected void setupContextMenu() {
        MenuItem editSceneItem = new MenuItem("Edit "+getId());
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem deleteNodeItem = new MenuItem("Delete");
        contextMenu.getItems().addAll(editSceneItem, separatorMenuItem, deleteNodeItem);

        //Handle menu actions
        editSceneItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) openDialog();
        });

        deleteNodeItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) removeComponentNode(this);
        });
    }

    private void setDoubleClickAction() {
        setOnMousePressed(mouseEvent -> requestFocus()); //May be unnecessary
        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2)
                openDialog();
        });
    }

    protected void createNewInputConnectionPoint() {
        inputConnection = new AsisConnectionButton(true, getJoiComponent(), getEditorWindow().getInfinityPane().getContainer());
        attachHandlers(inputConnection);
        inputContainer.getChildren().add(inputConnection);
    }

    public AsisConnectionButton createNewOutputConnectionPoint(String labelText, String connectionId) {
        AsisConnectionButton connection = new AsisConnectionButton(false, getJoiComponent(), getEditorWindow().getInfinityPane().getContainer());
        attachHandlers(connection);

        Label connectionLabel = new Label(labelText);
        connectionLabel.setTextFill(Color.WHITE);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);
        hBox.getChildren().addAll(connectionLabel, connection);

        //Add button to list
        outputConnections.add(connection);

        connection.setId(connectionId);

        outputContainer.getChildren().add(hBox);

        return connection;
    }

    public void removeOutputConnection() {
        outputContainer.getChildren().remove(outputConnections.size() - 1);
        outputConnections.remove(outputConnections.size() - 1);
    }

    public void removeAllOutputConnection() {
        for (int i = outputContainer.getChildren().size(); i > 1; i--) {
            outputContainer.getChildren().remove(i - 1);
        }

        for (int i = outputConnections.size(); i > 1; i--) {
            outputConnections.remove(i - 1);
        }
    }

    private void attachHandlers(AsisConnectionButton connection) {
        addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> contextMenu.hide());

        ComponentConnectionManager componentConnectionManager = getEditorWindow().getConnectionManager();

        connection.setOnMouseMoved(componentConnectionManager::mouseMoved);
        connection.setOnMouseDragged(componentConnectionManager::mouseMoved);
        connection.setOnMousePressed(e -> componentConnectionManager.mousePressed(connection));
        connection.setOnMouseReleased(componentConnectionManager::mouseReleased);
    }

    private void initializeVBoxes() {
        outputContainer.setAlignment(Pos.CENTER_RIGHT);
        outputContainer.setSpacing(5);
        outputContainer.setPadding(new Insets(20, 0, 20, 0));
        setRight(outputContainer);

        inputContainer.setAlignment(Pos.CENTER_LEFT);
        setLeft(inputContainer);
    }

    public void positionInGrid(double x, double y) {
        if(Controller.getInstance().isSnapToGrid()) {
            setTranslateX(round(x));
            setTranslateY(round(y));
        } else {
            setTranslateX(x);
            setTranslateY(y);
        }

        innerX = x;
        innerY = y;
    }

    private static double round(double v) {
        final int multiple = 20;
        return multiple * (Math.round(v / multiple));
    }

    public void removeComponentNode(JOIComponentNode joiComponentNode) {
        Controller controller = Controller.getInstance();
        controller.getJoiPackage().getJoi().removeComponent(joiComponentNode.getComponentId());
        getEditorWindow().getConnectionManager().removeConnection(joiComponentNode);
        ((Pane) joiComponentNode.getParent()).getChildren().remove(joiComponentNode);
    }

    //Getters and setters
    public int getComponentId() {
        return this.joiComponent.getComponentId();
    }

    public String getTitle() {
        return titleLabel.getText();
    }
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    void changeOutputConnectionsVisibility(boolean visibility) {
        for (int i = 0; i < outputContainer.getChildren().size(); i++) {
            outputContainer.getChildren().get(i).setDisable(!visibility);
            outputContainer.getChildren().get(i).setVisible(visibility);
        }
    }

    public JOIComponent getJoiComponent() {
        return this.joiComponent;
    }

    public List<AsisConnectionButton> getOutputButtons() {
        return this.outputConnections;
    }
    public AsisConnectionButton getInputConnection() {
        return this.inputConnection;
    }

    public EditorWindow getEditorWindow() {
        return editorWindow;
    }
    public void setEditorWindow(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
    }
}
