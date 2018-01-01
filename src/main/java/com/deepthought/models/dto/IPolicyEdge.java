package com.deepthought.models.dto;

import com.tinkerpop.frames.InVertex;
import com.tinkerpop.frames.OutVertex;
import com.tinkerpop.frames.Property;

/**
 * 
 *
 */
public interface IPolicyEdge {
    @Property("probability")
    public double getProbability();
    
    @Property("probability")
    public void setProbability(double probability);
    
    @OutVertex
    IAction getActionOut();

    @InVertex
    IObjectDefinition getObjectDefinitionIn();
}
