package com.deepthought.api.v2.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request for retrieving stored memories based on a prompt and time range.
 */
public class MemoryRetrievalRequest {

    @Schema(description = "Optional prompt used to select relevant memories", example = "conversations about reinforcement learning")
    private String prompt;

    @Schema(description = "Start of the time window (inclusive)", example = "2024-01-01T00:00:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private Date from;

    @Schema(description = "End of the time window (inclusive)", example = "2024-12-31T23:59:59Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private Date to;

    @Schema(description = "Maximum number of results to return", example = "50")
    private Integer limit;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
