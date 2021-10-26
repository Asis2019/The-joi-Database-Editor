package com.asis.joi.model.entities;

import com.asis.ui.asis_node.node_functional_expansion.ComponentVisitor;

/**
 * Implemented by the joiComponents themselves/
 */
interface ComponentVisitable {

    void accept(ComponentVisitor componentVisitor);

}
