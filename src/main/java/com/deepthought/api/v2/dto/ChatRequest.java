package com.deepthought.api.v2.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request for chat endpoint
 */
public class ChatRequest {
	
	@Schema(description = "User message", example = "Tell me about machine learning", required = true)
	private String message;
	
	@Schema(description = "Session ID for conversation continuity", example = "session-123")
	private String sessionId;
	
	@Schema(description = "Previous messages in conversation")
	private List<ChatMessage> history;
	
	@Schema(description = "Generation configuration")
	private GenerationConfig config;
	
	@Schema(description = "Whether to show reasoning process", example = "false")
	private boolean showReasoning = false;
	
	public ChatRequest() {
		this.config = new GenerationConfig();
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public List<ChatMessage> getHistory() {
		return history;
	}
	
	public void setHistory(List<ChatMessage> history) {
		this.history = history;
	}
	
	public GenerationConfig getConfig() {
		return config;
	}
	
	public void setConfig(GenerationConfig config) {
		this.config = config;
	}
	
	public boolean isShowReasoning() {
		return showReasoning;
	}
	
	public void setShowReasoning(boolean showReasoning) {
		this.showReasoning = showReasoning;
	}
}


