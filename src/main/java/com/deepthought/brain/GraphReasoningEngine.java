package com.deepthought.brain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.data.edges.FeatureWeight;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.ReasoningPath;
import com.deepthought.data.repository.FeatureRepository;

/**
 * Performs multi-hop graph traversal to gather relevant features
 * and compute attention-based relevance scores
 */
@Component
public class GraphReasoningEngine {
	
	private static Logger log = LoggerFactory.getLogger(GraphReasoningEngine.class);
	
	@Autowired
	private FeatureRepository featureRepository;
	
	/**
	 * Performs multi-hop reasoning starting from query features
	 * 
	 * @param queryFeatures Starting features from the query
	 * @param maxHops Maximum number of hops to traverse
	 * @param minConfidence Minimum weight threshold for following edges
	 * @return List of reasoning paths with confidence scores
	 */
	public List<ReasoningPath> reason(List<Feature> queryFeatures, int maxHops, double minConfidence) {
		List<ReasoningPath> paths = new ArrayList<>();
		
		if(queryFeatures == null || queryFeatures.isEmpty()) {
			log.warn("No query features provided for reasoning");
			return paths;
		}
		
		// For each query feature, explore its neighborhood
		for(Feature queryFeature : queryFeatures) {
			Feature featureRecord = featureRepository.findByValue(queryFeature.getValue());
			if(featureRecord != null) {
				ReasoningPath path = explorePath(featureRecord, maxHops, minConfidence, new HashSet<>());
				if(path.getFeatures().size() > 0) {
					path.computeTotalConfidence();
					paths.add(path);
				}
			}
		}
		
		return paths;
	}
	
	/**
	 * Recursively explores a path from a starting feature
	 * 
	 * @param feature Current feature
	 * @param remainingHops Number of hops remaining
	 * @param minConfidence Minimum weight threshold
	 * @param visited Set of already visited features to avoid cycles
	 * @return Reasoning path from this feature
	 */
	private ReasoningPath explorePath(Feature feature, int remainingHops, double minConfidence, Set<String> visited) {
		ReasoningPath path = new ReasoningPath();
		
		if(remainingHops <= 0 || visited.contains(feature.getValue())) {
			return path;
		}
		
		visited.add(feature.getValue());
		path.addStep(feature, 1.0, "Starting from: " + feature.getValue());
		
		// Get connected features with weights
		List<FeatureWeight> weights = feature.getFeatureWeights();
		if(weights == null || weights.isEmpty()) {
			return path;
		}
		
		// Follow edges above confidence threshold
		for(FeatureWeight weight : weights) {
			if(weight.getWeight() >= minConfidence) {
				Feature nextFeature = weight.getEndFeature();
				if(nextFeature != null && !visited.contains(nextFeature.getValue())) {
					String step = String.format("Connected to '%s' (weight: %.3f)", 
						nextFeature.getValue(), weight.getWeight());
					path.addStep(nextFeature, weight.getWeight(), step);
					
					// Recursively explore from connected feature
					if(remainingHops > 1) {
						Set<String> newVisited = new HashSet<>(visited);
						ReasoningPath subPath = explorePath(nextFeature, remainingHops - 1, 
							minConfidence, newVisited);
						
						// Merge subpath into current path
						for(int i = 0; i < subPath.getFeatures().size(); i++) {
							if(!visited.contains(subPath.getFeatures().get(i).getValue())) {
								path.addStep(subPath.getFeatures().get(i), 
									subPath.getWeights().get(i), 
									subPath.getSteps().get(i));
								visited.add(subPath.getFeatures().get(i).getValue());
							}
						}
					}
				}
			}
		}
		
		return path;
	}
	
	/**
	 * Computes attention scores for features based on their relevance to query
	 * 
	 * @param queryFeatures Features from the query
	 * @param candidateFeatures Features to score
	 * @return Map of feature values to attention scores
	 */
	public Map<String, Double> computeAttentionScores(List<Feature> queryFeatures, 
													   List<Feature> candidateFeatures) {
		Map<String, Double> scores = new HashMap<>();
		
		for(Feature candidate : candidateFeatures) {
			double maxScore = 0.0;
			
			// For each query feature, find highest connection weight to candidate
			for(Feature query : queryFeatures) {
				Feature queryRecord = featureRepository.findByValue(query.getValue());
				if(queryRecord != null) {
					List<Feature> connected = featureRepository.getConnectedFeatures(
						query.getValue(), candidate.getValue());
					
					if(!connected.isEmpty()) {
						Feature connectedFeature = connected.get(0);
						for(FeatureWeight weight : connectedFeature.getFeatureWeights()) {
							if(weight.getEndFeature().getValue().equals(candidate.getValue())) {
								maxScore = Math.max(maxScore, weight.getWeight());
							}
						}
					}
				}
			}
			
			scores.put(candidate.getValue(), maxScore);
		}
		
		return scores;
	}
	
	/**
	 * Gathers all relevant features within specified hops from query features
	 * 
	 * @param queryFeatures Starting features
	 * @param maxHops Maximum traversal depth
	 * @return List of relevant features with their relevance scores
	 */
	public List<Feature> gatherRelevantFeatures(List<Feature> queryFeatures, int maxHops) {
		Set<String> seenValues = new HashSet<>();
		List<Feature> relevant = new ArrayList<>();
		
		for(Feature queryFeature : queryFeatures) {
			Feature featureRecord = featureRepository.findByValue(queryFeature.getValue());
			if(featureRecord != null) {
				gatherFeaturesRecursive(featureRecord, maxHops, seenValues, relevant);
			}
		}
		
		return relevant;
	}
	
	/**
	 * Recursively gathers features
	 */
	private void gatherFeaturesRecursive(Feature feature, int remainingHops, 
										 Set<String> seen, List<Feature> result) {
		if(remainingHops <= 0 || seen.contains(feature.getValue())) {
			return;
		}
		
		seen.add(feature.getValue());
		result.add(feature);
		
		List<FeatureWeight> weights = feature.getFeatureWeights();
		if(weights != null && remainingHops > 1) {
			for(FeatureWeight weight : weights) {
				Feature next = weight.getEndFeature();
				if(next != null && !seen.contains(next.getValue())) {
					gatherFeaturesRecursive(next, remainingHops - 1, seen, result);
				}
			}
		}
	}
}


