package com.deepthought.api.v2.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response from chat endpoint
 */
public class ChatResponse {
	
	@Schema(description = "Assistant's response message", example = "Machine learning is a branch of artificial intelligence...")
	private String message;
	
	@Schema(description = "Session ID for conversation continuity")
	private String sessionId;
	
	@Schema(description = "Confidence score (0.0-1.0)", example = "0.82")
	private double confidence;
	
	@Schema(description = "Reasoning explanation (if showReasoning is true)")
	private String reasoning;
	
	@Schema(description = "Sources used in generating response")
	private List<String> sources;
	
	public ChatResponse() {}
	
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
	
	public double getConfidence() {
		return confidence;
	}
	
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public String getReasoning() {
		return reasoning;
	}
	
	public void setReasoning(String reasoning) {
		this.reasoning = reasoning;
	}
	
	public List<String> getSources() {
		return sources;
	}
	
	public void setSources(List<String> sources) {
		this.sources = sources;
	}
}


