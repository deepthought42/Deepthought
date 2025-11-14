package com.deepthought.brain;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.deepthought.models.Feature;
import com.deepthought.models.ReasoningPath;

/**
 * Produces human-readable explanations from reasoning paths
 */
@Component
public class ExplanationGenerator {
	
	private static Logger log = LoggerFactory.getLogger(ExplanationGenerator.class);
	
	public enum ExplanationType {
		SUMMARY,      // Brief overview
		STEP_BY_STEP, // Detailed step-by-step
		TECHNICAL     // Technical with weights and metrics
	}
	
	/**
	 * Generates explanation from reasoning paths
	 * 
	 * @param paths Reasoning paths through the graph
	 * @param type Type of explanation to generate
	 * @return Human-readable explanation
	 */
	public String generateExplanation(List<ReasoningPath> paths, ExplanationType type) {
		if(paths == null || paths.isEmpty()) {
			return "No reasoning paths available to explain.";
		}
		
		switch(type) {
			case SUMMARY:
				return generateSummaryExplanation(paths);
			case STEP_BY_STEP:
				return generateStepByStepExplanation(paths);
			case TECHNICAL:
				return generateTechnicalExplanation(paths);
			default:
				return generateSummaryExplanation(paths);
		}
	}
	
	/**
	 * Generates a brief summary explanation
	 */
	private String generateSummaryExplanation(List<ReasoningPath> paths) {
		StringBuilder explanation = new StringBuilder();
		
		explanation.append("I reasoned through ");
		explanation.append(paths.size());
		explanation.append(" path");
		if(paths.size() != 1) {
			explanation.append("s");
		}
		explanation.append(" in the knowledge graph. ");
		
		// Find most confident path
		ReasoningPath bestPath = paths.get(0);
		for(ReasoningPath path : paths) {
			if(path.getTotalConfidence() > bestPath.getTotalConfidence()) {
				bestPath = path;
			}
		}
		
		explanation.append("The strongest connection (confidence: ");
		explanation.append(String.format("%.2f", bestPath.getTotalConfidence()));
		explanation.append(") involves ");
		explanation.append(bestPath.getFeatures().size());
		explanation.append(" key concepts: ");
		
		// List top features from best path
		List<Feature> topFeatures = bestPath.getFeatures();
		int featureLimit = Math.min(5, topFeatures.size());
		for(int i = 0; i < featureLimit; i++) {
			explanation.append("\"");
			explanation.append(topFeatures.get(i).getValue());
			explanation.append("\"");
			if(i < featureLimit - 1) {
				explanation.append(", ");
			}
		}
		
		if(topFeatures.size() > featureLimit) {
			explanation.append(", and ");
			explanation.append(topFeatures.size() - featureLimit);
			explanation.append(" more");
		}
		
		explanation.append(".");
		
		return explanation.toString();
	}
	
	/**
	 * Generates detailed step-by-step explanation
	 */
	private String generateStepByStepExplanation(List<ReasoningPath> paths) {
		StringBuilder explanation = new StringBuilder();
		
		explanation.append("Reasoning Process:\n\n");
		
		for(int pathIdx = 0; pathIdx < paths.size(); pathIdx++) {
			ReasoningPath path = paths.get(pathIdx);
			
			explanation.append("Path ");
			explanation.append(pathIdx + 1);
			explanation.append(" (confidence: ");
			explanation.append(String.format("%.3f", path.getTotalConfidence()));
			explanation.append("):\n");
			
			List<String> steps = path.getSteps();
			for(int i = 0; i < steps.size(); i++) {
				explanation.append("  ");
				explanation.append(i + 1);
				explanation.append(". ");
				explanation.append(steps.get(i));
				explanation.append("\n");
			}
			
			explanation.append("\n");
		}
		
		return explanation.toString();
	}
	
	/**
	 * Generates technical explanation with weights and metrics
	 */
	private String generateTechnicalExplanation(List<ReasoningPath> paths) {
		StringBuilder explanation = new StringBuilder();
		
		explanation.append("Technical Reasoning Analysis:\n\n");
		explanation.append("Total paths explored: ");
		explanation.append(paths.size());
		explanation.append("\n\n");
		
		for(int pathIdx = 0; pathIdx < paths.size(); pathIdx++) {
			ReasoningPath path = paths.get(pathIdx);
			
			explanation.append("=== Path ");
			explanation.append(pathIdx + 1);
			explanation.append(" ===\n");
			
			explanation.append("Overall confidence: ");
			explanation.append(String.format("%.4f", path.getTotalConfidence()));
			explanation.append("\n");
			
			explanation.append("Features traversed: ");
			explanation.append(path.getFeatures().size());
			explanation.append("\n\n");
			
			explanation.append("Edge weights:\n");
			List<Feature> features = path.getFeatures();
			List<Double> weights = path.getWeights();
			
			for(int i = 0; i < features.size(); i++) {
				explanation.append("  ");
				explanation.append(features.get(i).getValue());
				explanation.append(" [weight: ");
				explanation.append(String.format("%.4f", weights.get(i)));
				explanation.append("]\n");
			}
			
			explanation.append("\n");
		}
		
		return explanation.toString();
	}
	
	/**
	 * Generates explanation for why a specific feature was selected
	 */
	public String explainFeatureSelection(Feature selected, List<Feature> candidates, 
										  double[] probabilities) {
		StringBuilder explanation = new StringBuilder();
		
		explanation.append("Selected \"");
		explanation.append(selected.getValue());
		explanation.append("\" from ");
		explanation.append(candidates.size());
		explanation.append(" candidates. ");
		
		// Find index of selected feature
		int selectedIdx = -1;
		for(int i = 0; i < candidates.size(); i++) {
			if(candidates.get(i).getValue().equals(selected.getValue())) {
				selectedIdx = i;
				break;
			}
		}
		
		if(selectedIdx >= 0 && selectedIdx < probabilities.length) {
			explanation.append("Probability: ");
			explanation.append(String.format("%.3f", probabilities[selectedIdx]));
			explanation.append(". ");
			
			// Show top alternatives
			explanation.append("Top alternatives were: ");
			
			List<Integer> topIndices = getTopIndices(probabilities, 3);
			int count = 0;
			for(int idx : topIndices) {
				if(idx != selectedIdx && count < 2) {
					explanation.append("\"");
					explanation.append(candidates.get(idx).getValue());
					explanation.append("\" (");
					explanation.append(String.format("%.3f", probabilities[idx]));
					explanation.append(")");
					count++;
					if(count < 2) {
						explanation.append(", ");
					}
				}
			}
		}
		
		return explanation.toString();
	}
	
	/**
	 * Gets indices of top N values in array
	 */
	private List<Integer> getTopIndices(double[] values, int n) {
		List<Integer> indices = new ArrayList<>();
		
		for(int i = 0; i < values.length; i++) {
			indices.add(i);
		}
		
		// Sort by values (descending)
		indices.sort((a, b) -> Double.compare(values[b], values[a]));
		
		return indices.subList(0, Math.min(n, indices.size()));
	}
	
	/**
	 * Generates simple explanation of reasoning sources
	 */
	public List<String> generateSourcesList(List<ReasoningPath> paths) {
		List<String> sources = new ArrayList<>();
		
		for(ReasoningPath path : paths) {
			for(Feature feature : path.getFeatures()) {
				String source = feature.getValue();
				if(!sources.contains(source)) {
					sources.add(source);
				}
			}
		}
		
		return sources;
	}
}


