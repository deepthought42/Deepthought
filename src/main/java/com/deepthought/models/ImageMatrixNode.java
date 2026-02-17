package com.deepthought.models;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Neo4j node entity that stores an image as a 2D RGB matrix. The matrix is
 * serialized as JSON (int[height][width][3]) for persistence. Types include
 * original image, outline, PCA-transformed, black-and-white, and cropped objects.
 */
@NodeEntity
public class ImageMatrixNode {

	public enum Type {
		ORIGINAL,
		OUTLINE,
		PCA,
		BLACK_WHITE,
		CROPPED_OBJECT
	}

	@Schema(description = "Unique identifier of the ImageMatrixNode.", example = "1", required = true)
	@Id
	@GeneratedValue
	private Long id;

	@Schema(description = "Type of image matrix", required = true)
	@Property
	private String type;

	@Schema(description = "Width of the image in pixels", required = true)
	@Property
	private int width;

	@Schema(description = "Height of the image in pixels", required = true)
	@Property
	private int height;

	@Property
	@JsonIgnore
	private String rgb_matrix_json;

	private static final Gson gson = new GsonBuilder().create();

	public ImageMatrixNode() {
		this.rgb_matrix_json = "";
	}

	public ImageMatrixNode(Type type, int width, int height, int[][][] rgb_matrix) {
		this.type = type.name();
		this.width = width;
		this.height = height;
		setRgbMatrix(rgb_matrix);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setType(Type type) {
		this.type = type != null ? type.name() : null;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Returns the RGB matrix as int[height][width][3] where indices 0,1,2 are R,G,B (0-255).
	 */
	@JsonIgnore
	public int[][][] getRgbMatrix() {
		if (rgb_matrix_json == null || rgb_matrix_json.isEmpty()) {
			return new int[0][0][0];
		}
		return gson.fromJson(rgb_matrix_json, int[][][].class);
	}

	/**
	 * Sets the RGB matrix. Expects int[height][width][3] with R,G,B values 0-255.
	 */
	public void setRgbMatrix(int[][][] rgb_matrix) {
		this.rgb_matrix_json = rgb_matrix != null ? gson.toJson(rgb_matrix) : "";
	}
}
