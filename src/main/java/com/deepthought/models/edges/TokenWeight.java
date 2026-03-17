package com.deepthought.models.edges;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.Token;

/**
 * Acts as a relationship between 2 {@link Token} nodes within the graph and
 * 	holds the most recent policy/model weight for the token relationship
 */
@RelationshipEntity(type = "HAS_RELATED_TOKEN")
public class TokenWeight {
	@Id
	@GeneratedValue
	private Long id;

	@Property
    private double weight;

	@StartNode
	private Token token;

	@EndNode
	private Token end_token;

	public long getId(){
		return this.id;
	}

    public Token getEndToken(){
    	return this.end_token;
    }

    public void setEndToken(Token token){
    	this.end_token = token;
    }

    public double getWeight(){
    	return this.weight;
    }

    public void setWeight(double weight){
    	this.weight = weight;
    }

	public void setToken(Token token) {
		this.token = token;
	}

	public Token getToken() {
		return this.token;
	}
}
