package com.deepthought.api.v2.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request for reasoning endpoint
 */
public class ReasoningRequest {
	
	@Schema(description = "Query to reason about", example = "What causes economic recessions?", required = true)
	private String query;
	
	@Schema(description = "Context for the query", example = "[\"historical patterns\", \"policy factors\"]")
	private List<String> context;
	
	@Schema(description = "Maximum reasoning steps", example = "5")
	private int maxSteps = 5;
	
	@Schema(description = "Session ID for conversation continuity", example = "session-123")
	private String sessionId;
	
	@Schema(description = "Generation configuration")
	private GenerationConfig config;
	
	public ReasoningRequest() {
		this.config = new GenerationConfig();
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public List<String> getContext() {
		return context;
	}
	
	public void setContext(List<String> context) {
		this.context = context;
	}
	
	public int getMaxSteps() {
		return maxSteps;
	}
	
	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public GenerationConfig getConfig() {
		return config;
	}
	
	public void setConfig(GenerationConfig config) {
		this.config = config;
	}
}


