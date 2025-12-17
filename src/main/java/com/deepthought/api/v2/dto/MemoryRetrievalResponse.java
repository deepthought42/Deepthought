package com.deepthought.api.v2.dto;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response wrapper for memory retrieval results.
 */
public class MemoryRetrievalResponse {

    @Schema(description = "Whether the retrieval was successful")
    private boolean success;

    @Schema(description = "Optional message, typically used for errors")
    private String message;

    @Schema(description = "Retrieved memory items")
    private List<MemoryItem> items;

    public static MemoryRetrievalResponse success(List<MemoryItem> items) {
        MemoryRetrievalResponse r = new MemoryRetrievalResponse();
        r.success = true;
        r.items = items;
        return r;
    }

    public static MemoryRetrievalResponse error(String message) {
        MemoryRetrievalResponse r = new MemoryRetrievalResponse();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MemoryItem> getItems() {
        return items;
    }

    public void setItems(List<MemoryItem> items) {
        this.items = items;
    }

    /**
     * Lightweight view of a stored memory.
     */
    public static class MemoryItem {

        @Schema(description = "Identifier of the memory record")
        private Long id;

        @Schema(description = "Timestamp when the memory was created")
        private Date timestamp;

        @Schema(description = "Input feature values associated with the memory")
        private List<String> inputFeatures;

        @Schema(description = "Predicted value stored with this memory, if any")
        private String predictedValue;

        @Schema(description = "Simple relevance score computed for the given prompt")
        private Double relevanceScore;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public List<String> getInputFeatures() {
            return inputFeatures;
        }

        public void setInputFeatures(List<String> inputFeatures) {
            this.inputFeatures = inputFeatures;
        }

        public String getPredictedValue() {
            return predictedValue;
        }

        public void setPredictedValue(String predictedValue) {
            this.predictedValue = predictedValue;
        }

        public Double getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(Double relevanceScore) {
            this.relevanceScore = relevanceScore;
        }
    }
}
