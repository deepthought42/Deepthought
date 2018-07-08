package com.deepthought.models.edges;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.Feature;

/**
 * 
 */
@RelationshipEntity(type = "HAS_FEATURE")
public class FeatureWeight {
	@Id 
	@GeneratedValue   
	private Long relationshipId;
    
	@Property  
    private double weight;
    
	@StartNode 
	private Feature start_feature;
    
	@EndNode   
	private Feature end_feature;

	@Property
	private List<String> labels = new ArrayList<String>();
    
    public Feature getStartFeature(){
    	return this.start_feature;
    }
    
    public void setStartFeature(Feature feature){
    	this.start_feature = feature;
    }
    
    public double getWeight(){
    	return this.weight;
    }
    
    public void setWeight(double weight){
    	this.weight = weight;
    }

	public void setEndFeature(Feature feature) {
		this.end_feature = feature;
	}
	
	public Feature getEndFeature(){
		return end_feature;
	}

	public List<String> getLabels() {
		return this.labels;
	}
}
