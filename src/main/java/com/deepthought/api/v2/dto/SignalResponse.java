package com.deepthought.api.v2.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Response object for processed signals returned by the signals API.
 * 
 * Preconditions:
 * - Instances are created and populated by server-side components, not clients.
 * 
 * Postconditions:
 * - Fields describe the outcome of processing a corresponding {@code SignalRequest}, including text, session and confidence.
 */
@Getter
@Setter
public class SignalResponse {

    @Schema(description = "Generated response text")
    private String outputText;

    @Schema(description = "Identifier for the session associated with this interaction")
    private String sessionId;

    @Schema(description = "Approximate confidence score for the response", example = "0.82")
    private Double confidence;

    @Schema(description = "Optional list of source descriptors relevant to the response")
    private List<String> sources;
}
