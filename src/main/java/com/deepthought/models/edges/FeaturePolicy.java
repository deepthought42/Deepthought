package com.deepthought.models.edges;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.deepthought.models.Feature;
import com.deepthought.models.MemoryRecord;

/**
 * 
 */
@RelationshipProperties
public class FeaturePolicy {
	@RelationshipId
	@GeneratedValue   
	private Long relationshipId;
    
	@Property
	private List<String> policy_features = new ArrayList<String>();
	
	@Property
	private List<Double> policy_weights = new ArrayList<Double>();
    
	@Property  
    private double reward;
	
	@Transient
	private MemoryRecord memory;
    
	@TargetNode
	private Feature feature;
    
    public MemoryRecord getMemoryRecord(){
    	return this.memory;
    }
    
    public void setMemoryRecord(MemoryRecord memory){
    	this.memory = memory;
    }
  
    
    public List<String> getPolicyFeatures(){
    	return this.policy_features;
    }
    
    public void setPolicyFeatures(List<String> policy_features){
    	this.policy_features = policy_features;
    }
	
    public List<Double> getPolicyWeights(){
    	return this.policy_weights;
    }
    
    public void setPolicyWeights(List<Double> policy_weights){
    	this.policy_weights = policy_weights;
    }

	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	
	public double getReward(){
    	return this.reward;
    }
    
    public void setReward(double reward){
    	this.reward = reward;
    }
}
