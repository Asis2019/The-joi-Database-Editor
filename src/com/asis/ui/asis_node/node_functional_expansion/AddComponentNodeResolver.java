package com.asis.ui.asis_node.node_functional_expansion;

import com.asis.joi.model.entities.Arithmetic;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.ui.asis_node.*;

/**
 * This class is used to tell the ComponentNodeManager how to create the different types of components.
 * It's a polymorphic way of doing if else with instance of.
 */
public class AddComponentNodeResolver implements ComponentVisitor {

    @Override
    public void visit(Scene scene) {
        ComponentNodeManager.getInstance().addJOIComponentNode(
                SceneNode.class, com.asis.joi.model.entities.Scene.class,
                scene.getLayoutXPosition(), scene.getLayoutYPosition(), scene.getComponentTitle(),
                scene.getComponentId(), true);
    }

    @Override
    public void visit(Condition condition) {
        ComponentNodeManager.getInstance().addJOIComponentNode(
                ConditionNode.class, Condition.class,
                condition.getLayoutXPosition(), condition.getLayoutYPosition(), condition.getComponentTitle(),
                condition.getComponentId(), true);
    }

    @Override
    public void visit(VariableSetter variableSetter) {
        ComponentNodeManager.getInstance().addJOIComponentNode(
                VariableSetterNode.class, VariableSetter.class,
                variableSetter.getLayoutXPosition(), variableSetter.getLayoutYPosition(), variableSetter.getComponentTitle(),
                variableSetter.getComponentId(), true);
    }

    @Override
    public void visit(Arithmetic arithmetic) {
        ComponentNodeManager.getInstance().addJOIComponentNode(
                ArithmeticNode.class, Arithmetic.class,
                arithmetic.getLayoutXPosition(), arithmetic.getLayoutYPosition(), arithmetic.getComponentTitle(),
                arithmetic.getComponentId(), true);
    }

}
