package com.deepthought.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.api.v2.dto.ChatMessage;
import com.deepthought.data.db.DataDecomposer;
import com.deepthought.data.models.Feature;
import com.deepthought.data.repository.ConversationSessionRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Manages conversation sessions and context
 */
@Component
public class ConversationManager {
	
	private static Logger log = LoggerFactory.getLogger(ConversationManager.class);
	
	@Autowired
	private ConversationSessionRepository sessionRepository;
	
	private Gson gson = new Gson();
	
	/**
	 * Creates a new conversation session
	 * 
	 * @return New session ID
	 */
	public String createSession() {
		String sessionId = UUID.randomUUID().toString();
		ConversationSession session = new ConversationSession(sessionId);
		sessionRepository.save(session);
		log.info("Created new conversation session: {}", sessionId);
		return sessionId;
	}
	
	/**
	 * Gets or creates a conversation session
	 * 
	 * @param sessionId Session ID (null to create new)
	 * @return Session ID
	 */
	public String getOrCreateSession(String sessionId) {
		if(sessionId == null || sessionId.isEmpty()) {
			return createSession();
		}
		
		Optional<ConversationSession> session = sessionRepository.findBySessionId(sessionId);
		if(!session.isPresent()) {
			log.info("Session {} not found, creating new one", sessionId);
			return createSession();
		}
		
		return sessionId;
	}
	
	/**
	 * Adds a message to the conversation history
	 * 
	 * @param sessionId Session ID
	 * @param message Message to add
	 */
	public void addMessage(String sessionId, ChatMessage message) {
		Optional<ConversationSession> optSession = sessionRepository.findBySessionId(sessionId);
		
		if(!optSession.isPresent()) {
			log.warn("Session {} not found", sessionId);
			return;
		}
		
		ConversationSession session = optSession.get();
		
		// Deserialize existing messages
		List<ChatMessage> messages = getMessages(sessionId);
		messages.add(message);
		
		// Serialize and save
		String messagesJson = gson.toJson(messages);
		session.setMessagesJson(messagesJson);
		session.incrementMessageCount();
		session.updateLastAccessed();
		
		sessionRepository.save(session);
		log.debug("Added message to session {}: {} - {}", sessionId, message.getRole(), 
			message.getContent().substring(0, Math.min(50, message.getContent().length())));
	}
	
	/**
	 * Gets all messages in a conversation
	 * 
	 * @param sessionId Session ID
	 * @return List of messages
	 */
	public List<ChatMessage> getMessages(String sessionId) {
		Optional<ConversationSession> optSession = sessionRepository.findBySessionId(sessionId);
		
		if(!optSession.isPresent()) {
			log.warn("Session {} not found", sessionId);
			return new ArrayList<>();
		}
		
		ConversationSession session = optSession.get();
		String messagesJson = session.getMessagesJson();
		
		if(messagesJson == null || messagesJson.isEmpty()) {
			return new ArrayList<>();
		}
		
		try {
			return gson.fromJson(messagesJson, new TypeToken<List<ChatMessage>>(){}.getType());
		} catch(Exception e) {
			log.error("Error deserializing messages for session {}", sessionId, e);
			return new ArrayList<>();
		}
	}
	
	/**
	 * Gets context features from conversation history
	 * 
	 * @param sessionId Session ID
	 * @param maxFeatures Maximum number of features to return
	 * @return List of features from conversation context
	 */
	public List<Feature> getContextFeatures(String sessionId, int maxFeatures) {
		List<ChatMessage> messages = getMessages(sessionId);
		List<Feature> allFeatures = new ArrayList<>();
		
		// Extract features from recent messages (most recent first)
		for(int i = messages.size() - 1; i >= 0 && allFeatures.size() < maxFeatures; i--) {
			ChatMessage msg = messages.get(i);
			try {
				List<Feature> msgFeatures = DataDecomposer.decompose(msg.getContent());
				for(Feature f : msgFeatures) {
					if(allFeatures.size() >= maxFeatures) {
						break;
					}
					allFeatures.add(f);
				}
			} catch(Exception e) {
				log.error("Error decomposing message content", e);
			}
		}
		
		return allFeatures;
	}
	
	/**
	 * Updates context features for a session
	 * 
	 * @param sessionId Session ID
	 * @param features Features to store
	 */
	public void updateContextFeatures(String sessionId, List<Feature> features) {
		Optional<ConversationSession> optSession = sessionRepository.findBySessionId(sessionId);
		
		if(!optSession.isPresent()) {
			log.warn("Session {} not found", sessionId);
			return;
		}
		
		ConversationSession session = optSession.get();
		
		// Extract feature values
		List<String> featureValues = new ArrayList<>();
		for(Feature f : features) {
			featureValues.add(f.getValue());
		}
		
		// Apply sliding window if needed
		int maxWindow = session.getMaxContextWindow();
		if(featureValues.size() > maxWindow) {
			featureValues = featureValues.subList(featureValues.size() - maxWindow, featureValues.size());
		}
		
		String featuresJson = gson.toJson(featureValues);
		session.setContextFeaturesJson(featuresJson);
		session.updateLastAccessed();
		
		sessionRepository.save(session);
	}
	
	/**
	 * Gets stored context features from session
	 * 
	 * @param sessionId Session ID
	 * @return List of features
	 */
	public List<Feature> getStoredContextFeatures(String sessionId) {
		Optional<ConversationSession> optSession = sessionRepository.findBySessionId(sessionId);
		
		if(!optSession.isPresent()) {
			return new ArrayList<>();
		}
		
		ConversationSession session = optSession.get();
		String featuresJson = session.getContextFeaturesJson();
		
		if(featuresJson == null || featuresJson.isEmpty()) {
			return new ArrayList<>();
		}
		
		try {
			List<String> featureValues = gson.fromJson(featuresJson, 
				new TypeToken<List<String>>(){}.getType());
			
			List<Feature> features = new ArrayList<>();
			for(String value : featureValues) {
				features.add(new Feature(value));
			}
			return features;
		} catch(Exception e) {
			log.error("Error deserializing features for session {}", sessionId, e);
			return new ArrayList<>();
		}
	}
	
	/**
	 * Clears conversation history for a session
	 * 
	 * @param sessionId Session ID
	 */
	public void clearSession(String sessionId) {
		Optional<ConversationSession> optSession = sessionRepository.findBySessionId(sessionId);
		
		if(optSession.isPresent()) {
			ConversationSession session = optSession.get();
			session.setMessagesJson("");
			session.setContextFeaturesJson("");
			session.setMessageCount(0);
			session.updateLastAccessed();
			sessionRepository.save(session);
			log.info("Cleared session {}", sessionId);
		}
	}
	
	/**
	 * Deletes a conversation session
	 * 
	 * @param sessionId Session ID
	 */
	public void deleteSession(String sessionId) {
		Optional<ConversationSession> optSession = sessionRepository.findBySessionId(sessionId);
		
		if(optSession.isPresent()) {
			sessionRepository.delete(optSession.get());
			log.info("Deleted session {}", sessionId);
		}
	}
	
	/**
	 * Gets session information
	 * 
	 * @param sessionId Session ID
	 * @return Session or null if not found
	 */
	public ConversationSession getSession(String sessionId) {
		Optional<ConversationSession> optSession = sessionRepository.findBySessionId(sessionId);
		return optSession.orElse(null);
	}
}


