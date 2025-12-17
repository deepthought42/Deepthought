package com.deepthought.api.v2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepthought.api.v2.dto.ChatMessage;
import com.deepthought.api.v2.dto.GenerationConfig;
import com.deepthought.api.v2.dto.MemoryRetrievalRequest;
import com.deepthought.api.v2.dto.MemoryRetrievalResponse;
import com.deepthought.api.v2.dto.SignalRequest;
import com.deepthought.api.v2.dto.SignalResponse;
import com.deepthought.brain.ExplanationGenerator;
import com.deepthought.brain.GraphReasoningEngine;
import com.deepthought.brain.TextGenerator;
import com.deepthought.conversation.ConversationManager;
import com.deepthought.data.db.DataDecomposer;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.MemoryRecord;
import com.deepthought.data.models.ReasoningPath;
import com.deepthought.data.repository.FeatureRepository;
import com.deepthought.data.repository.MemoryRecordRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * High-level API v2 controller providing generic signal processing and memory retrieval.
 */
@RestController
@RequestMapping("/api/v2")
@Tag(name = "Signals API", description = "Generic signal processing and memory retrieval interface")
public class EnhancedReasoningController {
	
	private static Logger log = LoggerFactory.getLogger(EnhancedReasoningController.class);
	
	@Autowired
	private GraphReasoningEngine reasoningEngine;
	
	@Autowired
	private TextGenerator textGenerator;
	
	@Autowired
	private ExplanationGenerator explanationGenerator;
	
	@Autowired
	private ConversationManager conversationManager;
	
	@Autowired
	private FeatureRepository featureRepository;
	
	@Autowired
	private MemoryRecordRepository memoryRecordRepository;
	
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
			SignalResponse response = new SignalResponse();
			
			// 1. Establish or create a session for continuity (if provided)
			String sessionId = conversationManager.getOrCreateSession(request.getSessionId());
			response.setSessionId(sessionId);
			
			// 2. Combine text with basic references to other signals so they can influence processing
			StringBuilder combinedText = new StringBuilder();
			if(request.getText() != null) {
				combinedText.append(request.getText());
			}
			
			if(request.getImageReferences() != null && !request.getImageReferences().isEmpty()) {
				combinedText.append("\nImages: ").append(String.join(", ", request.getImageReferences()));
			}
			if(request.getAudioReferences() != null && !request.getAudioReferences().isEmpty()) {
				combinedText.append("\nAudio: ").append(String.join(", ", request.getAudioReferences()));
			}
			if(request.getOtherSignals() != null && !request.getOtherSignals().isEmpty()) {
				combinedText.append("\nSignals: ").append(String.join(", ", request.getOtherSignals()));
			}
			
			String finalText = combinedText.toString();
			
			// 3. Record the incoming message in the conversation history
			if(finalText != null && !finalText.isEmpty()) {
				ChatMessage userMessage = new ChatMessage("user", finalText);
				conversationManager.addMessage(sessionId, userMessage);
			}
			
			// 4. Build context from recent conversation plus current content
			List<Feature> contextFeatures = conversationManager.getContextFeatures(sessionId, 30);
			if(finalText != null && !finalText.isEmpty()) {
				contextFeatures.addAll(DataDecomposer.decompose(finalText));
			}
			
			// 5. Perform internal processing using existing reasoning engine
			GenerationConfig config = new GenerationConfig();
			List<ReasoningPath> paths = reasoningEngine.reason(
				contextFeatures,
				config.getMaxHops(),
				config.getMinConfidence()
			);
			
			// 6. Gather relevant features for text generation
			List<Feature> relevantFeatures = new ArrayList<>();
			for(ReasoningPath path : paths) {
				relevantFeatures.addAll(path.getFeatures());
			}
			
			// 7. Generate a textual response
			List<Feature> vocabulary = getGenerationVocabulary();
			String generatedText = textGenerator.generateWithReasoning(
				contextFeatures,
				relevantFeatures,
				vocabulary,
				config
			);
			response.setOutputText(generatedText);
			
			// 8. Compute a simple confidence score
			double avgConfidence = 0.0;
			if(!paths.isEmpty()) {
				for(ReasoningPath path : paths) {
					avgConfidence += path.getTotalConfidence();
				}
				avgConfidence /= paths.size();
			}
			response.setConfidence(avgConfidence);
			
