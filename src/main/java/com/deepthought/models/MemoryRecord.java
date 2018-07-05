package com.deepthought.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import com.deepthought.models.edges.ActionReward;
import com.deepthought.models.edges.FeaturePolicy;
import com.deepthought.models.edges.Prediction;

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

	@Relationship(type = "HAS_VOCABULARY")
	private Vocabulary vocabulary;
	
	@Relationship(type = "REWARDED")
	private ActionReward rewarded_action;
	
	@Relationship(type = "FEATURE_POLICY")
	private Set<FeaturePolicy> feature_policies = new HashSet<FeaturePolicy>();
	
	@Relationship(type = "PREDICTION")
	private Set<Prediction> action_prediction = new HashSet<Prediction>();
	
	public Vocabulary getVocabulary(){
		return this.vocabulary;
	}
	
	public void setVocabulary(Vocabulary vocab){
		this.vocabulary = vocab;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public ActionReward getRewardedAction() {
		return rewarded_action;
	}

	public void setRewardedAction(ActionReward rewarded_action) {
		this.rewarded_action = rewarded_action;
	}

	public Set<FeaturePolicy> getFeaturePolicies() {
		return feature_policies;
	}

	public void setFeaturePolicies(Set<FeaturePolicy> feature_policies) {
		this.feature_policies = feature_policies;
	}

	public Set<Prediction> getActionPrediction() {
		return action_prediction;
	}

	public void setActionPrediction(Set<Prediction> action_prediction) {
		this.action_prediction = action_prediction;
	}
}
