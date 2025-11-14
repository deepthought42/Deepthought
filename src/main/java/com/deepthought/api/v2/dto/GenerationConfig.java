package com.deepthought.api.v2.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Configuration parameters for text generation
 */
public class GenerationConfig {
	
	@Schema(description = "Temperature for sampling (0.0 = deterministic, 1.0 = random)", example = "0.7")
	private double temperature = 0.7;
	
	@Schema(description = "Maximum number of tokens to generate", example = "100")
	private int maxTokens = 100;
	
	@Schema(description = "Beam width for beam search (1 = greedy)", example = "3")
	private int beamWidth = 1;
	
	@Schema(description = "Maximum number of reasoning hops in graph", example = "3")
	private int maxHops = 3;
	
	@Schema(description = "Minimum confidence threshold for including features", example = "0.1")
	private double minConfidence = 0.1;
	
	@Schema(description = "Whether to include reasoning explanation", example = "true")
	private boolean includeExplanation = true;
	
	public GenerationConfig() {}
	
	public double getTemperature() {
		return temperature;
	}
	
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	
	public int getMaxTokens() {
		return maxTokens;
	}
	
	public void setMaxTokens(int maxTokens) {
		this.maxTokens = maxTokens;
	}
	
	public int getBeamWidth() {
		return beamWidth;
	}
	
	public void setBeamWidth(int beamWidth) {
		this.beamWidth = beamWidth;
	}
	
	public int getMaxHops() {
		return maxHops;
	}
	
	public void setMaxHops(int maxHops) {
		this.maxHops = maxHops;
	}
	
	public double getMinConfidence() {
		return minConfidence;
	}
	
	public void setMinConfidence(double minConfidence) {
		this.minConfidence = minConfidence;
	}
	
	public boolean isIncludeExplanation() {
		return includeExplanation;
	}
	
	public void setIncludeExplanation(boolean includeExplanation) {
		this.includeExplanation = includeExplanation;
	}
}


