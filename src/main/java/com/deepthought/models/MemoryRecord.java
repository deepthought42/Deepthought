package com.deepthought.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import com.deepthought.models.edges.Prediction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Stores all data for making predictions and learning from feedback. This node
 *  also connects to {@link Token}s through a {@link Prediction} relationship/edge
 *  that contains the predicted weight for the token that this memory stores.
 */
@NodeEntity
public class MemoryRecord {

	@Id
	@GeneratedValue
	private Long id;

	@Property
	private Date date;

	@Relationship(type = "DESIRED_TOKEN")
	private Token desired_token;

	@Relationship(type = "PREDICTED")
	private Token predicted_token;

	@Relationship(type = "PREDICTION", direction = Relationship.OUTGOING)
	private List<Prediction> predictions;

	private List<String> input_token_values;
	private String[] output_token_values;

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

	public Token getPredictedToken() {
		return this.predicted_token;
	}

	public void setPredictedToken(Token predicted_token){
		this.predicted_token = predicted_token;
	}

	public List<String> getInputTokenValues() {
		return input_token_values;
	}

	public void setInputTokenValues(List<String> input_token_values) {
		this.input_token_values = input_token_values;
	}

	public String[] getOutputTokenKeys() {
		return output_token_values;
	}

	public void setOutputTokenKeys(String[] output_token_values) {
		this.output_token_values = output_token_values;
	}

	public double[][] getPolicyMatrix() {
        Gson gson = new GsonBuilder().create();

		return gson.fromJson(policy_matrix_json, double[][].class);
	}

	public void setPolicyMatrix(double[][] policy_matrix) {
        Gson gson = new GsonBuilder().create();

		this.policy_matrix_json = gson.toJson(policy_matrix);
	}

	public Token getDesiredToken() {
		return desired_token;
	}

	public void setDesiredToken(Token token) {
		desired_token = token;
	}
}
