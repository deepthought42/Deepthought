package com.deepthought.brain.knowledge;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.data.edges.FeatureWeight;
import com.deepthought.data.models.Feature;
import com.deepthought.data.repository.FeatureRepository;
import com.deepthought.data.repository.FeatureWeightRepository;

/**
 * Integrates new knowledge into the graph at runtime
 * without requiring full retraining
 */
@Component
public class KnowledgeIntegrator {
	
	private static Logger log = LoggerFactory.getLogger(KnowledgeIntegrator.class);
	
	@Autowired
	private FeatureRepository featureRepository;
	
	@Autowired
	private FeatureWeightRepository featureWeightRepository;
	
	/**
	 * Strategy for handling conflicting knowledge
	 */
	public enum ConflictResolution {
		AVERAGE,     // Average the conflicting weights
		KEEP_HIGHER, // Keep the higher weight
		KEEP_LOWER,  // Keep the lower weight
		REPLACE,     // Replace with new value
		REJECT       // Reject new knowledge if conflict exists
	}
	
	/**
	 * Adds a new knowledge fact to the graph
	 * 
	 * @param sourceFeature Source feature
	 * @param targetFeature Target feature
	 * @param weight Connection weight (0.0-1.0)
	 * @param source Source of this knowledge (for tracking)
	 * @param conflictStrategy How to handle conflicts
	 * @return True if knowledge was integrated, false if rejected
	 */
	public boolean addKnowledge(String sourceFeature, String targetFeature, double weight,
								String source, ConflictResolution conflictStrategy) {
		
		log.info("Adding knowledge: {} -> {} (weight: {}, source: {})", 
			sourceFeature, targetFeature, weight, source);
		
		// Validate weight
		if(weight < 0.0 || weight > 1.0) {
			log.warn("Invalid weight {}, clamping to [0,1]", weight);
			weight = Math.max(0.0, Math.min(1.0, weight));
		}
		
		// Get or create features
		Feature source_feature = featureRepository.findByValue(sourceFeature);
		if(source_feature == null) {
			source_feature = new Feature(sourceFeature);
			source_feature = featureRepository.save(source_feature);
			log.debug("Created new source feature: {}", sourceFeature);
		}
		
		Feature target_feature = featureRepository.findByValue(targetFeature);
		if(target_feature == null) {
			target_feature = new Feature(targetFeature);
			target_feature = featureRepository.save(target_feature);
			log.debug("Created new target feature: {}", targetFeature);
		}
		
		// Check for existing connection
		List<Feature> existingConnections = featureRepository.getConnectedFeatures(
			sourceFeature, targetFeature);
		
		if(!existingConnections.isEmpty()) {
			// Connection exists - handle conflict
			return handleConflict(source_feature, target_feature, weight, 
				existingConnections, conflictStrategy);
		} else {
			// New connection - create it
			FeatureWeight newWeight = featureRepository.createWeightedConnection(
				sourceFeature, targetFeature, weight);
			log.info("Created new connection: {} -> {} (weight: {})", 
				sourceFeature, targetFeature, weight);
			return true;
		}
	}
	
	/**
	 * Handles conflicting knowledge
	 */
	private boolean handleConflict(Feature sourceFeature,
									Feature targetFeature, double newWeight,
									List<Feature> existingConnections, ConflictResolution strategy) {
		
		Feature existingFeature = existingConnections.get(0);
		FeatureWeight existingWeight = null;
		
		// Find the existing weight edge
		for(FeatureWeight fw : existingFeature.getFeatureWeights()) {
			if(fw.getResultFeature().getValue().equals(targetFeature.getValue())) {
				existingWeight = fw;
				break;
			}
		}
		
		if(existingWeight == null) {
			log.warn("Could not find existing weight edge");
			return false;
		}
		
		double oldWeight = existingWeight.getWeight();
		double finalWeight = oldWeight;
		
		log.debug("Conflict detected: existing weight={}, new weight={}, strategy={}", 
			oldWeight, newWeight, strategy);
		
		switch(strategy) {
			case AVERAGE:
				finalWeight = (oldWeight + newWeight) / 2.0;
				break;
			case KEEP_HIGHER:
				finalWeight = Math.max(oldWeight, newWeight);
				break;
			case KEEP_LOWER:
				finalWeight = Math.min(oldWeight, newWeight);
				break;
			case REPLACE:
				finalWeight = newWeight;
				break;
			case REJECT:
				log.info("Rejecting new knowledge due to conflict");
				return false;
		}
		
		existingWeight.setWeight(finalWeight);
		featureWeightRepository.save(existingWeight);
		
		log.info("Resolved conflict: {} -> {} (old: {}, new: {}, final: {})", 
			sourceFeature.getValue(), targetFeature.getValue(), oldWeight, newWeight, finalWeight);
		
		return true;
	}
	
