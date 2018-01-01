package com.deepthought.models;

import java.util.ArrayList;
import java.util.List;


/**
 * Defines objects that are available to the system for learning against
 */
public class ObjectDefinition {

	public final String value;
	public final String type;
	public final String key;
	public final List<Action> actions;
	
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
	public ObjectDefinition(String value, String type, List<Action> actions) {
		assert actions != null;
		
		this.value = value;
		this.type = type;
		this.key = null;
		this.actions = actions;
	}
	
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
	public ObjectDefinition(String key, String value, String type, List<Action> actions) {
		assert actions != null;
		
		this.value = value;
		this.type = type;
		this.key = key;
		this.actions = actions;
	}
	
	/**
	 * 
	 * 
	 * @param uid
	 * @param value
	 * @param type
	 */
	public ObjectDefinition(String value, String type) {
		this.value = value;
		this.type = type;
		this.key = null;
		this.actions = new ArrayList<Action>();
	}


	public String getType() {
		return type;
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
	public List<Action> getActions(){
		return this.actions;
	}
}
