package com.asis.ui.asis_node.node_functional_expansion;

import com.asis.joi.model.entities.Arithmetic;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.VariableSetter;

/**
 * Should be used to resolve any instances where if else and instanceof is used in excess.
 */
public interface ComponentVisitor {

    void visit(Scene scene);

    void visit(Condition condition);

    void visit(VariableSetter variableSetter);

    void visit(Arithmetic arithmetic);

}