			// 9. Provide lightweight trace information without exposing algorithms
			List<String> sources = explanationGenerator.generateSourcesList(paths);
			response.setSources(sources);
			
			// 10. Store the system response in conversation history
			if(generatedText != null && !generatedText.isEmpty()) {
				ChatMessage assistantMessage = new ChatMessage("assistant", generatedText);
				conversationManager.addMessage(sessionId, assistantMessage);
			}
			
			return ResponseEntity.ok(response);
			
		} catch(Exception e) {
			log.error("Error during signal processing", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
			Date from = request.getFrom();
			Date to = request.getTo();
			
			if(from == null || to == null) {
				return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(MemoryRetrievalResponse.error("Both 'from' and 'to' times are required"));
			}
			
			if(from.after(to)) {
				return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(MemoryRetrievalResponse.error("'from' must be before 'to'"));
			}
			
			// 1. Fetch memories in the given time window
			List<MemoryRecord> candidates = new ArrayList<>();
			memoryRecordRepository.findAll().forEach(candidates::add);
			
			List<MemoryRecord> timeFiltered = new ArrayList<>();
			for(MemoryRecord record : candidates) {
				Date d = record.getDate();
				if(d != null && !d.before(from) && !d.after(to)) {
					timeFiltered.add(record);
				}
			}
			
			// 2. If a prompt is provided, use it to score basic relevance
			String prompt = request.getPrompt();
			List<String> promptTokens = new ArrayList<>();
			if(prompt != null && !prompt.isEmpty()) {
				for(Feature f : DataDecomposer.decompose(prompt)) {
					if(f.getValue() != null) {
						promptTokens.add(f.getValue());
					}
				}
			}
			
			List<MemoryRetrievalResponse.MemoryItem> items = new ArrayList<>();
			for(MemoryRecord record : timeFiltered) {
				double relevance = 0.0;
				
				if(!promptTokens.isEmpty() && record.getInputFeatureValues() != null) {
					for(String token : promptTokens) {
						if(record.getInputFeatureValues().contains(token)) {
							relevance += 1.0;
						}
					}
				}
				
				MemoryRetrievalResponse.MemoryItem item = new MemoryRetrievalResponse.MemoryItem();
				item.setId(record.getID());
				item.setTimestamp(record.getDate());
				item.setInputFeatures(record.getInputFeatureValues());
				item.setPredictedValue(
					record.getPredictedFeature() != null ? record.getPredictedFeature().getValue() : null
				);
				item.setRelevanceScore(relevance);
				
				items.add(item);
			}
			
			// 3. Optionally limit number of results
			int limit = request.getLimit() != null ? request.getLimit() : 50;
			if(items.size() > limit) {
				items = items.subList(0, limit);
			}
			
			MemoryRetrievalResponse response = MemoryRetrievalResponse.success(items);
			return ResponseEntity.ok(response);
			
		} catch(Exception e) {
			log.error("Error during memory retrieval", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Gets a general vocabulary for text generation.
	 * This is populated with common words and domain-specific terms.
	 */
	private List<Feature> getGenerationVocabulary() {
		// In a production system, this would load from a curated vocabulary
		// For now, we'll use a basic set of common words
		List<Feature> vocab = new ArrayList<>();
		
		String[] commonWords = {
			"the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
			"have", "has", "had", "do", "does", "did", "will", "would", "could", "should",
			"can", "may", "might", "must", "of", "in", "on", "at", "to", "for",
			"with", "by", "from", "about", "as", "into", "through", "during", "before", "after",
			"above", "below", "between", "under", "over", "again", "further", "then", "once",
			"and", "or", "but", "if", "because", "so", "that", "which", "who", "what",
			"when", "where", "why", "how", "this", "these", "that", "those", "I", "you",
			"he", "she", "it", "we", "they", "them", "their", "my", "your", "his", "her",
			".", ",", "!", "?"
		};
		
		for(String word : commonWords) {
			vocab.add(new Feature(word));
		}
		
		// Also add any features that exist in the database
		// This helps incorporate domain knowledge
		List<Feature> dbFeatures = new ArrayList<>();
		featureRepository.findAll().forEach(dbFeatures::add);
		
		// Add up to 100 features from database
		int dbLimit = Math.min(100, dbFeatures.size());
		for(int i = 0; i < dbLimit; i++) {
			Feature f = dbFeatures.get(i);
			if(!vocab.contains(f)) {
				vocab.add(f);
			}
		}
		
		return vocab;
	}
}


