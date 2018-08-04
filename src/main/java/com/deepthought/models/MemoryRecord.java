package com.deepthought.models;

import java.util.Date;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

/**
 * 
 */
@NodeEntity
public class MemoryRecord {

	@Id 
	@GeneratedValue 
	private Long id;
	
	@Property
	private Date date;

	@Relationship(type = "HAS_VOCABULARY")
	private Vocabulary input_vocabulary;
	
	@Relationship(type = "HAS_VOCABULARY")
	private Vocabulary output_vocabulary;
	
	@Relationship(type = "REWARDED")
	private Feature rewarded_feature;
	
	private List<String> input_feature_keys;
	private List<String> output_feature_keys;
	
	private double[][] policy_matrix;
	private double[] prediction;
	
	public MemoryRecord(){
		setDate(new Date());
	}
	
	public Vocabulary getInputVocabulary(){
		return this.input_vocabulary;
	}
	
	public void setInputVocabulary(Vocabulary vocab){
		this.input_vocabulary = vocab;
	}
	
	public Vocabulary getOutputVocabulary(){
		return this.output_vocabulary;
	}
	
	public void setOutputVocabulary(Vocabulary vocab){
		this.output_vocabulary = vocab;
	}
	
	public Long getID() {
		return id;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Feature getRewardedFeature() {
		return rewarded_feature;
	}

	public void setRewardedFeature(Feature rewarded_feature) {
		this.rewarded_feature = rewarded_feature;
	}

	public double[] getPrediction() {
		return prediction;
	}

	public void setPrediction(double[] prediction) {
		this.prediction = prediction;
	}

	public List<String> getInputFeatureValues() {
		return input_feature_keys;
	}

	public void setInputFeatureValues(List<String> input_feature_keys) {
		this.input_feature_keys = input_feature_keys;
	}

	public List<String> getOutputFeatureKeys() {
		return output_feature_keys;
	}

	public void setOutputFeatureKeys(List<String> output_feature_keys) {
		this.output_feature_keys = output_feature_keys;
	}

	public double[][] getPolicyMatrix() {
		return policy_matrix;
	}

	public void setPolicyMatrix(double[][] policy_matrix) {
		this.policy_matrix = policy_matrix;
	}
}
