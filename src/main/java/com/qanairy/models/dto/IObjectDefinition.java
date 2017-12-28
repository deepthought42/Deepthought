package com.qanairy.models.dto;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;

/**
 * 
 *
 */
public interface IObjectDefinition {
	@Property("key")
	public String getKey();
	
	@Property("key")
	public void setKey(String key);
	
	@Property("type")
	public String getType();
	
	@Property("type")
	public String setType(String type);
	
	@Property("value")
	public String getValue();
	
	@Property("value")
	public String setValue(String value);
	
	@Adjacency(label="action")
	public Iterable<IAction> getActions();
	
	@Adjacency(label="action")
	public void addAction(IAction value);

	@Adjacency(label="action")
	public void removeAction(IAction value);
}