	/**
	 * Adds multiple knowledge facts as a batch
	 * 
	 * @param knowledgeFacts List of knowledge facts [source, target, weight]
	 * @param source Source identifier
	 * @param conflictStrategy Conflict resolution strategy
	 * @return Number of facts successfully integrated
	 */
	public int addKnowledgeBatch(List<KnowledgeFact> knowledgeFacts, String source,
								 ConflictResolution conflictStrategy) {
		
		int successCount = 0;
		
		for(KnowledgeFact fact : knowledgeFacts) {
			boolean success = addKnowledge(fact.getSource(), fact.getTarget(), 
				fact.getWeight(), source, conflictStrategy);
			if(success) {
				successCount++;
			}
		}
		
		log.info("Batch integration: {}/{} facts integrated", successCount, knowledgeFacts.size());
		return successCount;
	}
	
	/**
	 * Removes a knowledge connection
	 * 
	 * @param sourceFeature Source feature
	 * @param targetFeature Target feature
	 * @return True if connection was removed
	 */
	public boolean removeKnowledge(String sourceFeature, String targetFeature) {
		List<Feature> connections = featureRepository.getConnectedFeatures(
			sourceFeature, targetFeature);
		
		if(connections.isEmpty()) {
			log.warn("No connection found to remove: {} -> {}", sourceFeature, targetFeature);
			return false;
		}
		
		Feature feature = connections.get(0);
		List<FeatureWeight> weightsToRemove = new ArrayList<>();
		
		for(FeatureWeight fw : feature.getFeatureWeights()) {
			if(fw.getResultFeature().getValue().equals(targetFeature)) {
				weightsToRemove.add(fw);
			}
		}
		
		for(FeatureWeight fw : weightsToRemove) {
			feature.getFeatureWeights().remove(fw);
			featureWeightRepository.delete(fw);
		}
		
		featureRepository.save(feature);
		log.info("Removed connection: {} -> {}", sourceFeature, targetFeature);
		return true;
	}
	
	/**
	 * Updates the weight of an existing connection
	 * 
	 * @param sourceFeature Source feature
	 * @param targetFeature Target feature
	 * @param newWeight New weight value
	 * @return True if updated successfully
	 */
	public boolean updateWeight(String sourceFeature, String targetFeature, double newWeight) {
		List<Feature> connections = featureRepository.getConnectedFeatures(
			sourceFeature, targetFeature);
		
		if(connections.isEmpty()) {
			log.warn("No connection found to update: {} -> {}", sourceFeature, targetFeature);
			return false;
		}
		
		Feature feature = connections.get(0);
		
		for(FeatureWeight fw : feature.getFeatureWeights()) {
			if(fw.getResultFeature().getValue().equals(targetFeature)) {
				fw.setWeight(newWeight);
				featureWeightRepository.save(fw);
				log.info("Updated weight: {} -> {} = {}", sourceFeature, targetFeature, newWeight);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Validates new knowledge before integration
	 * 
	 * @param sourceFeature Source feature
	 * @param targetFeature Target feature
	 * @param weight Weight value
	 * @return True if knowledge is valid
	 */
	public boolean validateKnowledge(String sourceFeature, String targetFeature, double weight) {
		if(sourceFeature == null || sourceFeature.trim().isEmpty()) {
			log.warn("Invalid source feature: null or empty");
			return false;
		}
		
		if(targetFeature == null || targetFeature.trim().isEmpty()) {
			log.warn("Invalid target feature: null or empty");
			return false;
		}
		
		if(sourceFeature.equals(targetFeature)) {
			log.warn("Self-loop detected: {} -> {}", sourceFeature, targetFeature);
			return false;
		}
		
		if(weight < 0.0 || weight > 1.0) {
			log.warn("Weight out of range: {}", weight);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Represents a knowledge fact
	 */
	public static class KnowledgeFact {
		private String source;
		private String target;
		private double weight;
		
		public KnowledgeFact(String source, String target, double weight) {
			this.source = source;
			this.target = target;
			this.weight = weight;
		}
		
		public String getSource() {
			return source;
		}
		
		public String getTarget() {
			return target;
		}
		
		public double getWeight() {
			return weight;
		}
	}
}


