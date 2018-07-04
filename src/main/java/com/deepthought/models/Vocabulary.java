package com.deepthought.models;

import java.util.ArrayList;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

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
	
	private ArrayList<String> valueList = null;
	//private ArrayList<Float> weights = null;
	//private ArrayList<ArrayList<Float>> actions = null; 
	private String key;

	private String label = null;
	
	public Vocabulary(){}
	
	/**
	 * Generates an empty list with the given label as the list name.
	 * 
	 * @param valueList
	 */
	public Vocabulary(String listLabel) {
		this.valueList = new ArrayList<String>();
		this.label = listLabel;
		this.key = "";
		//this.weights = new ArrayList<Float>();
	}
	
	/**
	 * A specifically ordered list of values of a certain type specified as the label
	 * 
	 * @param valueList
	 */
	public Vocabulary(ArrayList<String> valueList, String label) {
		this.valueList = valueList;
		this.label = label;
		//this.weights = new ArrayList<Float>(valueList.size());
		this.key = "";

	}
	
	/**
	 * Appends an object of the vocabulary type to the end of the current vocabulary list if it 
	 *  doesn't yet exist in the valueList
	 * 
	 * @param obj
	 * @return boolean value indicating if vocabulary object was successfully added to list
	 */
	public boolean appendToVocabulary(String obj){
		if(this.valueList.contains(obj)){
			return false;
		}
		return valueList.add(obj);
	}

	public ArrayList<String> getValueList() {
		return this.valueList;
	}
	
	public String getLabel(){
		return this.label;
	}
}
