package com.deepthought.data.edges;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.data.models.Feature;

/**
 * Acts as a relationship between 2 {@link Feature} nodes within the graph and 
 * 	holds the most recent policy/model weight for the feature relationship
 */
@RelationshipEntity(type = "HAS_RELATED_FEATURE")
public class FeatureWeight {
	@Id 
	@GeneratedValue   
	private Long id;
    
	@Property  
    private double weight;
    
	@StartNode 
	private Feature feature;
    
	@EndNode   
	private Feature end_feature;

	public long getId(){
		return this.id;
	}
	
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
	
	public Feature getFeature() {
		return this.feature;
	}
}
