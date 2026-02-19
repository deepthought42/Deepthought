package com.deepthought.models.edges;

import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.deepthought.models.ImageMatrixNode;

/**
 * Relationship from a derived image node (outline, PCA, black-and-white, or
 * cropped object) to the original image node. Direction: derived --[PART_OF]--> original.
 */
@RelationshipProperties
public class PartOf {

	@Id
	@GeneratedValue
	private Long id;

	@Transient
	private ImageMatrixNode part;

	@TargetNode
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
