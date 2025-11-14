package com.deepthought.data.edges;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.data.models.Feature;
import com.deepthought.data.models.MemoryRecord;

/**
 * Encapsulates data for a predicted weight for a result feature in connection with a {@link MemoryRecord}
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
	private Feature result_feature;
    
    public Prediction(MemoryRecord memory, Feature feature, double weight) {
		setMemoryRecord(memory);
		setFeature(feature);
		setWeight(weight);
	}

	public Feature getFeature(){
    	return this.result_feature;
    }
    
    public void setFeature(Feature result_feature){
    	this.result_feature = result_feature;
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
}
