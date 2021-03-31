package com.asis.ui.asis_node.node_functional_expansion;

import com.asis.controllers.EditorWindow;
import com.asis.joi.model.entities.*;
import com.asis.ui.asis_node.ArithmeticNode;
import com.asis.ui.asis_node.ConditionNode;
import com.asis.ui.asis_node.SceneNode;
import com.asis.ui.asis_node.VariableSetterNode;
import com.asis.ui.asis_node.node_group.NodeGroup;
import com.asis.ui.asis_node.node_group.NodeGroupBridge;

/**
 * This class is used to tell the ComponentNodeManager how to create the different types of components.
 * It's a polymorphic way of doing if else with instance of.
 */
public class AddComponentNodeResolver implements ComponentVisitor {

    private final EditorWindow editorWindow;

    public AddComponentNodeResolver(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
    }

    @Override
    public void visit(Scene scene) {
        editorWindow.getNodeManager().addJOIComponentNode(
                SceneNode.class, Scene.class,
                scene.getLayoutXPosition(), scene.getLayoutYPosition(), scene.getComponentTitle(),
                scene.getComponentId(), true);
    }

    @Override
    public void visit(Condition condition) {
        editorWindow.getNodeManager().addJOIComponentNode(
                ConditionNode.class, Condition.class,
                condition.getLayoutXPosition(), condition.getLayoutYPosition(), condition.getComponentTitle(),
                condition.getComponentId(), true);
    }

    @Override
    public void visit(VariableSetter variableSetter) {
        editorWindow.getNodeManager().addJOIComponentNode(
                VariableSetterNode.class, VariableSetter.class,
                variableSetter.getLayoutXPosition(), variableSetter.getLayoutYPosition(), variableSetter.getComponentTitle(),
                variableSetter.getComponentId(), true);
    }

    @Override
    public void visit(Arithmetic arithmetic) {
        editorWindow.getNodeManager().addJOIComponentNode(
                ArithmeticNode.class, Arithmetic.class,
                arithmetic.getLayoutXPosition(), arithmetic.getLayoutYPosition(), arithmetic.getComponentTitle(),
                arithmetic.getComponentId(), true);
    }

    @Override
    public void visit(Group group) {
        editorWindow.getNodeManager().addJOIComponentNode(
                NodeGroup.class, Group.class,
                group.getLayoutXPosition(), group.getLayoutYPosition(), group.getComponentTitle(),
                group.getComponentId(), true);
    }

    @Override
    public void visit(GroupBridge groupBridge) {
        editorWindow.getNodeManager().addJOIComponentNode(
                NodeGroupBridge.class, GroupBridge.class,
                groupBridge.getLayoutXPosition(), groupBridge.getLayoutYPosition(), groupBridge.getComponentTitle(),
                groupBridge.getComponentId(), true);
    }
}
