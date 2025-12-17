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

/**
 * Service that encapsulates signal processing logic used by the {@code SignalsController}.
 *
 * Preconditions:
 * - All required collaborators (reasoning engine, text generator, explanation generator,
 *   conversation manager and feature repository) are available and injected by Spring.
 *
 * Postconditions:
 * - Does not persist or mutate state outside of the conversation context managed by {@link ConversationManager}.
 * - Produces {@link SignalResponse} instances that reflect the processing of the provided {@link SignalRequest}.
 */
@Service
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

    /**
     * Processes a signal request and produces a response.
     *
     * Preconditions:
     * - {@code request} is non-null.
     * - Collaborating services such as {@link ConversationManager}, {@link GraphReasoningEngine}
     *   and {@link TextGenerator} are operational.
     *
     * Postconditions:
     * - Returns a non-null {@link SignalResponse} describing the outcome of processing.
     * - May append user and assistant messages to the conversation identified by the session id.
     *
     * @param request signal request containing text and optional signal references
     * @return a populated {@link SignalResponse} instance
     */
    public SignalResponse processSignals(SignalRequest request) {
        assert request != null : "SignalRequest must not be null";
        
        log.info("Processing signal request");

        String sessionId = getOrCreateSession(request);
        String combinedText = buildCombinedText(request);

        recordUserMessage(sessionId, combinedText);

        List<Feature> contextFeatures = buildContextFeatures(sessionId, combinedText);
        GenerationConfig config = new GenerationConfig();
        List<ReasoningPath> reasoningPaths = performReasoning(contextFeatures, config);

        String generatedText = generateResponseText(contextFeatures, reasoningPaths, config);
        double confidence = computeAverageConfidence(reasoningPaths);
        List<String> sources = buildSources(reasoningPaths);

        recordAssistantMessage(sessionId, generatedText);

        SignalResponse result = buildSignalResponse(sessionId, generatedText, confidence, sources);
        assert result != null : "SignalResponse must not be null";
        return result;
    }

    /**
     * Builds a combined textual representation of all signals in the request.
     *
     * Preconditions:
     * - {@code request} is non-null.
     *
     * Postconditions:
     * - Returns a non-null string (possibly empty) concatenating text and signal descriptors.
     *
     * @param request signal request to summarise
     * @return combined textual representation of the request
     */
    private String buildCombinedText(SignalRequest request) {
        assert request != null : "SignalRequest must not be null";
        
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

        String result = combinedText.toString();
        assert result != null : "Combined text must not be null";
        return result;
    }

    /**
     * Resolves or creates a session for the given request.
     *
     * Preconditions:
     * - {@code request} is non-null.
     *
     * Postconditions:
     * - Returns a non-null session identifier that can be used to track conversation state.
     *
     * @param request signal request carrying an optional session id
     * @return a valid session identifier
     */
    private String getOrCreateSession(SignalRequest request) {
        assert request != null : "SignalRequest must not be null";
        
        String result = conversationManager.getOrCreateSession(request.getSessionId());
        assert result != null && !result.isEmpty() : "Session ID must not be null or empty";
        return result;
    }

    /**
     * Records a user message in the conversation history if content is present.
     *
     * Preconditions:
     * - {@code sessionId} is non-null and refers to an existing or new conversation.
     *
     * Postconditions:
     * - If {@code content} is non-null and non-empty, a user {@link ChatMessage} is appended
     *   to the conversation; otherwise no changes are made.
     *
     * @param sessionId identifier of the conversation session
     * @param content message content to record, may be null or empty
     */
    private void recordUserMessage(String sessionId, String content) {
        assert sessionId != null : "Session ID must not be null";
        
        if(content == null || content.isEmpty()) {
            return;
        }

        ChatMessage userMessage = new ChatMessage("user", content);
        conversationManager.addMessage(sessionId, userMessage);
    }

    /**
     * Builds context features from the conversation history and current content.
     *
     * Preconditions:
     * - {@code sessionId} is non-null.
     *
     * Postconditions:
     * - Returns a non-null list of {@link Feature} instances representing context.
     * - If {@code content} cannot be decomposed, only historical context features are returned.
     *
     * @param sessionId identifier of the conversation session
     * @param content current message content to incorporate into context, may be null
     * @return list of context features
     */
    private List<Feature> buildContextFeatures(String sessionId, String content) {
        assert sessionId != null : "Session ID must not be null";
        
        List<Feature> contextFeatures = conversationManager.getContextFeatures(sessionId, 30);
        if(content != null && !content.isEmpty()) {
            try {
                contextFeatures.addAll(DataDecomposer.decompose(content));
            } catch(Exception e) {
                // If the text cannot be decomposed, continue with existing context
                log.error("Error decomposing signal text", e);
            }
        }
        
        assert contextFeatures != null : "Context features must not be null";
        return contextFeatures;
    }

    /**
     * Performs graph reasoning based on the supplied context and configuration.
     *
     * Preconditions:
     * - {@code contextFeatures} and {@code config} are non-null.
     *
     * Postconditions:
     * - Returns a non-null list of {@link ReasoningPath} instances, possibly empty.
     *
     * @param contextFeatures features providing reasoning context
     * @param config generation and reasoning configuration
     * @return list of reasoning paths
     */
    private List<ReasoningPath> performReasoning(List<Feature> contextFeatures, GenerationConfig config) {
        assert contextFeatures != null : "Context features must not be null";
        assert config != null : "Generation config must not be null";
        
        List<ReasoningPath> result = reasoningEngine.reason(
            contextFeatures,
            config.getMaxHops(),
            config.getMinConfidence()
        );
        
        assert result != null : "Reasoning paths must not be null";
        return result;
    }

    /**
     * Generates a response text from context, reasoning paths and configuration.
     *
     * Preconditions:
     * - {@code contextFeatures}, {@code paths} and {@code config} are non-null.
     *
     * Postconditions:
     * - Returns a non-null string (possibly empty) representing the generated response.
     *
     * @param contextFeatures features providing generation context
     * @param paths reasoning paths used to select relevant features
     * @param config generation configuration
     * @return generated response text
     */
    private String generateResponseText(
        List<Feature> contextFeatures,
        List<ReasoningPath> paths,
        GenerationConfig config
    ) {
        assert contextFeatures != null : "Context features must not be null";
        assert paths != null : "Reasoning paths must not be null";
        assert config != null : "Generation config must not be null";
        
        List<Feature> relevantFeatures = new ArrayList<>();
        for(ReasoningPath path : paths) {
            if(path != null && path.getFeatures() != null) {
                relevantFeatures.addAll(path.getFeatures());
            }
        }

        List<Feature> vocabulary = getGenerationVocabulary();
        String result = textGenerator.generateWithReasoning(
            contextFeatures,
            relevantFeatures,
            vocabulary,
            config
        );
        
        assert result != null : "Generated text must not be null";
        return result;
    }

    /**
     * Computes an average confidence score from the given reasoning paths.
     *
     * Preconditions:
     * - {@code paths} is non-null.
     *
     * Postconditions:
     * - Returns a confidence value greater than or equal to zero.
     * - Returns 0.0 when the list of paths is empty.
     *
     * @param paths reasoning paths used to derive confidence
     * @return average confidence score
     */
    private double computeAverageConfidence(List<ReasoningPath> paths) {
        assert paths != null : "Reasoning paths must not be null";
        
        if(paths.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        int validPathCount = 0;
        for(ReasoningPath path : paths) {
            if(path != null) {
                double confidence = path.getTotalConfidence();
                // Ensure confidence is non-negative by taking absolute value
                total += Math.abs(confidence);
                validPathCount++;
            }
        }
        
        // Ensure divisor is never zero by checking validPathCount
        double result = (validPathCount > 0) ? (total / validPathCount) : 0.0;
        
        assert result >= 0.0 : "Confidence must be non-negative, got: " + result;
        return result;
    }

    /**
     * Builds a list of source descriptors from reasoning paths.
     *
     * Preconditions:
     * - {@code paths} is non-null.
     *
     * Postconditions:
     * - Returns a non-null list of source descriptors, possibly empty.
     *
     * @param paths reasoning paths used to derive sources
     * @return list of source descriptors
     */
    private List<String> buildSources(List<ReasoningPath> paths) {
        assert paths != null : "Reasoning paths must not be null";
        
        List<String> result = explanationGenerator.generateSourcesList(paths);
        assert result != null : "Sources list must not be null";
        return result;
    }

    /**
     * Records an assistant message in the conversation history if text is present.
     *
     * Preconditions:
     * - {@code sessionId} is non-null and refers to an existing or new conversation.
     *
     * Postconditions:
     * - If {@code generatedText} is non-null and non-empty, an assistant {@link ChatMessage}
     *   is appended to the conversation; otherwise no changes are made.
     *
     * @param sessionId identifier of the conversation session
     * @param generatedText assistant message content, may be null or empty
     */
    private void recordAssistantMessage(String sessionId, String generatedText) {
        assert sessionId != null : "Session ID must not be null";
        
        if(generatedText == null || generatedText.isEmpty()) {
            return;
        }

        ChatMessage assistantMessage = new ChatMessage("assistant", generatedText);
        conversationManager.addMessage(sessionId, assistantMessage);
    }

    /**
     * Builds a {@link SignalResponse} from the supplied components.
     *
     * Preconditions:
     * - {@code sessionId} is non-null.
     * - {@code sources} is non-null.
     *
     * Postconditions:
     * - Returns a non-null {@link SignalResponse} with fields set to the provided values.
     *
     * @param sessionId identifier of the conversation session
     * @param generatedText generated response text, may be null or empty
     * @param confidence average confidence score
     * @param sources list of source descriptors
     * @return a populated {@link SignalResponse}
     */
    private SignalResponse buildSignalResponse(
        String sessionId,
        String generatedText,
        double confidence,
        List<String> sources
    ) {
        assert sessionId != null : "Session ID must not be null";
        assert sources != null : "Sources list must not be null";
        // Ensure confidence is non-negative
        double normalizedConfidence = Math.abs(confidence);
        
        SignalResponse response = new SignalResponse();
        response.setSessionId(sessionId);
        response.setOutputText(generatedText);
        response.setConfidence(normalizedConfidence);
        response.setSources(sources);
        
        assert response != null : "SignalResponse must not be null";
        return response;
    }

    /**
     * Gets a general vocabulary for text generation.
     *
     * Preconditions:
     * - {@link FeatureRepository} is available and operational.
     *
     * Postconditions:
     * - Returns a non-null list of {@link Feature} instances that includes common words
     *   and up to 100 domain-specific terms from the repository.
     *
     * @return vocabulary used for text generation
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
        if(featureRepository != null) {
            featureRepository.findAll().forEach(dbFeatures::add);
        }

        // Add up to 100 features from database
        int dbLimit = Math.min(100, Math.max(0, dbFeatures.size()));
        for(int i = 0; i < dbLimit; i++) {
            Feature f = dbFeatures.get(i);
            if(f != null && !vocab.contains(f)) {
                vocab.add(f);
            }
        }

        assert vocab != null : "Vocabulary must not be null";
        return vocab;
    }
}
