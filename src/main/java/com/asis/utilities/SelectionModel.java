package com.asis.utilities;

import com.asis.ui.asis_node.JOIComponentNode;
import javafx.scene.Node;

import java.util.HashSet;
import java.util.Set;

public class SelectionModel {
    Set<Node> selection = new HashSet<>();

    public void add(Node node) {
        if(node instanceof JOIComponentNode) {
            ((JOIComponentNode) node).focusState(true);
        }

        selection.add(node);
    }

    public void remove(Node node) {
        selection.remove(node);

        if(node instanceof JOIComponentNode) {
            ((JOIComponentNode) node).focusState(false);
        }
    }

    public void clear() {
        while (!selection.isEmpty()) {
            remove(selection.iterator().next());
        }
    }

    public boolean contains(Node node) {
        return selection.contains(node);
    }

    public int size() {
        return selection.size();
    }
}
