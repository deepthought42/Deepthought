package com.deepthought.conversation;

import java.util.Date;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * Stores conversation session data in Neo4j
 */
@NodeEntity
public class ConversationSession {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Property
	private String sessionId;
	
	@Property
	private Date createdAt;
	
	@Property
	private Date lastAccessedAt;
	
	@Property
	private String messagesJson; // Serialized chat messages
	
	@Property
	private String contextFeaturesJson; // Serialized feature values
	
	@Property
	private int messageCount;
	
	@Property
	private int maxContextWindow = 50; // Maximum features to keep in context
	
	public ConversationSession() {
		this.createdAt = new Date();
		this.lastAccessedAt = new Date();
		this.messageCount = 0;
	}
	
	public ConversationSession(String sessionId) {
		this();
		this.sessionId = sessionId;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public Date getLastAccessedAt() {
		return lastAccessedAt;
	}
	
	public void setLastAccessedAt(Date lastAccessedAt) {
		this.lastAccessedAt = lastAccessedAt;
	}
	
	public String getMessagesJson() {
		return messagesJson;
	}
	
	public void setMessagesJson(String messagesJson) {
		this.messagesJson = messagesJson;
	}
	
	public String getContextFeaturesJson() {
		return contextFeaturesJson;
	}
	
	public void setContextFeaturesJson(String contextFeaturesJson) {
		this.contextFeaturesJson = contextFeaturesJson;
	}
	
	public int getMessageCount() {
		return messageCount;
	}
	
	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}
	
	public void incrementMessageCount() {
		this.messageCount++;
	}
	
	public int getMaxContextWindow() {
		return maxContextWindow;
	}
	
	public void setMaxContextWindow(int maxContextWindow) {
		this.maxContextWindow = maxContextWindow;
	}
	
	public void updateLastAccessed() {
		this.lastAccessedAt = new Date();
	}
}


