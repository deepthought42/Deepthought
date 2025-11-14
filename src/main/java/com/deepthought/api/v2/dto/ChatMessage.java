package com.deepthought.api.v2.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a single message in a conversation
 */
public class ChatMessage {
	
	@Schema(description = "Role of the message sender", example = "user", allowableValues = {"user", "assistant", "system"})
	private String role;
	
	@Schema(description = "Content of the message", example = "What causes economic recessions?")
	private String content;
	
	@Schema(description = "Timestamp of the message", example = "2025-10-11T10:30:00Z")
	private String timestamp;
	
	public ChatMessage() {}
	
	public ChatMessage(String role, String content) {
		this.role = role;
		this.content = content;
		this.timestamp = java.time.Instant.now().toString();
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}


