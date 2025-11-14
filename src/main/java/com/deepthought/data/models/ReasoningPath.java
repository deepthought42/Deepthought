package com.deepthought.data.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a reasoning path through the knowledge graph
 */
public class ReasoningPath {
	
	private List<Feature> features;
	private List<Double> weights;
	private double totalConfidence;
	private List<String> steps;
	private Map<String, Object> metadata;
	
	public ReasoningPath() {
		this.features = new ArrayList<>();
		this.weights = new ArrayList<>();
		this.steps = new ArrayList<>();
		this.metadata = new HashMap<>();
		this.totalConfidence = 0.0;
	}
	
	public void addStep(Feature feature, double weight, String description) {
		this.features.add(feature);
		this.weights.add(weight);
		this.steps.add(description);
	}
	
	public void computeTotalConfidence() {
		if(weights.isEmpty()) {
			totalConfidence = 0.0;
			return;
		}
		
		// Average weight along path
		double sum = 0.0;
		for(double w : weights) {
			sum += w;
		}
		totalConfidence = sum / weights.size();
	}
	
	public List<Feature> getFeatures() {
		return features;
	}
	
	public void setFeatures(List<Feature> features) {
		this.features = features;
	}
	
	public List<Double> getWeights() {
		return weights;
	}
	
	public void setWeights(List<Double> weights) {
		this.weights = weights;
	}
	
	public double getTotalConfidence() {
		return totalConfidence;
	}
	
	public void setTotalConfidence(double totalConfidence) {
		this.totalConfidence = totalConfidence;
	}
	
	public List<String> getSteps() {
		return steps;
	}
	
	public void setSteps(List<String> steps) {
		this.steps = steps;
	}
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}
	
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	public void addMetadata(String key, Object value) {
		this.metadata.put(key, value);
	}
}


