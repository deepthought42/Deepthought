package com.deepthought.models.edges;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.Token;
import com.deepthought.models.MemoryRecord;

/**
 * Encapsulates data for a predicted weight for a result token in connection with a {@link MemoryRecord}
 */
@RelationshipEntity(type = "PREDICTION")
public class Prediction {
	@Id
	@GeneratedValue
	private Long relationshipId;

	@Property
    private double weight;

	@StartNode
	private MemoryRecord memory;

	@EndNode
	private Token result_token;

    public Prediction(MemoryRecord memory, Token token, double weight) {
		setMemoryRecord(memory);
		setToken(token);
		setWeight(weight);
	}

	public Token getToken(){
    	return this.result_token;
    }

    public void setToken(Token result_token){
    	this.result_token = result_token;
    }

    public double getWeight(){
    	return this.weight;
    }

    public void setWeight(double weight){
    	this.weight = weight;
    }

	public void setMemoryRecord(MemoryRecord memory) {
		this.memory = memory;
	}
}
