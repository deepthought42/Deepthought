package com.deepthought.api.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepthought.api.v2.dto.MemoryRetrievalRequest;
import com.deepthought.api.v2.dto.MemoryRetrievalResponse;
import com.deepthought.api.v2.dto.SignalRequest;
import com.deepthought.api.v2.dto.SignalResponse;
import com.deepthought.services.MemoryRetrievalService;
import com.deepthought.services.SignalProcessingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * High-level API v2 controller that exposes endpoints for generic signal processing and memory retrieval.
 * 
 * Preconditions:
 * - {@link SignalProcessingService} and {@link MemoryRetrievalService} beans must be available and injected.
 * - This controller is registered in the Spring context and mapped under the {@code /api/v2} path.
 * 
 * Postconditions:
 * - For successfully handled requests, responses follow the contract of the corresponding DTOs.
 * - In case of unexpected failures, an HTTP 5xx status code is returned without exposing internal details.
 */
@RestController
@RequestMapping("/api/v2")
@Tag(name = "Signals API", description = "Generic signal processing and memory retrieval interface")
public class SignalsController {
	
	private static Logger log = LoggerFactory.getLogger(SignalsController.class);
	
	@Autowired
	private SignalProcessingService signalProcessingService;

	@Autowired
	private MemoryRetrievalService memoryRetrievalService;
	
	/**
	 * Processes generic signals (text, images, audio, and other metadata) and produces a response.
	 * 
	 * Preconditions:
	 * - The request body is deserializable into a {@link SignalRequest} instance.
	 * - The underlying {@link SignalProcessingService} is available and operational.
	 * 
	 * Postconditions:
	 * - On success, returns HTTP 200 with a non-null {@link SignalResponse} in the body.
	 * - On internal error, returns HTTP 500 without leaking implementation details in the body.
	 * 
	 * @param request the incoming signal request containing text and optional signal references
	 * @return a response entity wrapping the {@link SignalResponse} or an error status
	 */
	@Operation(
		summary = "Process signals",
		description = "Accepts text and other signals and returns a generated response"
	)
	@PostMapping("/signals")
	public ResponseEntity<SignalResponse> processSignals(@RequestBody SignalRequest request) {
		log.info("Signal request received");
		try {
			SignalResponse response = signalProcessingService.processSignals(request);
			return ResponseEntity.ok(response);
		} catch(Exception e) {
			log.error("Error during signal processing", e);
			return ResponseEntity.status(500).build();
		}
	}
	
	/**
	 * Retrieves stored memories for a given prompt and time range.
	 * 
	 * Preconditions:
	 * - The request body is deserializable into a {@link MemoryRetrievalRequest} instance.
	 * - The underlying {@link MemoryRetrievalService} is available and operational.
	 * 
	 * Postconditions:
	 * - Always returns HTTP 200 for validation outcomes, with success or failure encoded in the response body.
	 * - On unexpected internal errors, returns HTTP 500.
	 * 
	 * @param request the retrieval request containing time bounds, optional prompt and limit
	 * @return a response entity wrapping a {@link MemoryRetrievalResponse} that describes the result
	 */
	@Operation(
		summary = "Retrieve memories",
		description = "Returns stored memories that match a prompt within a time range"
	)
	@PostMapping("/memory/retrieve")
	public ResponseEntity<MemoryRetrievalResponse> retrieveMemories(
		@RequestBody MemoryRetrievalRequest request
	) {
		log.info("Memory retrieval request received");
		
		try {
			MemoryRetrievalResponse response = memoryRetrievalService.retrieveMemories(request);
			// MemoryRetrievalService encodes validation errors in the response body
			// Controller always returns 200 with success flag / message for simplicity
			return ResponseEntity.ok(response);
		} catch(Exception e) {
			log.error("Error during memory retrieval", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}


