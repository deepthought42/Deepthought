package com.deepthought.api.v2.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response from reasoning endpoint
 */
public class ReasoningResponse {
	
	@Schema(description = "Generated conclusion/answer", example = "Economic recessions are typically caused by...")
	private String conclusion;
	
	@Schema(description = "Confidence score (0.0-1.0)", example = "0.85")
	private double confidence;
	
	@Schema(description = "Natural language explanation of reasoning")
	private String explanation;
	
	@Schema(description = "Step-by-step reasoning path")
	private List<String> reasoningSteps;
	
	@Schema(description = "Source features used in reasoning")
	private List<String> sources;
	
	@Schema(description = "Alternative hypotheses considered")
	private List<Map<String, Object>> alternativeHypotheses;
	
	@Schema(description = "Session ID for conversation continuity")
	private String sessionId;
	
	public ReasoningResponse() {}
	
	public String getConclusion() {
		return conclusion;
	}
	
	public void setConclusion(String conclusion) {
		this.conclusion = conclusion;
	}
	
	public double getConfidence() {
		return confidence;
	}
	
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public String getExplanation() {
		return explanation;
	}
	
	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}
	
	public List<String> getReasoningSteps() {
		return reasoningSteps;
	}
	
	public void setReasoningSteps(List<String> reasoningSteps) {
		this.reasoningSteps = reasoningSteps;
	}
	
	public List<String> getSources() {
		return sources;
	}
	
	public void setSources(List<String> sources) {
		this.sources = sources;
	}
	
	public List<Map<String, Object>> getAlternativeHypotheses() {
		return alternativeHypotheses;
	}
	
	public void setAlternativeHypotheses(List<Map<String, Object>> alternativeHypotheses) {
		this.alternativeHypotheses = alternativeHypotheses;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}


