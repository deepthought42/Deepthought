package com.deepthought.models.dto;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Incidence;
import com.tinkerpop.frames.Property;

/**
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
	
	@Incidence(direction=Direction.OUT, label="action")
	public Iterable<IAction> getActions();

	@Incidence(direction=Direction.OUT, label="action")
	public void removeAction(IAction value);
	
	@Incidence(direction=Direction.OUT, label="action")
	public Iterable<IPolicyEdge> getPolicyEdges();
	
	@Incidence(direction=Direction.OUT, label="action")
	public IPolicyEdge addAction(IAction action);
}
