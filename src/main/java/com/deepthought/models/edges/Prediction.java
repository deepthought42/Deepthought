package com.deepthought.models.edges;

import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.deepthought.models.Feature;
import com.deepthought.models.MemoryRecord;

/**
 * Encapsulates data for a predicted weight for a result feature in connection with a {@link MemoryRecord}
 */
@RelationshipProperties
public class Prediction {
	@RelationshipId
	@GeneratedValue
	private Long relationshipId;
    
	@Property  
    private double weight;
    
	@Transient
	private MemoryRecord memory;
    
	@TargetNode   
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
