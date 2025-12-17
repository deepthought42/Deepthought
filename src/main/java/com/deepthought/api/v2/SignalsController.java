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
 * High-level API v2 controller providing generic signal processing and memory retrieval.
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
	 * Process generic signals (text, images, audio, and other metadata) and produce a response.
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
	 * Retrieve stored memories for a given prompt and time range.
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


