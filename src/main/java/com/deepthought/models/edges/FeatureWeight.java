package com.deepthought.models.edges;

import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.deepthought.models.Feature;

/**
 * Acts as a relationship between 2 {@link Feature} nodes within the graph and 
 * 	holds the most recent policy/model weight for the feature relationship
 */
@RelationshipProperties
public class FeatureWeight {
	@RelationshipId
	@GeneratedValue   
	private Long id;
    
	@Property  
    private double weight;
    
	@Transient
	private Feature feature;
    
	@TargetNode
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
