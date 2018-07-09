package com.deepthought.models.edges;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.Action;
import com.deepthought.models.MemoryRecord;

/**
 * A rich edge that contains the reward attributed to a given action for a memory record
 */
@RelationshipEntity(type = "REWARDED")
public class FeatureReward {
	@Id 
	@GeneratedValue   
	private Long relationshipId;
    
	@Property  
    private double reward;
    
	@StartNode 
	private MemoryRecord memory;
    
	@EndNode   
	private Action action;
    
    public Action getAction(){
    	return this.action;
    }
    
    public void setAction(Action action){
    	this.action = action;
    }
    
    public double getReward(){
    	return this.reward;
    }
    
    public void setReward(double reward){
    	this.reward = reward;
    }

	public void setMemoryRecord(MemoryRecord memory) {
		this.memory = memory;
	}
}
