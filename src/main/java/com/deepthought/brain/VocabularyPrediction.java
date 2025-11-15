package com.deepthought.brain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.data.models.Feature;
import com.deepthought.data.models.Vocabulary;
import com.deepthought.data.repository.VocabularyRepository;

/**
 * Predicts which Vocabulary should be used based on a list of Features.
 * Analyzes the features to determine the most appropriate vocabulary context.
 */
@Component
public class VocabularyPrediction {
	
	private static Logger log = LoggerFactory.getLogger(VocabularyPrediction.class);
	
	@Autowired
	private VocabularyRepository vocabularyRepository;
	
	/**
	 * Predicts which vocabulary should be used based on the given features.
	 * 
	 * @param object_list List of features to analyze
	 * @return A Vocabulary with the predicted label (may be newly created)
	 */
	public Vocabulary predict(List<Feature> object_list) {
		if (object_list == null || object_list.isEmpty()) {
			log.warn("Empty feature list provided, returning default vocabulary");
			return new Vocabulary("default");
		}
		
		// Extract feature values
		List<String> featureValues = new ArrayList<>();
		for (Feature feature : object_list) {
			if (feature != null && feature.getValue() != null) {
				featureValues.add(feature.getValue());
			}
		}
		
		// Find the vocabulary that contains the most matching features
		Vocabulary bestMatch = findBestMatchingVocabulary(featureValues);
		
		if (bestMatch != null) {
			log.debug("Found matching vocabulary: {}", bestMatch.getLabel());
			return bestMatch;
		}
		
		// If no match found, generate a vocabulary label based on features
		String predictedLabel = generateVocabularyLabel(featureValues);
		log.debug("No matching vocabulary found, generating new label: {}", predictedLabel);
		
		Vocabulary vocabulary = new Vocabulary(predictedLabel);
		// Add the features to the vocabulary
		for (Feature feature : object_list) {
			if (feature != null) {
				vocabulary.appendToVocabulary(feature);
			}
		}
		
		return vocabulary;
	}
	
	/**
	 * Finds the vocabulary that contains the most matching features from the given list.
	 * 
	 * @param featureValues List of feature values to match
	 * @return The best matching vocabulary, or null if none found
	 */
	private Vocabulary findBestMatchingVocabulary(List<String> featureValues) {
		if (featureValues == null || featureValues.isEmpty()) {
			return null;
		}
		
		// Get all vocabularies from the database
		List<Vocabulary> allVocabularies = new ArrayList<>();
		vocabularyRepository.findAll().forEach(allVocabularies::add);
		
		if (allVocabularies.isEmpty()) {
			return null;
		}
		
		Vocabulary bestMatch = null;
		int maxMatches = 0;
		
		for (Vocabulary vocab : allVocabularies) {
			vocab.initializeMappings(); // Ensure mappings are initialized
			
			int matchCount = 0;
			for (String featureValue : featureValues) {
				if (vocab.contains(featureValue)) {
					matchCount++;
				}
			}
			
			// Calculate match ratio to prefer vocabularies with higher overlap
			double matchRatio = vocab.size() > 0 ? (double) matchCount / vocab.size() : 0.0;
			
			// Prefer vocabularies with more matches and higher match ratio
			if (matchCount > maxMatches || (matchCount == maxMatches && matchRatio > 0.5)) {
				maxMatches = matchCount;
				bestMatch = vocab;
			}
		}
		
		// Only return a match if it has at least some overlap
		if (maxMatches > 0 && bestMatch != null) {
			return bestMatch;
		}
		
		return null;
	}
	
	/**
	 * Generates a vocabulary label based on the feature values.
	 * Uses common patterns or feature characteristics to suggest a label.
	 * 
	 * @param featureValues List of feature values
	 * @return A suggested vocabulary label
	 */
	private String generateVocabularyLabel(List<String> featureValues) {
		if (featureValues == null || featureValues.isEmpty()) {
			return "default";
		}
		
		// Analyze feature patterns to suggest a label
		Map<String, Integer> domainKeywords = new HashMap<>();
		domainKeywords.put("web", 0);
		domainKeywords.put("ui", 0);
		domainKeywords.put("button", 0);
		domainKeywords.put("form", 0);
		domainKeywords.put("click", 0);
		domainKeywords.put("internet", 0);
		domainKeywords.put("api", 0);
		domainKeywords.put("data", 0);
		
		// Count domain-specific keywords
		for (String value : featureValues) {
			String lowerValue = value.toLowerCase();
			for (String keyword : domainKeywords.keySet()) {
				if (lowerValue.contains(keyword)) {
					domainKeywords.put(keyword, domainKeywords.get(keyword) + 1);
				}
			}
		}
		
		// Find the most common domain keyword
		String bestDomain = "general";
		int maxCount = 0;
		for (Map.Entry<String, Integer> entry : domainKeywords.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				bestDomain = entry.getKey();
			}
		}
		
		// If we found a strong domain match, use it
		if (maxCount > 0) {
			return bestDomain + "_vocabulary";
		}
		
		// Otherwise, use a generic label based on feature count
		return "vocabulary_" + featureValues.size();
	}
}

