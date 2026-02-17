package com.deepthought.models.edges;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.deepthought.models.ImageMatrixNode;

/**
 * Relationship from a derived image node (outline, PCA, black-and-white, or
 * cropped object) to the original image node. Direction: derived --[PART_OF]--> original.
 */
@RelationshipEntity(type = "PART_OF")
public class PartOf {

	@Id
	@GeneratedValue
	private Long id;

	@StartNode
	private ImageMatrixNode part;

	@EndNode
	private ImageMatrixNode whole;

	public PartOf() {
	}

	public PartOf(ImageMatrixNode part, ImageMatrixNode whole) {
		this.part = part;
		this.whole = whole;
	}

	public Long getId() {
		return id;
	}

	public ImageMatrixNode getPart() {
		return part;
	}

	public void setPart(ImageMatrixNode part) {
		this.part = part;
	}

	public ImageMatrixNode getWhole() {
		return whole;
	}

	public void setWhole(ImageMatrixNode whole) {
		this.whole = whole;
	}
}
