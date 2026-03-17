package com.deepthought.models.edges;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.Token;
import com.deepthought.models.MemoryRecord;

/**
 *
 */
@RelationshipEntity(type = "TOKEN_POLICY")
public class TokenPolicy {
	@Id
	@GeneratedValue
	private Long relationshipId;

	@Property
	private List<String> policy_tokens = new ArrayList<String>();

	@Property
	private List<Double> policy_weights = new ArrayList<Double>();

	@Property
    private double reward;

	@StartNode
	private MemoryRecord memory;

	@EndNode
	private Token token;

    public MemoryRecord getMemoryRecord(){
    	return this.memory;
    }

    public void setMemoryRecord(MemoryRecord memory){
    	this.memory = memory;
    }


    public List<String> getPolicyTokens(){
    	return this.policy_tokens;
    }

    public void setPolicyTokens(List<String> policy_tokens){
    	this.policy_tokens = policy_tokens;
    }

    public List<Double> getPolicyWeights(){
    	return this.policy_weights;
    }

    public void setPolicyWeights(List<Double> policy_weights){
    	this.policy_weights = policy_weights;
    }

	public void setToken(Token token) {
		this.token = token;
	}

	public double getReward(){
    	return this.reward;
    }

    public void setReward(double reward){
    	this.reward = reward;
    }
}
