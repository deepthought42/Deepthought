package com.deepthought.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.constraints.NotBlank;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A Vocabulary is a list of words or tokens that are essentially labels for each index 
 * in a weight vector that is used in machine learning. It maintains a consistent mapping
 * between words/tokens and their indices for use in feature vectors and weight matrices.
 * 
 * This class provides:
 * - Ordered storage of vocabulary items
 * - Bidirectional mapping (word -> index, index -> word)
 * - Thread-safe operations for concurrent access
 * - Integration with the graph-based learning system
 */
@Node
public class Vocabulary {

    @Schema(description = "Unique identifier of the Vocabulary.", example = "1", required = true)
    @Id 
    @GeneratedValue 
    private Long id;
    
    @Schema(description = "Vocabulary name/label", example = "web_elements", required = true)
    @NotBlank
    @Property
    private String label;
    
    @Schema(description = "List of vocabulary items (words/tokens)", required = true)
    @Property
    private List<String> valueList;
    
    @Schema(description = "Mapping from word to index for fast lookup")
    @JsonIgnore
    private transient Map<String, Integer> wordToIndexMap;
    
    @Schema(description = "Current size of the vocabulary")
    @Property
    private int size;
    
    @Schema(description = "Thread-safe counter for generating new indices")
    @JsonIgnore
    private transient AtomicInteger nextIndex;
    
    /**
     * Default constructor for Neo4j
     */
    public Vocabulary() {
        this.valueList = new ArrayList<>();
        this.wordToIndexMap = new HashMap<>();
        this.nextIndex = new AtomicInteger(0);
        this.size = 0;
    }
    
    /**
     * Creates a new vocabulary with the given label
     * 
     * @param label The name/label for this vocabulary
     */
    public Vocabulary(String label) {
        this();
        this.label = label;
    }
    
    /**
     * Creates a new vocabulary with the given label and initial features
     * 
     * @param features Initial list of features to add to the vocabulary
     * @param label The name/label for this vocabulary
     */
    public Vocabulary(List<Feature> features, String label) {
        this(label);
        if (features != null) {
            for (Feature feature : features) {
                this.appendToVocabulary(feature);
            }
        }
    }
    
    /**
     * Adds a word/token to the vocabulary if it doesn't already exist
     * 
     * @param word The word/token to add
     * @return The index of the word (existing or newly assigned)
     */
    public synchronized int addWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw new IllegalArgumentException("Word cannot be null or empty");
        }
        
        String normalizedWord = word.trim().toLowerCase();
        
        if (wordToIndexMap.containsKey(normalizedWord)) {
            return wordToIndexMap.get(normalizedWord);
        }
        
        int index = nextIndex.getAndIncrement();
        valueList.add(normalizedWord);
        wordToIndexMap.put(normalizedWord, index);
        size = valueList.size();
        
        return index;
    }
    
    /**
     * Adds a Feature to the vocabulary by its value
     * 
     * @param feature The feature to add
     * @return The index of the feature's value
     */
    public int appendToVocabulary(Feature feature) {
        if (feature == null || feature.getValue() == null) {
            throw new IllegalArgumentException("Feature and its value cannot be null");
        }
        return addWord(feature.getValue());
    }
    
    /**
     * Gets the index of a word in the vocabulary
     * 
     * @param word The word to look up
     * @return The index of the word, or -1 if not found
     */
    public int getIndex(String word) {
        if (word == null) {
            return -1;
        }
        return wordToIndexMap.getOrDefault(word.trim().toLowerCase(), -1);
    }
    
    /**
     * Gets the word at a specific index
     * 
     * @param index The index to look up
     * @return The word at the index, or null if index is out of bounds
     */
    public String getWord(int index) {
        if (index < 0 || index >= valueList.size()) {
            return null;
        }
        return valueList.get(index);
    }
    
    /**
     * Checks if a word exists in the vocabulary
     * 
     * @param word The word to check
     * @return true if the word exists, false otherwise
     */
    public boolean contains(String word) {
        if (word == null) {
            return false;
        }
        return wordToIndexMap.containsKey(word.trim().toLowerCase());
    }
    
    /**
     * Gets the current size of the vocabulary
     * 
     * @return The number of words in the vocabulary
     */
    public int size() {
        return size;
    }
    
    /**
     * Gets the list of all words in the vocabulary
     * 
     * @return A copy of the vocabulary list
     */
    public List<String> getWords() {
        return new ArrayList<>(valueList);
    }
    
    /**
     * Gets the list of features corresponding to the vocabulary words
     * 
     * @return A list of Feature objects
     */
    public List<Feature> getFeatures() {
        List<Feature> features = new ArrayList<>();
        for (String word : valueList) {
            features.add(new Feature(word));
        }
        return features;
    }
    
    /**
     * Creates a feature vector representation of the vocabulary
     * where each position corresponds to a word in the vocabulary
     * 
     * @param inputWords The words to represent in the vector
     * @return A boolean array where true indicates the word is present
     */
    public boolean[] createFeatureVector(List<String> inputWords) {
        boolean[] vector = new boolean[size];
        
        for (String word : inputWords) {
            int index = getIndex(word);
            if (index >= 0) {
                vector[index] = true;
            }
        }
        
        return vector;
    }
    
    /**
     * Creates a sparse vector representation where only present words are marked
     * 
     * @param inputWords The words to represent
     * @return A map of word indices to their presence (1.0 for present, 0.0 for absent)
     */
    public Map<Integer, Double> createSparseVector(List<String> inputWords) {
        Map<Integer, Double> sparseVector = new HashMap<>();
        
        for (String word : inputWords) {
            int index = getIndex(word);
            if (index >= 0) {
                sparseVector.put(index, 1.0);
            }
        }
        
        return sparseVector;
    }
    
    /**
     * Initializes the word-to-index mapping from the stored value list
     * This method should be called after loading from Neo4j
     */
    public void initializeMappings() {
        if (wordToIndexMap == null) {
            wordToIndexMap = new HashMap<>();
        }
        if (nextIndex == null) {
            nextIndex = new AtomicInteger(0);
        }
        
        wordToIndexMap.clear();
        for (int i = 0; i < valueList.size(); i++) {
            wordToIndexMap.put(valueList.get(i), i);
        }
        nextIndex.set(valueList.size());
        size = valueList.size();
    }
    
    /**
     * Clears the vocabulary
     */
    public void clear() {
        valueList.clear();
        if (wordToIndexMap != null) {
            wordToIndexMap.clear();
        }
        if (nextIndex != null) {
            nextIndex.set(0);
        }
        size = 0;
    }
    
    // Getters and Setters for Neo4j persistence
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public List<String> getValueList() {
        return new ArrayList<>(valueList);
    }
    
    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
        initializeMappings(); // Rebuild mappings when setting from Neo4j
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    @Override
    public String toString() {
        return String.format("Vocabulary{label='%s', size=%d, words=%s}", 
                           label, size, valueList);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vocabulary)) return false;
        
        Vocabulary other = (Vocabulary) obj;
        return label != null ? label.equals(other.label) : other.label == null;
    }
    
    @Override
    public int hashCode() {
        return label != null ? label.hashCode() : 0;
    }
}
