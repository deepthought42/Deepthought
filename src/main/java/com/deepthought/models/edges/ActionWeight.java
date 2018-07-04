package com.deepthought.models.edges;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.Action;
import com.deepthought.models.ObjectDefinition;

@RelationshipEntity(type = "HAS_ACTION")
public class ActionWeight {
	@Id 
	@GeneratedValue   
	private Long relationshipId;
    
	@Property  
    private double weight;
    
	@StartNode 
	private ObjectDefinition object_definition;
    
	@EndNode   
	private Action action;
    
    public Action getAction(){
    	return this.action;
    }
    
    public void setAction(Action action){
    	this.action = action;
    }
    
    public double getWeight(){
    	return this.weight;
    }
    
    public void setWeight(double weight){
    	this.weight = weight;
    }

	public void setObjectDefinition(ObjectDefinition objDef) {
		this.object_definition = objDef;
	}
}
