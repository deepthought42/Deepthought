package com.deepthought.models.dto;

import com.tinkerpop.frames.Property;

/**
 * 
 */
public interface IAction {
	@Property("key")
	public String getKey();
	
	@Property("key")
	public void setKey(String key);
	
	@Property("name")
	public String getName();
	
	@Property("name")
	public String setName(String name);
	
	@Property("value")
	public String getValue();
	
	@Property("value")
	public String setValue(String value);
}
