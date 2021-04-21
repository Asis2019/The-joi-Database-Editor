package com.asis.ui.asis_node.node_functional_expansion;

import com.asis.joi.model.entities.*;

/**
 * Should be used to resolve any instances where if else and instanceof is used in excess.
 */
public interface ComponentVisitor {

    void visit(Scene scene);

    void visit(Condition condition);

    void visit(VariableSetter variableSetter);

    void visit(Arithmetic arithmetic);

    void visit(Group group);

    void visit(GroupBridge groupBridge);
}
