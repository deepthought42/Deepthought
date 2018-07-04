package com.deepthought.models;

import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Defines an action in name only
 */
@NodeEntity
public class Action {
	@Id 
	@GeneratedValue 
	private Long id;
	
	private String name;
	private String key;
	private String val;
	
	@Relationship(type = "HAS_ACTION", direction = Relationship.INCOMING)
	private List<ObjectDefinition> object_definitions;
	
	/**
	 * Construct empty action object
	 */
	public Action(){}
	
	/**
	 * 
	 * @param action_name
	 */
	public Action(String action_name) {
		this.name = action_name;
		this.val = "";
		setKey(generateKey());
	}
	
	/**
	 * 
	 * @param action_name
	 */
	public Action(String action_name, String value) {
		this.name = action_name;
		this.val = value;
		setKey(generateKey());
	}
	/**
	 * {@inheritDoc}
	 */
	public String generateKey() {
		return getName() + ":"+ getValue().hashCode();
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
	
	/**
	 * @return the name of this action
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * @return the name of this action
	 */
	public void setName(String name){
		this.name = name;
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

	public void setValue(String value) {
		this.val = value;
	}
}
