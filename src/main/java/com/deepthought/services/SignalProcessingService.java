package com.deepthought.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deepthought.api.v2.dto.ChatMessage;
import com.deepthought.api.v2.dto.GenerationConfig;
import com.deepthought.api.v2.dto.SignalRequest;
import com.deepthought.api.v2.dto.SignalResponse;
import com.deepthought.brain.ExplanationGenerator;
import com.deepthought.brain.GraphReasoningEngine;
import com.deepthought.brain.TextGenerator;
import com.deepthought.conversation.ConversationManager;
import com.deepthought.data.db.DataDecomposer;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.ReasoningPath;
import com.deepthought.data.repository.FeatureRepository;

import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates signal processing logic used by the SignalsController.
 */
@Service
@Getter
@Setter
public class SignalProcessingService {

    private static final Logger log = LoggerFactory.getLogger(SignalProcessingService.class);

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

    public SignalResponse processSignals(SignalRequest request) {
        log.info("Processing signal request");

        SignalResponse response = new SignalResponse();

        // 1. Establish or create a session for continuity (if provided)
        String sessionId = conversationManager.getOrCreateSession(request.getSessionId());
        response.setSessionId(sessionId);

        // 2. Combine text with basic references to other signals so they can influence processing
        String finalText = buildCombinedText(request);

        // 3. Record the incoming message in the conversation history
        if(finalText != null && !finalText.isEmpty()) {
            ChatMessage userMessage = new ChatMessage("user", finalText);
            conversationManager.addMessage(sessionId, userMessage);
        }

        // 4. Build context from recent conversation plus current content
        List<Feature> contextFeatures = conversationManager.getContextFeatures(sessionId, 30);
        if(finalText != null && !finalText.isEmpty()) {
            try {
                contextFeatures.addAll(DataDecomposer.decompose(finalText));
            } catch(Exception e) {
                // If the text cannot be decomposed, continue with existing context
                log.error("Error decomposing signal text", e);
            }
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

        return response;
    }

    private String buildCombinedText(SignalRequest request) {
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

        return combinedText.toString();
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
