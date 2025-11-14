package com.deepthought.data.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import com.deepthought.data.edges.Prediction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Stores all data for making predictions and learning from feedback. This node
 *  also connects to {@link Feature}s through a {@link Prediction} relationship/edge
 *  that contains the predicted weight for the feature that this memory stores.
 */
@NodeEntity
public class MemoryRecord {

	@Id 
	@GeneratedValue 
	private Long id;
	
	@Property
	private Date date;

	@Relationship(type = "DESIRED_FEATURE")
	private Feature desired_feature;
	
	@Relationship(type = "PREDICTED")
	private Feature predicted_feature;
	
	@Relationship(type = "PREDICTION", direction = Relationship.OUTGOING)
	private List<Prediction> predictions;
	
	private List<String> input_feature_values;
	private String[] output_feature_values;
	
	private String policy_matrix_json;
	
	public MemoryRecord(){
		setDate(new Date());
		policy_matrix_json = "";
		setPredictions( new ArrayList<>() );
	}
	
	public void setPredictions(List<Prediction> prediction_edges) {
		this.predictions = prediction_edges;
	}
	
	public List<Prediction> getPredictions(){
		return this.predictions;
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
	
	public Feature getPredictedFeature() {
		return this.predicted_feature;
	}
	
	public void setPredictedFeature(Feature predicted_feature){
		this.predicted_feature = predicted_feature;
	}

	public List<String> getInputFeatureValues() {
		return input_feature_values;
	}

	public void setInputFeatureValues(List<String> input_feature_values) {
		this.input_feature_values = input_feature_values;
	}

	public String[] getOutputFeatureKeys() {
		return output_feature_values;
	}

	public void setOutputFeatureKeys(String[] output_feature_values) {
		this.output_feature_values = output_feature_values;
	}

	public double[][] getPolicyMatrix() {
        Gson gson = new GsonBuilder().create();

		return gson.fromJson(policy_matrix_json, double[][].class);
	}

	public void setPolicyMatrix(double[][] policy_matrix) {
        Gson gson = new GsonBuilder().create();

		this.policy_matrix_json = gson.toJson(policy_matrix);
	}

	public Feature getDesiredFeature() {
		return desired_feature;
	}

	public void setDesiredFeature(Feature feature) {
		desired_feature = feature;
	}
}
