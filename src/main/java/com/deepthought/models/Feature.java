package com.deepthought.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.deepthought.models.edges.FeatureWeight;


/**
 * Defines objects that are available to the system for learning against
 */
@NodeEntity
public class Feature {

	@Id 
	@GeneratedValue 
	private Long id;
    
	private String value;
	private String type;
	private String key;
	
	@Relationship(type = "HAS_ACTION")
	private List<FeatureWeight> feature_weights = new ArrayList<FeatureWeight>();
	
	public Feature(){}
	
	/**
	 * Instantiates a new object definition
	 * 
	 * @param uid
	 * @param value
	 * @param type
	 * @param actions
	 * 
	 * @pre actions != null
	 */
	public Feature(String value, String type, List<FeatureWeight> actions) {
		assert actions != null;
		
		this.value = value;
		this.type = type;
		this.key = generateKey();
		this.feature_weights = actions;
	}
	
	/**
	 * 
	 * 
	 * @param uid
	 * @param value
	 * @param type
	 */
	public Feature(String value, String type) {
		this.value = value;
		this.type = type;
		this.key = generateKey();
		this.feature_weights = new ArrayList<FeatureWeight>();
	}

	public String generateKey() {
		return DigestUtils.sha256Hex(getValue()+":"+getType());
	}

	public String getType() {
		return type;
	}
	
	public String getKey(){
		return this.key;
	}
	
	public void setKey(String key){
		this.key = key;
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
	 * Gets list of probabilities associated with actions for this object definition
	 * @return
	 */
	public List<FeatureWeight> getFeatureWeights(){
		return this.feature_weights;
	}
	
	@Override
	public boolean equals(Object o){
		if (this == o) return true;
        if (!(o instanceof Feature)) return false;
        
        Feature that = (Feature)o;
        if(this.getKey().equals(that.getKey())){
        	return true;
        }
        return false;
	}
}
