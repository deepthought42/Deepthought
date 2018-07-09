package com.deepthought.models.edges;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.Action;
import com.deepthought.models.MemoryRecord;

/**
 * 
 */
@RelationshipEntity(type = "PREDICTION")
public class Prediction {
	@Id 
	@GeneratedValue   
	private Long relationshipId;
    
	@Property  
    private double weight;
    
	@StartNode 
	private MemoryRecord memory;
    
	@EndNode   
	private Action action;

	@Property
	private List<String> labels = new ArrayList<String>();
    
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

	public void setMemoryRecord(MemoryRecord memory) {
		this.memory = memory;
	}

	public List<String> getLabels() {
		return this.labels;
	}
}
