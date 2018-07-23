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
import com.deepthought.models.Feature;

/**
 * 
 */
@RelationshipEntity(type = "HAS_ACTION")
public class ActionWeight {
	@Id 
	@GeneratedValue   
	private Long relationshipId;
    
	@Property  
    private double weight;
    
	@StartNode 
	private Feature feature;
    
	@EndNode   
	private Feature end_feature;

	@Property
	private List<String> labels = new ArrayList<String>();
    
    public Feature getEndFeature(){
    	return this.end_feature;
    }
    
    public void setEndFeature(Feature feature){
    	this.end_feature = feature;
    }
    
    public double getWeight(){
    	return this.weight;
    }
    
    public void setWeight(double weight){
    	this.weight = weight;
    }

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public List<String> getLabels() {
		return this.labels;
	}
}
