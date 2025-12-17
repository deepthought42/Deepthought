package com.deepthought.api.v2.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic response for processed signals.
 */
public class SignalResponse {

    @Schema(description = "Generated response text")
    private String outputText;

    @Schema(description = "Identifier for the session associated with this interaction")
    private String sessionId;

    @Schema(description = "Approximate confidence score for the response", example = "0.82")
    private Double confidence;

    @Schema(description = "Optional list of source descriptors relevant to the response")
    private List<String> sources;

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
