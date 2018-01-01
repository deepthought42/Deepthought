package com.deepthought.models;

/**
 * Defines an action in name only
 */
public class Action {
	private String name;
	private String key;
	private String val;
	
	/**
	 * Construct empty action object
	 */
	public Action(){
		this.name = null;
		this.key = null;
		this.val = "";
	}
	
	/**
	 * 
	 * @param action_name
	 */
	public Action(String action_name) {
		this.name = action_name;
		this.val = "";
	}
	
	/**
	 * 
	 * @param action_name
	 */
	public Action(String action_name, String value) {
		this.name = action_name;
		this.val = value;
	}
	
	/**
	 * @return the name of this action
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString(){
		return this.name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode(){
		return this.name.hashCode();
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getValue() {
		return val;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Action clone() {
		return new Action(this.getName(), this.getValue());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
        if (!(o instanceof Action)) return false;
        Action that = (Action)o;
        return (this.getName().equals(that.getName()) && this.getValue().equals(that.getValue()));
	}
}
