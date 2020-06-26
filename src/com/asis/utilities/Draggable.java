package com.asis.utilities;

import com.asis.ui.asis_node.SceneNode;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generalised implementation of 'Draggability' of a {@link Node}. The Draggable class is used as a 'namespace' for the internal
 * class/interfaces/enum.
 * @author phill
 *
 */
public class Draggable {
    public enum Event {
        DragStart, Drag, DragEnd
    }

    public interface Listener {
        void accept(Nature draggableNature, Event dragEvent);
    }

    /**
     * Class that encapsulates the draggable nature of a node.
     * <ul>
     * <li>EventNode: the event that receives the drag events</li>
     * <li>One or more DragNodes: that move in response to the drag events. The EventNode is usually (but not always) a
     * DragNode</li>
     * <li>Listeners: listen for the drag events</li>
     * </ul>
     * @author phill
     *
     */
    public static final class Nature implements EventHandler<MouseEvent> {
        private double lastMouseX = 0, lastMouseY = 0; // scene coords

        private boolean dragging = false;

        private final Node eventNode;
        private final List<Node> dragNodes = new ArrayList<>();
        private final List<Listener> dragListeners = new ArrayList<>();

        public Nature(final Node node) {
            this(node, node);
        }

        public Nature(final Node eventNode, final Node... dragNodes) {
            this.eventNode = eventNode;
            this.dragNodes.addAll(Arrays.asList(dragNodes));
            this.eventNode.addEventHandler(MouseEvent.ANY, this);
        }

        public final boolean addListener(final Listener listener) {
            return this.dragListeners.add(listener);
        }

        @Override
        public final void handle(final MouseEvent event) {
            if (MouseEvent.MOUSE_PRESSED == event.getEventType()) {
                if (this.eventNode.contains(event.getX(), event.getY())) {
                    this.lastMouseX = event.getSceneX();
                    this.lastMouseY = event.getSceneY();
                    event.consume();
                }
            } else if (MouseEvent.MOUSE_DRAGGED == event.getEventType()) {
                if (!this.dragging) {
                    this.dragging = true;
                    for (final Listener listener : this.dragListeners) {
                        listener.accept(this, Draggable.Event.DragStart);
                    }
                }
                if (this.dragging) {
                    final double deltaX = event.getSceneX() - this.lastMouseX;
                    final double deltaY = event.getSceneY() - this.lastMouseY;

                    for (final Node dragNode : this.dragNodes) {
                        if(dragNode instanceof SceneNode) {
                            SceneNode draggingScene = (SceneNode) dragNode;

                            final double initialTranslateX = draggingScene.innerX;
                            final double initialTranslateY = draggingScene.innerY;
                            draggingScene.positionInGrid(initialTranslateX+deltaX, initialTranslateY+deltaY);
                            //final double initialTranslateX = dragNode.getTranslateX();
                            //final double initialTranslateY = dragNode.getTranslateY();
                            //dragNode.setTranslateX(initialTranslateX + deltaX);
                            //dragNode.setTranslateY(initialTranslateY + deltaY);
                        }
                    }

                    this.lastMouseX = event.getSceneX();
                    this.lastMouseY = event.getSceneY();

                    event.consume();
                    for (final Listener listener : this.dragListeners) {
                        listener.accept(this, Draggable.Event.Drag);
                    }
                }
            } else if (MouseEvent.MOUSE_RELEASED == event.getEventType()) {
                if (this.dragging) {
                    event.consume();
                    this.dragging = false;
                    for (final Listener listener : this.dragListeners) {
                        listener.accept(this, Draggable.Event.DragEnd);
                    }
                }
            }

        }
    }
}