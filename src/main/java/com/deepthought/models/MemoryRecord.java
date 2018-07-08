package com.deepthought.models;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import com.deepthought.models.edges.FeatureReward;

/**
 * 
 */
@NodeEntity
public class MemoryRecord {

	@Id 
	@GeneratedValue 
	private Long id;
	
	@Property
	private String key;

	@Relationship(type = "HAS_START_VOCABULARY")
	private Vocabulary start_vocabulary;
	
	@Relationship(type = "HAS_END_VOCABULARY")
	private Vocabulary end_vocabulary;
	
	@Relationship(type = "REWARDED")
	private FeatureReward rewarded_feature;
	
	@Property
	private List<String> start_feature_keys = new ArrayList<String>();
	
	@Property
	private List<String> end_feature_keys = new ArrayList<String>();
	
	@Property
	private double[][] feature_policy;
	
	@Property
	private double[] prediction;
	
	public MemoryRecord (){}
	
	public MemoryRecord (Vocabulary start_vocabulary, List<String> start_vocab_feature_keys, 
						 Vocabulary end_vocabulary, List<String> end_vocab_feature_keys,
						 double[][] feature_policy, double[] prediction, FeatureReward rewarded_feature){
		setStartVocabulary(start_vocabulary);
		setStartFeatureKeys(start_vocab_feature_keys);
		setEndVocabulary(end_vocabulary);
		setEndFeatureKeys(end_vocab_feature_keys);
		setFeaturePolicy(feature_policy);
		setPrediction(prediction);
		setRewardedFeature(rewarded_feature);
	}
	
	public Vocabulary getStartVocabulary(){
		return this.start_vocabulary;
	}
	
	public void setStartVocabulary(Vocabulary vocab){
		this.start_vocabulary = vocab;
	}
	
	public Vocabulary getEndVocabulary(){
		return this.end_vocabulary;
	}
	
	public void setEndVocabulary(Vocabulary vocab){
		this.end_vocabulary = vocab;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public FeatureReward getRewardedFeature() {
		return rewarded_feature;
	}

	public void setRewardedFeature(FeatureReward rewarded_feature) {
		this.rewarded_feature = rewarded_feature;
	}

	public List<String> getStartFeatureKeys() {
		return start_feature_keys;
	}

	public void setStartFeatureKeys(List<String> feature_keys) {
		this.start_feature_keys = feature_keys;
	}
	
	public List<String> getEndFeatureKeys() {
		return end_feature_keys;
	}

	public void setEndFeatureKeys(List<String> feature_keys) {
		this.end_feature_keys = feature_keys;
	}

	public double[] getPrediction() {
		return prediction;
	}

	public void setPrediction(double[] prediction) {
		this.prediction = prediction;
	}

	public double[][] getFeaturePolicy() {
		return feature_policy;
	}

	public void setFeaturePolicy(double[][] feature_policy) {
		this.feature_policy = feature_policy;
	}
}
