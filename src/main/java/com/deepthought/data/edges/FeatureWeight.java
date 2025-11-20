package com.deepthought.data.edges;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.data.models.Feature;
import com.deepthought.data.models.Vocabulary;

/**
 * Acts as a relationship between 2 {@link Feature} nodes within the graph and
 * holds the most recent policy/model weight vector.
 *
 * <p>The weight vector is a map of weights for the feature relationships across different {@link Vocabulary}s.
 * Each vocabulary label maps to its associated weight value.
 */
@RelationshipEntity(type = "HAS_RELATED_FEATURE")
public class FeatureWeight {
	@Id
	@GeneratedValue
	private Long id;
    
	@Property
    private Map<String, Double> vocabularyWeights;

	@Property
	private double weight;
    
	@StartNode
	private Feature input_feature;
    
	@EndNode
	private Feature result_feature;

	public FeatureWeight() {
		this.vocabularyWeights = new HashMap<>();
	}

	public long getId(){
		return this.id;
	}
	
	public Feature getInputFeature(){
		return this.input_feature;
	}
	
	public void setInputFeature(Feature input_feature){
		this.input_feature = input_feature;
	}
	
    public Feature getResultFeature(){
		return this.result_feature;
    }
    
    public void setResultFeature(Feature result_feature){
		this.result_feature = result_feature;
    }
    
    /**
     * Gets the weight for a specific vocabulary label.
     * 
     * @param vocabularyLabel The label of the vocabulary
     * @return The weight for the vocabulary, or 0.0 if not found
     */
    public double getVocabularyWeight(String vocabularyLabel){
		if (vocabularyWeights == null) {
			vocabularyWeights = new HashMap<>();
		}
		return vocabularyWeights.getOrDefault(vocabularyLabel, 0.0);
    }
    
    /**
     * Sets the weight for a specific vocabulary label.
     * 
     * @param vocabularyLabel The label of the vocabulary
     * @param weight The weight value to set
     */
    public void setVocabularyWeight(String vocabularyLabel, double weight){
		if (vocabularyWeights == null) {
			vocabularyWeights = new HashMap<>();
		}
		vocabularyWeights.put(vocabularyLabel, weight);
    }
    
    /**
     * Gets the complete map of vocabulary labels to weights.
     * 
     * @return A map where keys are vocabulary labels and values are weights
     */
    public Map<String, Double> getVocabularyWeights(){
		if (vocabularyWeights == null) {
			vocabularyWeights = new HashMap<>();
		}
		return vocabularyWeights;
    }
    
    /**
     * Sets the complete map of vocabulary labels to weights.
     * 
     * @param weights A map where keys are vocabulary labels and values are weights
     */
    public void setVocabularyWeights(Map<String, Double> vocabularyWeights){
		this.vocabularyWeights = vocabularyWeights != null ? vocabularyWeights : new HashMap<>();
    }

	public double getWeight(){
		return this.weight;
	}

	public void setWeight(double weight){
		this.weight = weight;
	}
}
