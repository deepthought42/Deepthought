package com.deepthought.api.v2.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Request object for processing multimodal signals such as text, images, audio and other metadata.
 * 
 * Preconditions:
 * - The instance may be constructed with any combination of non-null or null fields.
 * - Callers must respect the semantics of each field (for example, imageReferences refers to images only).
 * 
 * Postconditions:
 * - After construction, fields reflect the values provided by the caller via setters or deserialization.
 * - The object contains no derived or lazily computed state; all state is explicit in its fields.
 */
@Getter
@Setter
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
}
