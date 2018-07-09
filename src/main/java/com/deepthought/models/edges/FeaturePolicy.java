package com.deepthought.models.edges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.Feature;
import com.deepthought.models.MemoryRecord;

/**
 * 
 */
@RelationshipEntity(type = "FEATURE_POLICY")
public class FeaturePolicy {
	@Id 
	@GeneratedValue   
	private Long relationshipId;
    
	@Property
	private List<String> policy_actions = new ArrayList<String>();
	
	@Property
	private List<Double> policy_weights = new ArrayList<Double>();
    
	@StartNode 
	private MemoryRecord memory;
    
	@EndNode   
	private Feature feature;
    
    public MemoryRecord getMemoryRecord(){
    	return this.memory;
    }
    
    public void setMemoryRecord(MemoryRecord memory){
    	this.memory = memory;
    }
  
    
    public List<String> getPolicyFeatures(){
    	return this.policy_actions;
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
}
