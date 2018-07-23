package com.deepthought.models;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * A list of unique objects of a designated type that are stored and loaded in a specific order as
 *  a way of maintaining the ability to grow a vertex of features that are always in the exact same
 *  order from run to run.
 */
@NodeEntity
public class Vocabulary{

	@Id 
	@GeneratedValue 
	private Long id;
	private String key;
	private String label = null;
	
	@Relationship(type = "HAS_FEATURE")
	private List<Feature> features = new ArrayList<Feature>();
	
	public Vocabulary(){}
	
	/**
	 * Generates an empty list with the given label as the list name.
	 * 
	 * @param valueList
	 */
	public Vocabulary(String listLabel) {
		setLabel(listLabel);
		this.setKey(label);
	}
	
	/**
	 * A specifically ordered list of values of a certain type specified as the label
	 * 
	 * @param valueList
	 */
	public Vocabulary(List<Feature> features, String label) {
		setLabel(label);
		setFeatures(features);
		this.setKey(label);

	}

	/**
	 * Appends an object of the vocabulary type to the end of the current vocabulary list if it 
	 *  doesn't yet exist in the valueList
	 * 
	 * @param obj
	 * @return boolean value indicating if vocabulary object was successfully added to list
	 */
	public boolean appendToVocabulary(Feature feature){
		if(this.features.contains(feature)){
			return false;
		}
		return features.add(feature);
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public void setFeatures(List<Feature> features) {
		this.features = features;		
	}
	
	public List<Feature> getFeatures() {
		return this.features;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
