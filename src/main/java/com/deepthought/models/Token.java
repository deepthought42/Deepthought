package com.deepthought.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.deepthought.models.edges.TokenWeight;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Defines objects that are available to the system for learning against
 */
@NodeEntity
public class Token {

	@Schema(description = "Unique identifier of the Token.", example = "1", required = true)
	@Id
	@GeneratedValue
	private Long id;

	@Schema(description = "Token label", example = "form", required = true)
    @NotBlank
	private String value;

	@Relationship(type = "HAS_RELATED_TOKEN")
	private List<TokenWeight> token_weights = new ArrayList<TokenWeight>();

	public Token(){}

	/**
	 * Instantiates a new object definition
	 *
	 * @param uid
	 * @param value
	 * @param type
	 * @param tokens
	 *
	 * @pre tokens != null
	 */
	public Token(String value, List<TokenWeight> tokens) {
		assert tokens != null;

		this.value = value;
		this.token_weights = tokens;
	}

	/**
	 *
	 *
	 * @param value
	 * @param type
	 */
	public Token(String value) {
		this.value = value;
		this.token_weights = new ArrayList<TokenWeight>();
	}

	public String getValue(){
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString(){
		return this.value;
	}

	/**
	 * Gets list of probabilities associated with tokens for this object definition
	 * @return
	 */
	@JsonIgnore
	public List<TokenWeight> getTokenWeights(){
		return this.token_weights;
	}

	@Override
	public boolean equals(Object o){
		if (this == o) return true;
        if (!(o instanceof Token)) return false;

        Token that = (Token)o;
        if(this.getValue().equals(that.getValue())){
        	return true;
        }
        return false;
	}
}
