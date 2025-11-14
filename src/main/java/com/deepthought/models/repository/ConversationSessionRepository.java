package com.deepthought.models.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.deepthought.conversation.ConversationSession;

/**
 * Repository for ConversationSession entities
 */
@Repository
public interface ConversationSessionRepository extends Neo4jRepository<ConversationSession, Long> {
	
	/**
	 * Find a conversation session by its session ID
	 * 
	 * @param sessionId The session ID
	 * @return Optional containing the session if found
	 */
	Optional<ConversationSession> findBySessionId(String sessionId);
	
	/**
	 * Delete old sessions that haven't been accessed in a while
	 * Useful for cleanup
	 * 
	 * @param cutoffDate Sessions last accessed before this date will be deleted
	 */
	void deleteByLastAccessedAtBefore(java.util.Date cutoffDate);
}


