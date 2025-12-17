package com.deepthought.api.v2.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic request for processing signals (text, images, audio, and other metadata).
 */
public class SignalRequest {

    @Schema(description = "Primary text content of the request", example = "Summarize the latest events.")
    private String text;

    @Schema(description = "References to images (URLs, IDs, or descriptors)")
    private List<String> imageReferences;

    @Schema(description = "References to audio signals (URLs, IDs, or descriptors)")
    private List<String> audioReferences;

    @Schema(description = "Other signal references or descriptors")
    private List<String> otherSignals;

    @Schema(description = "Session identifier for continuity across requests", example = "session-123")
    private String sessionId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getImageReferences() {
        return imageReferences;
    }

    public void setImageReferences(List<String> imageReferences) {
        this.imageReferences = imageReferences;
    }

    public List<String> getAudioReferences() {
        return audioReferences;
    }

    public void setAudioReferences(List<String> audioReferences) {
        this.audioReferences = audioReferences;
    }

    public List<String> getOtherSignals() {
        return otherSignals;
    }

    public void setOtherSignals(List<String> otherSignals) {
        this.otherSignals = otherSignals;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
