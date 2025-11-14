package com.deepthought.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.api.v2.dto.GenerationConfig;
import com.deepthought.models.Feature;

/**
 * Converts feature sequences into coherent natural language text
 * using graph-based next-token prediction
 */
@Component
public class TextGenerator {
	
	private static Logger log = LoggerFactory.getLogger(TextGenerator.class);
	
	@Autowired
	private Brain brain;
	
	private Random random = new Random();
	
	// Special tokens
	private static final String EOS_TOKEN = "<EOS>";
	private static final String START_TOKEN = "<START>";
	
	/**
	 * Generates text from context features using sequential prediction
	 * 
	 * @param contextFeatures Features providing context for generation
	 * @param candidateVocab Vocabulary of possible output features
	 * @param config Generation configuration
	 * @return Generated text
	 */
	public String generate(List<Feature> contextFeatures, List<Feature> candidateVocab, 
						   GenerationConfig config) {
		
		if(contextFeatures == null || contextFeatures.isEmpty()) {
			log.warn("No context features provided for generation");
			return "";
		}
		
		if(candidateVocab == null || candidateVocab.isEmpty()) {
			log.warn("No candidate vocabulary provided");
			return featuresToText(contextFeatures);
		}
		
		List<Feature> generatedFeatures = new ArrayList<>();
		List<Feature> currentContext = new ArrayList<>(contextFeatures);
		
		int maxTokens = config.getMaxTokens();
		double temperature = config.getTemperature();
		
		// Generate tokens one by one
		for(int i = 0; i < maxTokens; i++) {
			Feature nextFeature;
			
			if(config.getBeamWidth() > 1) {
				// Beam search (simplified - just use greedy for now)
				nextFeature = generateNextFeatureGreedy(currentContext, candidateVocab);
			} else {
				// Greedy or sampling based on temperature
				if(temperature < 0.1) {
					nextFeature = generateNextFeatureGreedy(currentContext, candidateVocab);
				} else {
					nextFeature = generateNextFeatureSampled(currentContext, candidateVocab, temperature);
				}
			}
			
			if(nextFeature == null || nextFeature.getValue().equals(EOS_TOKEN)) {
				break;
			}
			
			generatedFeatures.add(nextFeature);
			
			// Update context with newly generated feature
			currentContext.add(nextFeature);
			
			// Keep context window manageable
			if(currentContext.size() > 20) {
				currentContext.remove(0);
			}
		}
		
		return featuresToText(generatedFeatures);
	}
	
	/**
	 * Generates next feature using greedy selection (highest probability)
	 */
	private Feature generateNextFeatureGreedy(List<Feature> context, List<Feature> candidates) {
		Feature predicted = brain.predictNextFeature(context, candidates);
		return predicted;
	}
	
	/**
	 * Generates next feature using temperature-based sampling
	 */
	private Feature generateNextFeatureSampled(List<Feature> context, List<Feature> candidates, 
											   double temperature) {
		double[] distribution = brain.predictNextFeatureDistribution(context, candidates);
		
		if(distribution == null || distribution.length == 0) {
			return null;
		}
		
		// Apply temperature
		double[] temperedProbs = new double[distribution.length];
		double sum = 0.0;
		
		for(int i = 0; i < distribution.length; i++) {
			temperedProbs[i] = Math.pow(distribution[i], 1.0 / temperature);
			sum += temperedProbs[i];
		}
		
		// Normalize
		for(int i = 0; i < temperedProbs.length; i++) {
			temperedProbs[i] /= sum;
		}
		
		// Sample from distribution
		double randomValue = random.nextDouble();
		double cumulative = 0.0;
		
		for(int i = 0; i < temperedProbs.length; i++) {
			cumulative += temperedProbs[i];
			if(randomValue <= cumulative) {
				return candidates.get(i);
			}
		}
		
		// Fallback to last candidate
		return candidates.get(candidates.size() - 1);
	}
	
	/**
	 * Converts list of features into natural language text
	 * Handles basic punctuation and capitalization
	 */
	private String featuresToText(List<Feature> features) {
		if(features == null || features.isEmpty()) {
			return "";
		}
		
		StringBuilder text = new StringBuilder();
		boolean startOfSentence = true;
		
		for(int i = 0; i < features.size(); i++) {
			String word = features.get(i).getValue();
			
			// Skip special tokens
			if(word.equals(START_TOKEN) || word.equals(EOS_TOKEN)) {
				continue;
			}
			
			// Capitalize start of sentence
			if(startOfSentence) {
				word = capitalize(word);
				startOfSentence = false;
			}
			
			// Add space before word (unless it's punctuation)
			if(text.length() > 0 && !isPunctuation(word)) {
				text.append(" ");
			}
			
			text.append(word);
			
			// Check if this ends a sentence
			if(word.endsWith(".") || word.endsWith("!") || word.endsWith("?")) {
				startOfSentence = true;
			}
		}
		
		// Ensure sentence ends with punctuation
		if(text.length() > 0 && !endsWithPunctuation(text.toString())) {
			text.append(".");
		}
		
		return text.toString();
	}
	
	/**
	 * Capitalizes first letter of word
	 */
	private String capitalize(String word) {
		if(word == null || word.isEmpty()) {
			return word;
		}
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}
	
	/**
	 * Checks if token is punctuation
	 */
	private boolean isPunctuation(String token) {
		return token.matches("[.,!?;:]");
	}
	
	/**
	 * Checks if text ends with sentence-ending punctuation
	 */
	private boolean endsWithPunctuation(String text) {
		if(text == null || text.isEmpty()) {
			return false;
		}
		char last = text.charAt(text.length() - 1);
		return last == '.' || last == '!' || last == '?';
	}
	
	/**
	 * Generates text with reasoning - combines context and reasoning path features
	 * 
	 * @param contextFeatures Original query features
	 * @param reasoningFeatures Features gathered from graph reasoning
	 * @param candidateVocab Vocabulary for generation
	 * @param config Generation config
	 * @return Generated text incorporating reasoning
	 */
	public String generateWithReasoning(List<Feature> contextFeatures, 
									   List<Feature> reasoningFeatures,
									   List<Feature> candidateVocab,
									   GenerationConfig config) {
		
		// Combine context and reasoning features
		List<Feature> combinedContext = new ArrayList<>(contextFeatures);
		
		// Add most relevant reasoning features (top 10)
		int reasoningLimit = Math.min(10, reasoningFeatures.size());
		for(int i = 0; i < reasoningLimit; i++) {
			if(!combinedContext.contains(reasoningFeatures.get(i))) {
				combinedContext.add(reasoningFeatures.get(i));
			}
		}
		
		return generate(combinedContext, candidateVocab, config);
	}
}

