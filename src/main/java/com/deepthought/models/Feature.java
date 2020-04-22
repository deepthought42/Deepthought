package com.deepthought.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.deepthought.models.edges.FeatureWeight;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Defines objects that are available to the system for learning against
 */
@NodeEntity
public class Feature {

	@Schema(description = "Unique identifier of the Feature.", example = "1", required = true)
	@Id 
	@GeneratedValue 
	private Long id;
    
	@Schema(description = "Feature label", example = "form", required = true)
    @NotBlank
	private String value;
	
	@Relationship(type = "HAS_RELATED_FEATURE")
	private List<FeatureWeight> feature_weights = new ArrayList<FeatureWeight>();
	
	public Feature(){}
	
	/**
	 * Instantiates a new object definition
	 * 
	 * @param uid
	 * @param value
	 * @param type
	 * @param features
	 * 
	 * @pre features != null
	 */
	public Feature(String value, List<FeatureWeight> features) {
		assert features != null;
		
		this.value = value;
		this.feature_weights = features;
	}
	
	/**
	 * 
	 * 
	 * @param value
	 * @param type
	 */
	public Feature(String value) {
		this.value = value;
		this.feature_weights = new ArrayList<FeatureWeight>();
	}

	public String getValue(){
		return this.value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString(){
		return this.value;
	}
	
	/**
	 * Gets list of probabilities associated with features for this object definition
	 * @return
	 */
	@JsonIgnore
	public List<FeatureWeight> getFeatureWeights(){
		return this.feature_weights;
	}
	
	@Override
	public boolean equals(Object o){
		if (this == o) return true;
        if (!(o instanceof Feature)) return false;
        
        Feature that = (Feature)o;
        if(this.getValue().equals(that.getValue())){
        	return true;
        }
        return false;
	}
}
