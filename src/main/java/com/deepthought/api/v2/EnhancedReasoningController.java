package com.deepthought.api.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepthought.api.v2.dto.ChatMessage;
import com.deepthought.api.v2.dto.ChatRequest;
import com.deepthought.api.v2.dto.ChatResponse;
import com.deepthought.api.v2.dto.GenerationConfig;
import com.deepthought.api.v2.dto.ReasoningRequest;
import com.deepthought.api.v2.dto.ReasoningResponse;
import com.deepthought.brain.ExplanationGenerator;
import com.deepthought.brain.GraphReasoningEngine;
import com.deepthought.brain.TextGenerator;
import com.deepthought.brain.knowledge.KnowledgeIntegrator;
import com.deepthought.conversation.ConversationManager;
import com.deepthought.data.db.DataDecomposer;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.ReasoningPath;
import com.deepthought.data.repository.FeatureRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Enhanced API v2 controller providing LLM-style output capabilities
 */
@RestController
@RequestMapping("/api/v2")
@Tag(name = "Enhanced Reasoning API", description = "LLM-style reasoning and conversational interfaces")
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
	private KnowledgeIntegrator knowledgeIntegrator;
	
	@Autowired
	private FeatureRepository featureRepository;
	
	/**
	 * Multi-step reasoning with explanations
	 */
	@Operation(summary = "Perform multi-step reasoning", 
		description = "Reasons through the knowledge graph and generates natural language response")
	@PostMapping("/reason")
	public ResponseEntity<ReasoningResponse> reason(@RequestBody ReasoningRequest request) {
		log.info("Reasoning request: {}", request.getQuery());
		
		try {
			ReasoningResponse response = new ReasoningResponse();
			
			// 1. Decompose query into features
			List<Feature> queryFeatures = DataDecomposer.decompose(request.getQuery());
			log.debug("Query features: {}", queryFeatures.size());
			
			// 2. Add context features if provided
			if(request.getContext() != null) {
				for(String ctx : request.getContext()) {
					queryFeatures.addAll(DataDecomposer.decompose(ctx));
				}
			}
			
			// 3. Add conversation context if session provided
			if(request.getSessionId() != null) {
				List<Feature> sessionContext = conversationManager.getContextFeatures(
					request.getSessionId(), 20);
				queryFeatures.addAll(sessionContext);
			}
			
			// 4. Perform graph reasoning
			GenerationConfig config = request.getConfig();
			List<ReasoningPath> paths = reasoningEngine.reason(queryFeatures, 
				config.getMaxHops(), config.getMinConfidence());
			log.debug("Found {} reasoning paths", paths.size());
			
			// 5. Gather relevant features from reasoning
			List<Feature> relevantFeatures = new ArrayList<>();
			for(ReasoningPath path : paths) {
				relevantFeatures.addAll(path.getFeatures());
			}
			
			// 6. Get vocabulary for generation (use existing features + common words)
			List<Feature> vocabulary = getGenerationVocabulary();
			
			// 7. Generate natural language response
			String generatedText = textGenerator.generateWithReasoning(
				queryFeatures, relevantFeatures, vocabulary, config);
			response.setConclusion(generatedText);
			
			// 8. Compute confidence
			double avgConfidence = 0.0;
			if(!paths.isEmpty()) {
				for(ReasoningPath path : paths) {
					avgConfidence += path.getTotalConfidence();
				}
				avgConfidence /= paths.size();
			}
			response.setConfidence(avgConfidence);
			
			// 9. Generate explanation if requested
			if(config.isIncludeExplanation()) {
				String explanation = explanationGenerator.generateExplanation(paths, 
					ExplanationGenerator.ExplanationType.SUMMARY);
				response.setExplanation(explanation);
			}
			
			// 10. Extract reasoning steps
			List<String> steps = new ArrayList<>();
			for(ReasoningPath path : paths) {
				steps.addAll(path.getSteps());
			}
			response.setReasoningSteps(steps);
			
			// 11. Generate sources list
			List<String> sources = explanationGenerator.generateSourcesList(paths);
			response.setSources(sources);
			
			// 12. Set session ID
			if(request.getSessionId() != null) {
				response.setSessionId(request.getSessionId());
			}
			
			log.info("Reasoning completed with confidence: {}", avgConfidence);
			return ResponseEntity.ok(response);
			
		} catch(Exception e) {
			log.error("Error during reasoning", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Conversational interface
	 */
	@Operation(summary = "Chat interface", 
		description = "Multi-turn conversational interface with context awareness")
	@PostMapping("/chat")
	public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
		log.info("Chat request: {}", request.getMessage());
		
		try {
			ChatResponse response = new ChatResponse();
			
			// 1. Get or create session
			String sessionId = conversationManager.getOrCreateSession(request.getSessionId());
			response.setSessionId(sessionId);
			
			// 2. Add user message to history
			ChatMessage userMessage = new ChatMessage("user", request.getMessage());
			conversationManager.addMessage(sessionId, userMessage);
			
			// 3. Get conversation context
			List<Feature> contextFeatures = conversationManager.getContextFeatures(sessionId, 30);
			
			// 4. Decompose current message
			List<Feature> messageFeatures = DataDecomposer.decompose(request.getMessage());
			contextFeatures.addAll(messageFeatures);
			
			// 5. Perform reasoning
			GenerationConfig config = request.getConfig();
			List<ReasoningPath> paths = reasoningEngine.reason(contextFeatures, 
				config.getMaxHops(), config.getMinConfidence());
			
			// 6. Gather relevant features
			List<Feature> relevantFeatures = new ArrayList<>();
			for(ReasoningPath path : paths) {
				relevantFeatures.addAll(path.getFeatures());
			}
			
			// 7. Generate response
			List<Feature> vocabulary = getGenerationVocabulary();
			String generatedText = textGenerator.generateWithReasoning(
				messageFeatures, relevantFeatures, vocabulary, config);
			response.setMessage(generatedText);
			
			// 8. Compute confidence
			double avgConfidence = 0.0;
			if(!paths.isEmpty()) {
				for(ReasoningPath path : paths) {
					avgConfidence += path.getTotalConfidence();
				}
				avgConfidence /= paths.size();
			}
			response.setConfidence(avgConfidence);
			
			// 9. Add reasoning if requested
			if(request.isShowReasoning()) {
				String reasoning = explanationGenerator.generateExplanation(paths,
					ExplanationGenerator.ExplanationType.STEP_BY_STEP);
				response.setReasoning(reasoning);
			}
			
			// 10. Set sources
			List<String> sources = explanationGenerator.generateSourcesList(paths);
			response.setSources(sources);
			
			// 11. Add assistant message to history
			ChatMessage assistantMessage = new ChatMessage("assistant", generatedText);
			conversationManager.addMessage(sessionId, assistantMessage);
			
			// 12. Update context features
			List<Feature> allFeatures = new ArrayList<>(contextFeatures);
			allFeatures.addAll(relevantFeatures);
			conversationManager.updateContextFeatures(sessionId, allFeatures);
			
			log.info("Chat response generated for session: {}", sessionId);
			return ResponseEntity.ok(response);
			
		} catch(Exception e) {
			log.error("Error during chat", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Sequential text generation
	 */
	@Operation(summary = "Generate text", 
		description = "Generate text sequentially from context")
	@PostMapping("/generate")
	public ResponseEntity<Map<String, Object>> generate(@RequestBody Map<String, Object> request) {
		log.info("Generate request");
		
		try {
			String prompt = (String) request.get("prompt");
			GenerationConfig config = new GenerationConfig();
			
			if(request.containsKey("temperature")) {
				config.setTemperature(((Number) request.get("temperature")).doubleValue());
			}
			if(request.containsKey("maxTokens")) {
				config.setMaxTokens(((Number) request.get("maxTokens")).intValue());
			}
			
			// Decompose prompt
			List<Feature> contextFeatures = DataDecomposer.decompose(prompt);
			
			// Get vocabulary
			List<Feature> vocabulary = getGenerationVocabulary();
			
			// Generate
			String generatedText = textGenerator.generate(contextFeatures, vocabulary, config);
			
			Map<String, Object> response = new HashMap<>();
			response.put("generated_text", generatedText);
			response.put("prompt", prompt);
			
			return ResponseEntity.ok(response);
			
		} catch(Exception e) {
			log.error("Error during generation", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Detailed explanation generation
	 */
	@Operation(summary = "Generate explanation", 
		description = "Generate detailed explanation of reasoning process")
	@PostMapping("/explain")
	public ResponseEntity<Map<String, Object>> explain(@RequestBody Map<String, Object> request) {
		log.info("Explain request");
		
		try {
			String query = (String) request.get("query");
			String typeStr = (String) request.getOrDefault("type", "SUMMARY");
			
			ExplanationGenerator.ExplanationType type;
			try {
				type = ExplanationGenerator.ExplanationType.valueOf(typeStr);
			} catch(IllegalArgumentException e) {
				type = ExplanationGenerator.ExplanationType.SUMMARY;
			}
			
			// Decompose query
			List<Feature> queryFeatures = DataDecomposer.decompose(query);
			
			// Perform reasoning
			List<ReasoningPath> paths = reasoningEngine.reason(queryFeatures, 3, 0.1);
			
			// Generate explanation
			String explanation = explanationGenerator.generateExplanation(paths, type);
			
			Map<String, Object> response = new HashMap<>();
			response.put("explanation", explanation);
			response.put("query", query);
			response.put("paths_found", paths.size());
			
			return ResponseEntity.ok(response);
			
		} catch(Exception e) {
			log.error("Error during explanation", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dynamic knowledge update
	 */
	@Operation(summary = "Update knowledge", 
		description = "Add new knowledge to the graph at runtime")
	@PostMapping("/knowledge/update")
	public ResponseEntity<Map<String, Object>> updateKnowledge(@RequestBody Map<String, Object> request) {
		log.info("Knowledge update request");
		
		try {
			String source = (String) request.get("source");
			String target = (String) request.get("target");
			double weight = ((Number) request.getOrDefault("weight", 0.5)).doubleValue();
			String sourceId = (String) request.getOrDefault("source_id", "api");
			String strategyStr = (String) request.getOrDefault("conflict_strategy", "AVERAGE");
			
			KnowledgeIntegrator.ConflictResolution strategy;
			try {
				strategy = KnowledgeIntegrator.ConflictResolution.valueOf(strategyStr);
			} catch(IllegalArgumentException e) {
				strategy = KnowledgeIntegrator.ConflictResolution.AVERAGE;
			}
			
			// Validate
			boolean valid = knowledgeIntegrator.validateKnowledge(source, target, weight);
			if(!valid) {
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "Invalid knowledge fact");
				return ResponseEntity.badRequest().body(errorResponse);
			}
			
			// Add knowledge
			boolean success = knowledgeIntegrator.addKnowledge(source, target, weight, 
				sourceId, strategy);
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", success);
			response.put("source", source);
			response.put("target", target);
			response.put("weight", weight);
			
			return ResponseEntity.ok(response);
			
		} catch(Exception e) {
			log.error("Error during knowledge update", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * System health and capabilities
	 */
	@Operation(summary = "System health", 
		description = "Get system status and capabilities")
	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> health() {
		Map<String, Object> health = new HashMap<>();
		
		health.put("status", "operational");
		health.put("version", "2.0.0");
		
		Map<String, Boolean> features = new HashMap<>();
		features.put("reasoning", true);
		features.put("chat", true);
		features.put("generation", true);
		features.put("explanation", true);
		features.put("knowledge_update", true);
		features.put("multi_turn_conversation", true);
		features.put("graph_reasoning", true);
		
		health.put("features", features);
		
		// Graph statistics
		Map<String, Object> stats = new HashMap<>();
		long featureCount = featureRepository.count();
		stats.put("total_features", featureCount);
		
		health.put("statistics", stats);
		
		return ResponseEntity.ok(health);
	}
	
	/**
	 * Gets a general vocabulary for text generation
	 * This should be populated with common words and domain-specific terms
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


